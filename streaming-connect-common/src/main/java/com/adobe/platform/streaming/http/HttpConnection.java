/*
 * Copyright 2019 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adobe.platform.streaming.http;

import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthProvider;
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
public class HttpConnection {

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
  private AuthProvider auth;
  private String requestMethod = "GET";
  private boolean enableGzip;
  private boolean isPostDataCompressed;

  private transient int retries;
  private transient HttpURLConnection conn;

  private HttpConnection() {
    this.headers = new HashMap<>();
    retries = 0;
  }

  @SuppressWarnings("squid:S3776")
  HttpURLConnection connect() throws HttpException {
    Throwable cause = null;
    int responseCode = 500;
    String errorMsg = "";

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

        if (auth != null) {
          conn.setRequestProperty("Authorization", "Bearer " + auth.getToken());
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

        responseCode = conn.getResponseCode();
        if (HttpUtil.is2xx(responseCode)) {
          break;
        }

        errorMsg = errorStreamToString();
        if (HttpUtil.is5xx(responseCode)) {
          LOG.warn("attempt {} of {} failed with {} response - {}", retries, maxRetries, responseCode, errorMsg);
          close();
          HttpUtil.sleepUninterrupted(retryBackoff);
        } else if (HttpUtil.isUnauthorized(responseCode)) {
          throw new AuthException(String.format("requested failed. unauthorized to access the endpoint. " +
            "response code %s", responseCode));
        } else {
          throw new HttpException("request failed (" + responseCode + "): " + errorMsg, responseCode);
        }
      } catch (MalformedURLException e) {
        throw new HttpException(("bad withUrl: " + url), e);
      } catch (IOException e) {
        LOG.warn("attempt {} of {} failed with exception - {}", retries, maxRetries, e.getMessage());
        close();
        cause = e;
        HttpUtil.sleepUninterrupted(retryBackoff);
      } catch (AuthException authException) {
        throw new HttpException("exception while fetching the auth token", authException, responseCode);
      }
    }

    if (HttpUtil.is5xx(responseCode)) {
      throw new HttpException("request failed (" + responseCode + "): " + errorMsg, responseCode);
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
    return "gzip".equals(conn.getHeaderField(CONTENT_ENCODING));
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

  public InputStream getInputStream() throws HttpException {
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

    HttpConnectionBuilder withAuth(AuthProvider auth) {
      instance.auth = auth;
      return this;
    }

    HttpConnection build() {
      return instance;
    }
  }

}
