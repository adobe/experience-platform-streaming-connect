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

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author Adobe Inc.
 */
public enum TokenType {

  ACCESS_TOKEN("access_token"),
  JWT_TOKEN("jwt_token");

  private static final Map<String, TokenType> TOKEN_TYPES = ImmutableMap.<String, TokenType>builder()
    .put(ACCESS_TOKEN.name, ACCESS_TOKEN)
    .put(JWT_TOKEN.name, JWT_TOKEN)
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
