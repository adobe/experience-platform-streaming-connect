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

package com.adobe.platform.streaming.sink;

import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthProvider;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenType;
import com.adobe.platform.streaming.auth.impl.AuthProviderFactory;
import com.adobe.platform.streaming.http.HttpProducer;
import com.adobe.platform.streaming.sink.utils.SinkUtils;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Adobe Inc.
 */
public abstract class AbstractAEPPublisher implements DataPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractAEPPublisher.class);

  private static final String AEP_ENDPOINT = "aep.endpoint";
  private static final String AEP_CONNECTION_TIMEOUT = "aep.connection.timeout";
  private static final String AEP_CONNECTION_MAX_RETRIES = "aep.connection.maxRetries";
  private static final String AEP_CONNECTION_MAX_RETRIES_BACKOFF = "aep.connection.retryBackoff";
  private static final String AEP_CONNECTION_READ_TIMEOUT = "aep.connection.readTimeout";

  private static final String AEP_CONNECTION_AUTH_ENABLED = "aep.connection.auth.enabled";
  private static final String AEP_CONNECTION_AUTH_TOKEN_TYPE = "aep.connection.auth.token.type";
  private static final String AEP_CONNECTION_AUTH_CLIENT_ID = "aep.connection.auth.client.id";
  private static final String AEP_CONNECTION_AUTH_CLIENT_CODE = "aep.connection.auth.client.code";
  private static final String AEP_CONNECTION_AUTH_CLIENT_SECRET = "aep.connection.auth.client.secret";
  private static final String AEP_CONNECTION_AUTH_IMS_ORG = "aep.connection.auth.imsOrg";
  private static final String AEP_CONNECTION_AUTH_ACCOUNT_KEY = "aep.connection.auth.accountKey";
  private static final String AEP_CONNECTION_AUTH_FILE_PATH = "aep.connection.auth.filePath";

  private static final String AEP_CONNECTION_AUTH_ENABLED_VALUE = "true";
  private static final String AEP_CONNECTION_AUTH_DISABLED_VALUE = "false";

  protected HttpProducer getHttpProducer(Map<String, String> props) {
    return HttpProducer.newBuilder(props.get(AEP_ENDPOINT))
      .withConnectTimeout(SinkUtils.getProperty(props, AEP_CONNECTION_TIMEOUT, 5000))
      .withReadTimeout(SinkUtils.getProperty(props, AEP_CONNECTION_READ_TIMEOUT, 60000))
      .withMaxRetries(SinkUtils.getProperty(props, AEP_CONNECTION_MAX_RETRIES, 3))
      .withRetryBackoff(SinkUtils.getProperty(props, AEP_CONNECTION_MAX_RETRIES_BACKOFF, 300))
      .withAuth(getAuthProvider(props))
      .build();
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
      .build());
  }

  private AuthProvider getJWTTokenProvider(Map<String, String> props) throws AuthException {
    return AuthProviderFactory.getAuthProvider(TokenType.JWT_TOKEN, ImmutableMap.<String, String>builder()
      .put(AuthUtils.AUTH_CLIENT_ID, props.get(AEP_CONNECTION_AUTH_CLIENT_ID))
      .put(AuthUtils.AUTH_IMS_ORG_ID, props.get(AEP_CONNECTION_AUTH_IMS_ORG))
      .put(AuthUtils.AUTH_TECHNICAL_ACCOUNT_ID, props.get(AEP_CONNECTION_AUTH_ACCOUNT_KEY))
      .put(AuthUtils.AUTH_PRIVATE_KEY_FILE_PATH, props.get(AEP_CONNECTION_AUTH_FILE_PATH))
      .build());
  }

}
