/*
 *  ADOBE CONFIDENTIAL
 *  __________________
 *
 *  Copyright 2019 Adobe
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe.
 */

package com.adobe.platform.streaming.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Adobe Inc.
 */
class HttpConnection {

  private static final Logger LOG = LoggerFactory.getLogger(HttpConnection.class);

  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final String CONTENT_LENGTH = "Content-Length";
  private static final String GZIP = "gzip";

  private String endpoint;
  private String url;
  private Map<String, String> headers;
  private byte[] postData;
  private int maxRetries;
  private int retryBackoff;
  private int connectTimeout;
  private int readTimeout;
  private String requestMethod = "GET";
  private boolean enableGzip;
  private boolean isPostDataCompressed;

  private transient int retries;
  private transient HttpURLConnection conn;

  private HttpConnection() {
    this.headers = new HashMap<>();
    retries = 0;
  }

  HttpURLConnection connect() throws HttpException {
    Throwable cause = null;
    while (retries++ < maxRetries) {
      try {
        URL request = new URL(new URL(endpoint), url);
        LOG.debug("opening connection for: {}", request);
        conn = (HttpURLConnection) request.openConnection();
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod(requestMethod);

        for (Map.Entry<String, String> header : headers.entrySet()) {
          conn.setRequestProperty(header.getKey(), header.getValue());
        }

        if (postData != null) {
          if (enableGzip) {
            if (!isPostDataCompressed) {
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(postData.length);
              try (GZIPOutputStream gzipOS = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOS.write(postData, 0, postData.length);
                gzipOS.finish();
              }
              postData = byteArrayOutputStream.toByteArray();
              isPostDataCompressed = true;
            }
            conn.setRequestProperty(CONTENT_ENCODING, GZIP);
          }
          conn.setRequestProperty(CONTENT_LENGTH, Integer.toString(postData.length));
          conn.setDoOutput(true);
          try (OutputStream postStream = conn.getOutputStream()) {
            postStream.write(postData);
          }
        }

        int responseCode = conn.getResponseCode();
        if (HttpUtil.is2xx(responseCode)) {
          break;
        }

        String errorMsg = errorStreamToString();
        if (HttpUtil.is5xx(responseCode)) {
          LOG.warn("attempt {} of {} failed with {} response - {}", retries, maxRetries, responseCode, errorMsg);
          close();
          HttpUtil.sleepUninterrupted(retryBackoff);
        } else {
          throw new HttpException("request failed (" + responseCode + "): " + errorMsg);
        }
      } catch (MalformedURLException e) {
        throw new HttpException(("bad withUrl: " + url), e);
      } catch (IOException e) {
        LOG.warn("attempt {} of {} failed with exception - {}", retries, maxRetries, e.getMessage());
        close();
        cause = e;
        HttpUtil.sleepUninterrupted(retryBackoff);
      }
    }

    if (conn == null) {
      throw new HttpException("unable to connect", cause);
    }

    return conn;
  }

  private String errorStreamToString() throws IOException, HttpException {
    InputStream errStream = conn.getErrorStream();
    if (isGzip()) {
      return HttpUtil.streamToString(new StreamingGZipInputStream(errStream));
    }

    return HttpUtil.streamToString(errStream);
  }

  private boolean isGzip() {
    return "gzip".equals(conn.getHeaderField("Content-Encoding"));
  }

  void close() {
    if (conn != null) {
      LOG.debug("closing connection for: {}", conn.getURL());

      try {
        InputStream is = conn.getInputStream();
        if (is != null) {
          is.close();
        }
      } catch (IOException ioe) {
        LOG.info("close(): failed to close input stream: {}", ioe.getMessage());
      }

      conn = null;
    }
  }

  InputStream getInputStream() throws HttpException {
    try {
      if (isGzip()) {
        return new GZIPInputStream(conn.getInputStream());
      } else {
        return conn.getInputStream();
      }
    } catch (IOException e) {
      throw new HttpException("problem getting input stream", e);
    }
  }

  /**
   * @author Adobe Inc.
   */
  static class HttpConnectionBuilder {
    private HttpConnection instance;

    HttpConnectionBuilder withEndpoint(String endpoint) {
      instance = new HttpConnection();
      instance.endpoint = endpoint;
      return this;
    }

    HttpConnectionBuilder withUrl(String url) {
      instance.url = url;
      return this;
    }

    HttpConnectionBuilder withHeaders(Map<String, String> headers) {
      instance.headers.putAll(headers);
      return this;
    }

    HttpConnectionBuilder withPostData(byte[] postData) {
      instance.postData = postData;
      instance.requestMethod = "POST";
      return this;
    }

    HttpConnectionBuilder withGzipCompression(boolean b) {
      instance.enableGzip = b;
      return this;
    }

    HttpConnectionBuilder withMaxRetries(int maxRetries) {
      instance.maxRetries = maxRetries;
      return this;
    }

    HttpConnectionBuilder withRetryBackoff(int retryBackoff) {
      instance.retryBackoff = retryBackoff;
      return this;
    }

    HttpConnectionBuilder withConnectTimeout(int connectTimeout) {
      instance.connectTimeout = connectTimeout;
      return this;
    }

    HttpConnectionBuilder withReadTimeout(int readTimeout) {
      instance.readTimeout = readTimeout;
      return this;
    }

    HttpConnection build() {
      return instance;
    }
  }

}
