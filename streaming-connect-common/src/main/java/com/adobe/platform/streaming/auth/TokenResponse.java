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

