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

import com.adobe.platform.streaming.auth.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adobe Inc.
 */
public class HttpProducer implements Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(HttpProducer.class);
  private static final String CONTENT_TYPE = "Content-type";

  private final String endpoint;

  private String proxyHost;
  private String proxyPort;
  private String proxyUser;
  private String proxyPassword;

  private final boolean enableGzip;
  private int connectTimeout;
  private transient AuthProvider auth;
  private int readTimeout;
  private int maxRetries;
  private int retryBackoff;
  private Map<String, String> endpointHeaders;

  private HttpProducer(String endpoint) {
    LOG.info("in init: {}", endpoint);
    this.endpoint = endpoint;
    this.maxRetries = 3;
    this.retryBackoff = 300;
    this.enableGzip = false;
    this.connectTimeout = 5000;
    this.readTimeout = 60000;
    this.endpointHeaders = Collections.emptyMap();
  }

  public <T> T post(String url, byte[] postData, String contentType, ContentHandler<T> handler) throws HttpException {
    Map<String, String> headers = new HashMap<>(Collections.singletonMap(CONTENT_TYPE, contentType));
    headers.putAll(endpointHeaders);
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
      .withProxyHost(proxyHost)
      .withProxyPort(proxyHost)
      .withProxyUser(proxyUser)
      .withProxyPassword(proxyPassword)
      .withConnectTimeout(connectTimeout)
      .withAuth(auth)
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

    public HttpProducerBuilder withProxyHost(String proxyHost) {
      instance.proxyHost = proxyHost;
      return this;
    }

    public HttpProducerBuilder withProxyPort(String proxyPort) {
      instance.proxyPort = proxyPort;
      return this;
    }

    public HttpProducerBuilder withProxyUser(String proxyUser) {
      instance.proxyUser = proxyUser;
      return this;
    }

    public HttpProducerBuilder withProxyPassword(String proxyPassword) {
      instance.proxyPassword = proxyPassword;
      return this;
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

    public HttpProducerBuilder withAuth(AuthProvider auth) {
      instance.auth = auth;
      return this;
    }

    public HttpProducerBuilder withHeaders(Map<String, String> headers) {
      instance.endpointHeaders = headers;
      return this;
    }

    public HttpProducer build() {
      return instance;
    }

  }
}
