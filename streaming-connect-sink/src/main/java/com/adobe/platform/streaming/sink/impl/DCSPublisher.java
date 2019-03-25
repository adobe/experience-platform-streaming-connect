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

package com.adobe.platform.streaming.sink.impl;

import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthProvider;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenType;
import com.adobe.platform.streaming.auth.impl.AuthProviderFactory;
import com.adobe.platform.streaming.http.ContentHandler;
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpProducer;
import com.adobe.platform.streaming.sink.DataPublisher;
import com.adobe.platform.streaming.sink.utils.SinkUtils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.kafka.connect.sink.SinkRecord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Adobe Inc.
 */
class DCSPublisher implements DataPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(DCSPublisher.class);
  private static final String DCS_ENDPOINT = "dcs.endpoint";
  private static final String DCS_CONNECTION_TIMEOUT = "dcs.connection.timeout";
  private static final String DCS_CONNECTION_MAX_RETRIES = "dcs.connection.maxRetries";
  private static final String DCS_CONNECTION_MAX_RETRIES_BACKOFF = "dcs.connection.retryBackoff";
  private static final String DCS_CONNECTION_READ_TIMEOUT = "dcs.connection.readTimeout";

  private static final String DCS_CONNECTION_AUTH_ENABLED = "dcs.connection.auth.enabled";
  private static final String DCS_CONNECTION_AUTH_TOKEN_TYPE = "dcs.connection.auth.token.type";
  private static final String DCS_CONNECTION_AUTH_CLIENT_ID = "dcs.connection.auth.client.id";
  private static final String DCS_CONNECTION_AUTH_CLIENT_CODE = "dcs.connection.auth.client.code";
  private static final String DCS_CONNECTION_AUTH_CLIENT_SECRET = "dcs.connection.auth.client.secret";
  private static final String DCS_CONNECTION_AUTH_IMS_ORG = "dcs.connection.auth.imsOrg";
  private static final String DCS_CONNECTION_AUTH_ACCOUNT_KEY = "dcs.connection.auth.accountKey";
  private static final String DCS_CONNECTION_AUTH_FILE_PATH = "dcs.connection.auth.filePath";

  private static final String DCS_CONNECTION_AUTH_ENABLED_VALUE = "true";
  private static final String DCS_CONNECTION_AUTH_DISABLED_VALUE = "false";

  private final HttpProducer producer;
  private final Gson gson;

  DCSPublisher(Map<String, String> props) {
    gson = new GsonBuilder().create();
    producer = HttpProducer.newBuilder(props.get(DCS_ENDPOINT))
      .withConnectTimeout(SinkUtils.getProperty(props, DCS_CONNECTION_TIMEOUT, 5000))
      .withReadTimeout(SinkUtils.getProperty(props, DCS_CONNECTION_READ_TIMEOUT, 60000))
      .withMaxRetries(SinkUtils.getProperty(props, DCS_CONNECTION_MAX_RETRIES, 3))
      .withRetryBackoff(SinkUtils.getProperty(props, DCS_CONNECTION_MAX_RETRIES_BACKOFF, 300))
      .withAuth(getAuthProvider(props))
      .build();
  }

  @Override
  public void sendData(SinkRecord record) {
    try {
      JSONObject response = producer.post(
        StringUtils.EMPTY,
        SinkUtils.getBytePayload(gson, record),
        ContentType.APPLICATION_JSON.getMimeType(),
        ContentHandler.jsonHandler()
      );

      LOG.debug("Successfully published data to DCS: {}", response);
    } catch (HttpException httpException) {
      LOG.error("Failed to publish data to DCS", httpException);
    }
  }

  private AuthProvider getAuthProvider(Map<String, String> props) {
    try {
      boolean isAuthEnabled = props.getOrDefault(DCS_CONNECTION_AUTH_ENABLED, DCS_CONNECTION_AUTH_DISABLED_VALUE)
        .equals(DCS_CONNECTION_AUTH_ENABLED_VALUE);
      LOG.info("Auth Enabled for DCS Published: {}", isAuthEnabled);
      if (isAuthEnabled) {
        TokenType tokenType = TokenType.getTokenType(props.getOrDefault(
          DCS_CONNECTION_AUTH_TOKEN_TYPE,
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
      .put(AuthUtils.AUTH_CLIENT_ID, props.get(DCS_CONNECTION_AUTH_CLIENT_ID))
      .put(AuthUtils.AUTH_CLIENT_CODE, props.get(DCS_CONNECTION_AUTH_CLIENT_CODE))
      .put(AuthUtils.AUTH_CLIENT_SECRET, props.get(DCS_CONNECTION_AUTH_CLIENT_SECRET))
      .build());
  }

  private AuthProvider getJWTTokenProvider(Map<String, String> props) throws AuthException {
    return AuthProviderFactory.getAuthProvider(TokenType.JWT_TOKEN, ImmutableMap.<String, String>builder()
      .put(AuthUtils.AUTH_CLIENT_ID, props.get(DCS_CONNECTION_AUTH_CLIENT_ID))
      .put(AuthUtils.AUTH_IMS_ORG_ID, props.get(DCS_CONNECTION_AUTH_IMS_ORG))
      .put(AuthUtils.AUTH_TECHNICAL_ACCOUNT_ID, props.get(DCS_CONNECTION_AUTH_ACCOUNT_KEY))
      .put(AuthUtils.AUTH_PRIVATE_KEY_FILE_PATH, props.get(DCS_CONNECTION_AUTH_FILE_PATH))
      .build());
  }

}
