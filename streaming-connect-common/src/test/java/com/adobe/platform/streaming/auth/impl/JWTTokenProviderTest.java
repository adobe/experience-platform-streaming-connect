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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Adobe Inc.
 */
class JWTTokenProviderTest {

  private static final String ENDPOINT = "https://ims-na1.adobelogin.com";
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
