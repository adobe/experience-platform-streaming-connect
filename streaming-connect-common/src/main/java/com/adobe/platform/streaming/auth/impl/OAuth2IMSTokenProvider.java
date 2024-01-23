/*
 * Copyright 2024 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adobe.platform.streaming.auth.impl;

import com.adobe.platform.streaming.auth.AbstractAuthProvider;
import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenResponse;
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Adobe Inc.
 */
public class OAuth2IMSTokenProvider extends AbstractAuthProvider {

  private static final Logger LOG = LoggerFactory.getLogger(OAuth2IMSTokenProvider.class);
  private static final String IMS_ENDPOINT_PATH = "/ims/token/v3";
  private static final String GRANT_TYPE = "client_credentials";
  private static final String SCOPE = "openid,AdobeID,read_organizations,additional_info.projectedProductContext," +
    "session";

  private String endpoint = System.getenv(AuthUtils.AUTH_ENDPOINT);

  private final String clientId;
  private final String clientSecret;
  private HttpProducer httpProducer;

  OAuth2IMSTokenProvider(final String clientId, final String clientSecret,
    final AuthProxyConfiguration authProxyConfiguration) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.httpProducer = HttpProducer.newBuilder(endpoint)
      .withProxyHost(authProxyConfiguration.getProxyHost())
      .withProxyPort(authProxyConfiguration.getProxyPort())
      .withProxyUser(authProxyConfiguration.getProxyUsername())
      .withProxyPassword(authProxyConfiguration.getProxyPassword())
      .build();
  }

  OAuth2IMSTokenProvider(final String endpoint, final String clientId, final String clientSecret,
    final AuthProxyConfiguration authProxyConfiguration) {
    this(clientId, clientSecret, authProxyConfiguration);
    this.endpoint = endpoint;
    this.httpProducer = HttpProducer.newBuilder(endpoint)
      .withProxyHost(authProxyConfiguration.getProxyHost())
      .withProxyPort(authProxyConfiguration.getProxyPort())
      .withProxyUser(authProxyConfiguration.getProxyUsername())
      .withProxyPassword(authProxyConfiguration.getProxyPassword())
      .build();
  }

  @Override
  protected TokenResponse getTokenResponse() throws AuthException {
    LOG.debug("refreshing expired oauth2 accessToken: {}", clientId);
    StringBuilder params = new StringBuilder()
      .append("grant_type=").append(GRANT_TYPE)
      .append("&client_id=").append(clientId)
      .append("&client_secret=").append(clientSecret)
      .append("&scope=").append(SCOPE);

    try {
      final TokenResponse tokenResponse = httpProducer.post(IMS_ENDPOINT_PATH, params.toString().getBytes(),
        getContentHandler());
      // As the expiresIn time we get from the API is in seconds we need to convert this into milliseconds
      final long expiresInMilliSecond = tokenResponse.getExpiresIn() * 1000;
      final TokenResponse updatedTokenResponse = new TokenResponse(tokenResponse.getTokenType(), expiresInMilliSecond,
        tokenResponse.getRefreshToken(), tokenResponse.getAccessToken());
      return updatedTokenResponse;
    } catch (HttpException httpException) {
      throw new AuthException("Exception while fetching oauth2 access token", httpException);
    }
  }

}
