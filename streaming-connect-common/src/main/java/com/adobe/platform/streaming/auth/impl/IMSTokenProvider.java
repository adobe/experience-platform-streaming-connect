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

package com.adobe.platform.streaming.auth.impl;

import com.adobe.platform.streaming.auth.AbstractAuthProvider;
import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenResponse;
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpProducer;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Adobe Inc.
 */
public class IMSTokenProvider extends AbstractAuthProvider {

  private static final Logger LOG = LoggerFactory.getLogger(IMSTokenProvider.class);
  private static final String IMS_ENDPOINT_PATH = "/ims/token/v1";

  private String endpoint = System.getenv(AuthUtils.AUTH_ENDPOINT);
  private final String clientId;
  private final String clientCode;
  private final String clientSecret;
  private HttpProducer httpProducer;

  IMSTokenProvider(String clientId, String clientCode, String clientSecret,
    AuthProxyConfiguration authProxyConfiguration) {
    this.clientId = clientId;
    this.clientCode = clientCode;
    this.clientSecret = clientSecret;
    this.httpProducer = HttpProducer.newBuilder(endpoint)
      .withProxyHost(authProxyConfiguration.getProxyHost())
      .withProxyPort(authProxyConfiguration.getProxyPort())
      .withProxyUser(authProxyConfiguration.getProxyUsername())
      .withProxyPassword(authProxyConfiguration.getProxyPassword())
      .build();
  }

  IMSTokenProvider(String endpoint, String clientId, String clientCode, String clientSecret,
    AuthProxyConfiguration authProxyConfiguration) {
    this(clientId, clientCode, clientSecret, authProxyConfiguration);
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
    LOG.debug("refreshing expired accessToken: {}", clientId);
    StringBuilder params = new StringBuilder()
      .append("grant_type=authorization_code")
      .append("&client_id=").append(clientId)
      .append("&client_secret=").append(clientSecret)
      .append("&code=").append(clientCode);

    try {
      return httpProducer.post(
        IMS_ENDPOINT_PATH,
        params.toString().getBytes(),
        ContentType.APPLICATION_FORM_URLENCODED.getMimeType(),
        getContentHandler()
      );
    } catch (HttpException httpException) {
      throw new AuthException("Exception while fetching access token", httpException);
    }
  }

}
