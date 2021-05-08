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

import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthProvider;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenType;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Adobe Inc.
 */
public final class AuthProviderFactory {

  public static AuthProvider getAuthProvider(TokenType tokenType, Map<String, String> authProperties)
      throws AuthException {
    if (MapUtils.isEmpty(authProperties)) {
      throw new AuthException("Invalid properties to get auth provider");
    }

    switch (tokenType) {
      case ACCESS_TOKEN:
        return getIMSAuthProvider(authProperties);

      case JWT_TOKEN:
        return getJWTAuthProvider(authProperties);

      default:
        throw new AuthException("Invalid token type to get auth provider");
    }
  }

  private static AuthProvider getIMSAuthProvider(Map<String, String> authProperties) {
    String clientId = authProperties.get(AuthUtils.AUTH_CLIENT_ID);
    String clientCode = authProperties.get(AuthUtils.AUTH_CLIENT_CODE);
    String clientSecret = authProperties.get(AuthUtils.AUTH_CLIENT_SECRET);

    Preconditions.checkNotNull(clientId, "Invalid client Id");
    Preconditions.checkNotNull(clientCode, "Invalid client code");
    Preconditions.checkNotNull(clientSecret, "Invalid client secret");

    String endpoint = authProperties.get(AuthUtils.AUTH_ENDPOINT);
    return StringUtils.isEmpty(endpoint) ?
      new IMSTokenProvider(clientId, clientCode, clientSecret) :
      new IMSTokenProvider(endpoint, clientId, clientCode, clientSecret);
  }

  private static AuthProvider getJWTAuthProvider(Map<String, String> authProperties) {
    final String clientId = authProperties.get(AuthUtils.AUTH_CLIENT_ID);
    final String clientSecret = authProperties.get(AuthUtils.AUTH_CLIENT_SECRET);
    final String imsOrgId = authProperties.get(AuthUtils.AUTH_IMS_ORG_ID);
    final String technicalAccountKey = authProperties.get(AuthUtils.AUTH_TECHNICAL_ACCOUNT_ID);
    final String filePath = authProperties.get(AuthUtils.AUTH_PRIVATE_KEY_FILE_PATH);

    Preconditions.checkNotNull(clientId, "Invalid client Id");
    Preconditions.checkNotNull(clientSecret, "Invalid client secret.");
    Preconditions.checkNotNull(imsOrgId, "Invalid IMS Org");
    Preconditions.checkNotNull(technicalAccountKey, "Invalid technical account Id");

    String endpoint = authProperties.get(AuthUtils.AUTH_ENDPOINT);
    return StringUtils.isEmpty(endpoint) ?
      new JWTTokenProvider(clientId, clientSecret, imsOrgId, technicalAccountKey, filePath) :
      new JWTTokenProvider(endpoint, clientId, clientSecret, imsOrgId, technicalAccountKey, filePath);
  }

  private AuthProviderFactory() {}

}
