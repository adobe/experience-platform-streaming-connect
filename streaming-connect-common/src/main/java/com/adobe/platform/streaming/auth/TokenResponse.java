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

package com.adobe.platform.streaming.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * @author Adobe Inc.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponse {
  private final String tokenType;
  private final long expiresIn;
  private final String refreshToken;
  private final String accessToken;

  @JsonCreator
  public TokenResponse(@JsonProperty("token_type") String tokenType, @JsonProperty("expires_in") long expiresIn,
    @JsonProperty("refresh_token") String refreshToken, @JsonProperty("access_token") String accessToken) {
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.refreshToken = refreshToken;
    this.accessToken = accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }

}

