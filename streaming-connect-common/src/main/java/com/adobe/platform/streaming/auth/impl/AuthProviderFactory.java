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

package com.adobe.platform.streaming.auth.impl;

import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthProvider;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenType;
import com.google.common.base.Preconditions;
import org.apache.commons.collections.MapUtils;
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
    String clientId = authProperties.get(AuthUtils.AUTH_CLIENT_ID);
    String imsOrgId = authProperties.get(AuthUtils.AUTH_IMS_ORG_ID);
    String technicalAccountKey = authProperties.get(AuthUtils.AUTH_TECHNICAL_ACCOUNT_ID);
    String filePath = authProperties.get(AuthUtils.AUTH_PRIVATE_KEY_FILE_PATH);

    Preconditions.checkNotNull(clientId, "Invalid client Id");
    Preconditions.checkNotNull(imsOrgId, "Invalid IMS Org");
    Preconditions.checkNotNull(technicalAccountKey, "Invalid technical account Id");

    String endpoint = authProperties.get(AuthUtils.AUTH_ENDPOINT);
    return StringUtils.isEmpty(endpoint) ?
      new JWTTokenProvider(clientId, imsOrgId, technicalAccountKey, filePath) :
      new JWTTokenProvider(endpoint, clientId, imsOrgId, technicalAccountKey, filePath);
  }

  private AuthProviderFactory() {}

}
