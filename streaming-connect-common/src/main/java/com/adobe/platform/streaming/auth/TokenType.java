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

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author Adobe Inc.
 */
public enum TokenType {

  ACCESS_TOKEN("access_token"),
  JWT_TOKEN("jwt_token"),
  OAUTH2_ACCESS_TOKEN("oauth2_access_token");

  private static final Map<String, TokenType> TOKEN_TYPES = ImmutableMap.<String, TokenType>builder()
    .put(ACCESS_TOKEN.name, ACCESS_TOKEN)
    .put(JWT_TOKEN.name, JWT_TOKEN)
    .put(OAUTH2_ACCESS_TOKEN.name, OAUTH2_ACCESS_TOKEN)
    .build();

  private String name;

  TokenType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static TokenType getTokenType(String name) {
    TokenType tokenType = TOKEN_TYPES.get(name);
    if (tokenType == null) {
      throw new IllegalArgumentException("No token type found: " + name);
    }

    return tokenType;
  }

}
