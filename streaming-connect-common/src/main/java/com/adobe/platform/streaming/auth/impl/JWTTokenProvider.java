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

import com.adobe.platform.streaming.JacksonFactory;
import com.adobe.platform.streaming.auth.AbstractAuthProvider;
import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenResponse;
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpProducer;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.TRUE;

/**
 * @author Adobe Inc.
 */
@Deprecated
public class JWTTokenProvider extends AbstractAuthProvider {

  private static final Logger LOG = LoggerFactory.getLogger(JWTTokenProvider.class);
  private static final long JWT_TOKEN_EXPIRATION_THRESHOLD = 86400L;
  private static final long DEFAULT_JWT_TOKEN_UPDATE_THRESHOLD = 60000;
  private static final String IMS_ENDPOINT_PATH = "/ims/exchange/jwt/";

  private static final String JWT_EXPIRY_KEY = "exp";
  private static final String JWT_ISS_KEY = "iss";
  private static final String JWT_AUD_KEY = "aud";
  private static final String JWT_SUB_KEY = "sub";

  private String endpoint = System.getenv(AuthUtils.AUTH_ENDPOINT);
  private final String imsOrgId;
  private final String technicalAccountKey;
  private final String clientId;
  private final String clientSecret;
  private final String keyPath;
  private String jwtToken;
  private HttpProducer httpProducer;

  JWTTokenProvider(String endpoint, String clientId, String clientSecret, String imsOrgId, String technicalAccountKey,
    String keyPath, AuthProxyConfiguration authProxyConfiguration) {
    this(clientId, clientSecret, imsOrgId, technicalAccountKey, keyPath, authProxyConfiguration);
    this.endpoint = endpoint;
    this.httpProducer = HttpProducer.newBuilder(endpoint)
        .withProxyHost(authProxyConfiguration.getProxyHost())
        .withProxyPort(authProxyConfiguration.getProxyPort())
        .withProxyUser(authProxyConfiguration.getProxyUsername())
        .withProxyPassword(authProxyConfiguration.getProxyPassword())
        .build();
  }

  JWTTokenProvider(String clientId, String clientSecret, String imsOrgId, String technicalAccountKey, String keyPath,
    AuthProxyConfiguration authProxyConfiguration) {
    this.imsOrgId = imsOrgId;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.technicalAccountKey = technicalAccountKey;
    this.keyPath = keyPath;
    this.httpProducer = HttpProducer.newBuilder(endpoint)
      .withProxyHost(authProxyConfiguration.getProxyHost())
      .withProxyPort(authProxyConfiguration.getProxyPort())
      .withProxyUser(authProxyConfiguration.getProxyUsername())
      .withProxyPassword(authProxyConfiguration.getProxyPassword())
      .build();
  }

  @Override
  protected TokenResponse getTokenResponse() throws AuthException, IOException {
    LOG.debug("refreshing expired jwtToken: {}", clientId);
    StringBuilder params = new StringBuilder()
      .append("&client_id=").append(clientId)
      .append("&client_secret=").append(clientSecret)
      .append("&jwt_token=").append(getJWTToken());

    try {
      return httpProducer.post(
        IMS_ENDPOINT_PATH,
        params.toString().getBytes(),
        getContentHandler()
      );
    } catch (HttpException httpException) {
      throw new AuthException("Exception while fetching access token", httpException);
    }
  }

  private String getJWTToken() throws AuthException, IOException {
    if (isJWTExpired()) {
      refreshJWTToken();
    }

    return jwtToken;
  }

  private void refreshJWTToken() throws AuthException {
    File file = new File(keyPath);
    if (file.exists()) {
      try {
        Path path = Paths.get(keyPath);
        long size = Files.size(path);
        // Files.readAllBytes throws out of memory exception if the file size exceeds 2GB,
        // We want to ensure that size of private file does not exceeds the expected 1MB.
        if (size > 1024 * 1024) {
          throw new AuthException("Size of private file is greater than 1 MB, file path : " + keyPath);
        }

        // Create JWT payload
        Map<String, Object> jwtClaims = new HashMap<>();
        jwtClaims.put(JWT_ISS_KEY, imsOrgId);
        jwtClaims.put(JWT_SUB_KEY, technicalAccountKey);
        jwtClaims.put(JWT_EXPIRY_KEY, System.currentTimeMillis() / 1000 + JWT_TOKEN_EXPIRATION_THRESHOLD);
        jwtClaims.put(JWT_AUD_KEY, endpoint + "/c/" + clientId);
        for (String metaScope : new String[]{ "ent_dataservices_sdk" }) {
          jwtClaims.put(endpoint + "/s/" + metaScope, TRUE);
        }

        KeySpec ks = new PKCS8EncodedKeySpec(Files.readAllBytes(path));
        RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(ks);
        jwtToken = Jwts.builder().setClaims(jwtClaims).signWith(SignatureAlgorithm.RS256, privateKey).compact();
      } catch (Exception ex) {
        throw new AuthException(ex.getMessage(), ex);
      }
    } else {
      throw new AuthException("File does not exist at location : " + keyPath);
    }
  }

  private boolean isJWTExpired() throws IOException {
    if (StringUtils.isEmpty(jwtToken)) {
      return true;
    }

    String[] parts = jwtToken.split("\\.");
    Base64.Decoder base64Decoder = Base64.getDecoder();
    byte[] tokenBodyBytes = base64Decoder.decode(parts[1].getBytes(StandardCharsets.UTF_8));
    final JsonNode tokenBodyJson = JacksonFactory.OBJECT_MAPPER.readTree(tokenBodyBytes);
    long expiresIn = tokenBodyJson.get(JWT_EXPIRY_KEY).asLong();
    return System.currentTimeMillis() > (expiresIn - DEFAULT_JWT_TOKEN_UPDATE_THRESHOLD);
  }

}
