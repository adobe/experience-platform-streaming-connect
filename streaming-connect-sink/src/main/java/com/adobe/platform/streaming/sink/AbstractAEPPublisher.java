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

package com.adobe.platform.streaming.sink;

import com.adobe.platform.streaming.AEPStreamingException;
import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthProvider;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenType;
import com.adobe.platform.streaming.auth.impl.AuthProviderFactory;
import com.adobe.platform.streaming.auth.impl.AuthProxyConfiguration;
import com.adobe.platform.streaming.http.HttpProducer;
import com.adobe.platform.streaming.sink.utils.SinkUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Adobe Inc.
 */
public abstract class AbstractAEPPublisher implements DataPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractAEPPublisher.class);
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final MapType HEADER_MAP_TYPE = OBJECT_MAPPER
    .getTypeFactory().constructMapType(TreeMap.class, String.class, String.class);
  private static final String AEP_ENDPOINT = "aep.endpoint";

  private static final String AEP_CONNECTION_PROXY_HOST = "aep.connection.proxy.host";
  private static final String AEP_CONNECTION_PROXY_PORT = "aep.connection.proxy.port";
  private static final String AEP_CONNECTION_PROXY_USER = "aep.connection.proxy.user";
  private static final String AEP_CONNECTION_PROXY_PASSWORD = "aep.connection.proxy.password";

  private static final String AEP_CONNECTION_TIMEOUT = "aep.connection.timeout";
  private static final String AEP_CONNECTION_MAX_RETRIES = "aep.connection.maxRetries";
  private static final String AEP_CONNECTION_MAX_RETRIES_BACKOFF = "aep.connection.retryBackoff";
  private static final String AEP_CONNECTION_READ_TIMEOUT = "aep.connection.readTimeout";

  private static final String AEP_CONNECTION_AUTH_ENABLED = "aep.connection.auth.enabled";
  private static final String AEP_CONNECTION_AUTH_TOKEN_TYPE = "aep.connection.auth.token.type";
  private static final String AEP_CONNECTION_AUTH_CLIENT_ID = "aep.connection.auth.client.id";
  private static final String AEP_CONNECTION_AUTH_CLIENT_CODE = "aep.connection.auth.client.code";
  private static final String AEP_CONNECTION_AUTH_CLIENT_SECRET = "aep.connection.auth.client.secret";
  private static final String AEP_CONNECTION_AUTH_ENDPOINT = "aep.connection.auth.endpoint";
  private static final String AEP_CONNECTION_AUTH_IMS_ORG = "aep.connection.auth.imsOrg";
  private static final String AEP_CONNECTION_AUTH_ACCOUNT_KEY = "aep.connection.auth.accountKey";
  private static final String AEP_CONNECTION_AUTH_FILE_PATH = "aep.connection.auth.filePath";

  private static final String AEP_CONNECTION_OPTIONAL_HEADER = "aep.connection.endpoint.headers";

  private static final String AEP_CONNECTION_AUTH_ENABLED_VALUE = "true";
  private static final String AEP_CONNECTION_AUTH_DISABLED_VALUE = "false";

  protected HttpProducer getHttpProducer(Map<String, String> props) throws AEPStreamingException {
    HttpProducer.HttpProducerBuilder builder = HttpProducer.newBuilder(getAepEndpoint(props.get(AEP_ENDPOINT)))
      .withProxyHost(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_HOST, null))
      .withProxyPort(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_PORT, 443))
      .withProxyUser(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_USER, null))
      .withProxyPassword(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_PASSWORD, null))
      .withConnectTimeout(SinkUtils.getProperty(props, AEP_CONNECTION_TIMEOUT, 5000))
      .withReadTimeout(SinkUtils.getProperty(props, AEP_CONNECTION_READ_TIMEOUT, 60000))
      .withMaxRetries(SinkUtils.getProperty(props, AEP_CONNECTION_MAX_RETRIES, 3))
      .withRetryBackoff(SinkUtils.getProperty(props, AEP_CONNECTION_MAX_RETRIES_BACKOFF, 300))
      .withAuth(getAuthProvider(props));
    try {
      builder = builder.withHeaders(getHeaders(props.get(AEP_CONNECTION_OPTIONAL_HEADER)));
    } catch (JsonProcessingException e) {
      LOG.error("Unable to add HTTP Headers from '{}'", AEP_CONNECTION_OPTIONAL_HEADER, e);
    }
    return builder.build();
  }

  @Override
  public void start() {
    LOG.info("Starting AEP publisher");
  }

  private String getAepEndpoint(String aepEndpoint) throws AEPStreamingException {
    if (StringUtils.isEmpty(aepEndpoint)) {
      throw new AEPStreamingException("Invalid AEP Endpoint to publish");
    }

    return aepEndpoint.replace("/collection/", "/collection/batch/");
  }

  private Map<String, String> getHeaders(String header) throws JsonProcessingException {
    return StringUtils.isEmpty(header) ? Collections.emptyMap() :
           AbstractSinkTask.OBJECT_MAPPER.readValue(header, HEADER_MAP_TYPE);
  }

  private AuthProvider getAuthProvider(Map<String, String> props) {
    try {
      boolean isAuthEnabled = props.getOrDefault(AEP_CONNECTION_AUTH_ENABLED, AEP_CONNECTION_AUTH_DISABLED_VALUE)
        .equals(AEP_CONNECTION_AUTH_ENABLED_VALUE);
      LOG.info("Auth Enabled for DCS Published: {}", isAuthEnabled);
      if (isAuthEnabled) {
        TokenType tokenType = TokenType.getTokenType(props.getOrDefault(
          AEP_CONNECTION_AUTH_TOKEN_TYPE,
          TokenType.JWT_TOKEN.getName())
        );

        return tokenType == TokenType.ACCESS_TOKEN ? getIMSTokenProvider(props) : getJWTTokenProvider(props);
      }
    } catch (AuthException authException) {
      throw new IllegalArgumentException("Exception while instantiating the auth provider", authException);
    }

    return null;
  }

  private AuthProvider getIMSTokenProvider(Map<String, String> props) throws AuthException {
    return AuthProviderFactory.getAuthProvider(TokenType.ACCESS_TOKEN, ImmutableMap.<String, String>builder()
      .put(AuthUtils.AUTH_CLIENT_ID, props.get(AEP_CONNECTION_AUTH_CLIENT_ID))
      .put(AuthUtils.AUTH_CLIENT_CODE, props.get(AEP_CONNECTION_AUTH_CLIENT_CODE))
      .put(AuthUtils.AUTH_CLIENT_SECRET, props.get(AEP_CONNECTION_AUTH_CLIENT_SECRET))
      .put(AuthUtils.AUTH_ENDPOINT, props.get(AEP_CONNECTION_AUTH_ENDPOINT))
      .build(), AuthProxyConfiguration.builder()
      .proxyHost(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_HOST, null))
      .proxyPort(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_PORT, 443))
      .proxyUsername(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_USER, null))
      .proxyPassword(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_PASSWORD, null))
      .build());
  }

  private AuthProvider getJWTTokenProvider(Map<String, String> props) throws AuthException {
    return AuthProviderFactory.getAuthProvider(TokenType.JWT_TOKEN, ImmutableMap.<String, String>builder()
      .put(AuthUtils.AUTH_CLIENT_ID, props.get(AEP_CONNECTION_AUTH_CLIENT_ID))
      .put(AuthUtils.AUTH_CLIENT_SECRET, props.get(AEP_CONNECTION_AUTH_CLIENT_SECRET))
      .put(AuthUtils.AUTH_ENDPOINT, props.get(AEP_CONNECTION_AUTH_ENDPOINT))
      .put(AuthUtils.AUTH_IMS_ORG_ID, props.get(AEP_CONNECTION_AUTH_IMS_ORG))
      .put(AuthUtils.AUTH_TECHNICAL_ACCOUNT_ID, props.get(AEP_CONNECTION_AUTH_ACCOUNT_KEY))
      .put(AuthUtils.AUTH_PRIVATE_KEY_FILE_PATH, props.get(AEP_CONNECTION_AUTH_FILE_PATH))
      .build(), AuthProxyConfiguration.builder()
      .proxyHost(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_HOST, null))
      .proxyPort(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_PORT, 443))
      .proxyUsername(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_USER, null))
      .proxyPassword(SinkUtils.getProperty(props, AEP_CONNECTION_PROXY_PASSWORD, null))
      .build());
  }

}
