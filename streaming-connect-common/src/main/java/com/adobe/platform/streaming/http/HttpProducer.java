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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author Adobe Inc.
 */
public class HttpProducer implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(HttpProducer.class);
  private static final String CONTENT_TYPE = "Content-type";

  private String endpoint;
  private int connectTimeout;
  private int readTimeout;
  private int maxRetries;
  private int retryBackoff;
  private boolean enableGzip;

  public HttpProducer(String endpoint) {
    LOG.info("in init: {}", endpoint);
    this.endpoint = endpoint;
    this.maxRetries = 3;
    this.retryBackoff = 300;
    this.enableGzip = false;
    this.connectTimeout = 5000;
    this.readTimeout = 60000;
  }

  public <T> T post(String url, byte[] postData, String contentType, ContentHandler<T> handler) throws HttpException {
    Map<String, String> headers = Collections.singletonMap(CONTENT_TYPE, contentType);
    return post(url, postData, headers, handler);
  }

  private <T> T post(String url, byte[] postData, Map<String, String> headers, ContentHandler<T> handler)
      throws HttpException {
    HttpConnection conn = newConnectionBuilder()
      .withUrl(url)
      .withHeaders(headers)
      .withPostData(postData)
      .withGzipCompression(enableGzip)
      .build();

    try {
      conn.connect();
      return handler.getContent(conn);
    } finally {
      conn.close();
    }
  }

  private HttpConnection.HttpConnectionBuilder newConnectionBuilder() {
    return new HttpConnection.HttpConnectionBuilder()
      .withEndpoint(endpoint)
      .withConnectTimeout(connectTimeout)
      .withReadTimeout(readTimeout)
      .withRetryBackoff(retryBackoff)
      .withMaxRetries(maxRetries);
  }

  public static HttpProducerBuilder newBuilder(String endpoint) {
    return new HttpProducerBuilder(new HttpProducer(endpoint));
  }

  /**
   * @author Adobe Inc.
   */
  public static class HttpProducerBuilder {
    HttpProducer instance;

    HttpProducerBuilder(HttpProducer instance) {
      this.instance = instance;
    }

    public HttpProducerBuilder withReadTimeout(int readTimeout) {
      instance.readTimeout = readTimeout;
      return this;
    }

    public HttpProducerBuilder withConnectTimeout(int connectTimeout) {
      instance.connectTimeout = connectTimeout;
      return this;
    }

    public HttpProducerBuilder withMaxRetries(int maxRetries) {
      instance.maxRetries = maxRetries;
      return this;
    }

    public HttpProducerBuilder withRetryBackoff(int retryBackoff) {
      instance.retryBackoff = retryBackoff;
      return this;
    }

    public HttpProducer build() {
      return instance;
    }

  }
}
