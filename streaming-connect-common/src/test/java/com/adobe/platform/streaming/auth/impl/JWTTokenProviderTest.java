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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Adobe Inc.
 */
class JWTTokenProviderTest {

  private static final String ENDPOINT = "https://ims-na1-stg1.adobelogin.com";
  private static final String TEST_CLIENT = "testClientId";
  private static final String TEST_ORG = "testImsOrg";
  private static final String TEST_ACCOUNT_ID = "testTechnicalAccountId";
  private static final String TEST_FILE_PATH =
    JWTTokenProviderTest.class.getClassLoader().getResource("secret.key").getPath();
  private static final String TEST_INVALID_FILE_PATH =
    JWTTokenProviderTest.class.getClassLoader().getResource("secret_invalid.key").getPath();

  @Test
  void testGetTokenWithInvalidIMSOrg() {
    JWTTokenProvider tokenProvider = new JWTTokenProvider(ENDPOINT, null, TEST_CLIENT, TEST_ACCOUNT_ID, TEST_FILE_PATH);
    assertThrows(AuthException.class, tokenProvider::getToken);
  }

  @Test
  void testGetTokenWithInvalidClientId() {
    JWTTokenProvider tokenProvider = new JWTTokenProvider(ENDPOINT, TEST_ORG, null, TEST_ACCOUNT_ID, TEST_FILE_PATH);
    assertThrows(AuthException.class, tokenProvider::getToken);
  }

  @Test
  void testGetTokenWithInvalidAccountId() {
    JWTTokenProvider tokenProvider = new JWTTokenProvider(ENDPOINT, TEST_ORG, TEST_CLIENT, null, TEST_FILE_PATH);
    assertThrows(AuthException.class, tokenProvider::getToken);
  }

  @Test
  void testGetTokenInvalidPath() {
    JWTTokenProvider tokenProvider =
      new JWTTokenProvider(ENDPOINT, TEST_ORG, TEST_CLIENT, TEST_ACCOUNT_ID, TEST_INVALID_FILE_PATH);
    assertThrows(AuthException.class, tokenProvider::getToken);
  }

  @Test
  void testGetTokenValidPath() {
    JWTTokenProvider tokenProvider =
      new JWTTokenProvider(ENDPOINT, TEST_ORG, TEST_CLIENT, TEST_ACCOUNT_ID, TEST_FILE_PATH);
    assertThrows(AuthException.class, tokenProvider::getToken);
  }

}
