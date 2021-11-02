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
class IMSTokenProviderTest {

  private static final String TEST_ENDPOINT = "https://ims-na1.adobelogin.com";
  private static final String TEST_CLIENT_ID = "testClientId";
  private static final String TEST_CLIENT_CODE = "testClientCode";
  private static final String TEST_CLIENT_SECRET = "testClientSecret";

  @Test
  void testGetTokenInvalidClientId() {
    IMSTokenProvider imsTokenProvider = new IMSTokenProvider(TEST_ENDPOINT, null, TEST_CLIENT_CODE,
      TEST_CLIENT_SECRET, AuthProxyConfiguration.builder().build());
    assertThrows(AuthException.class, imsTokenProvider::getToken);
  }

  @Test
  void testGetTokenInvalidClientCode() {
    IMSTokenProvider imsTokenProvider = new IMSTokenProvider(TEST_ENDPOINT, TEST_CLIENT_ID, null,
      TEST_CLIENT_SECRET, AuthProxyConfiguration.builder().build());
    assertThrows(AuthException.class, imsTokenProvider::getToken);
  }

  @Test
  void testGetTokenInvalidSecret() {
    IMSTokenProvider imsTokenProvider = new IMSTokenProvider(TEST_ENDPOINT, TEST_CLIENT_ID, TEST_CLIENT_CODE,
      null, AuthProxyConfiguration.builder().build());
    assertThrows(AuthException.class, imsTokenProvider::getToken);
  }

  @Test
  void testGetToken() {
    IMSTokenProvider imsTokenProvider = new IMSTokenProvider(
      TEST_ENDPOINT,
      TEST_CLIENT_ID,
      TEST_CLIENT_CODE,
      TEST_CLIENT_SECRET,
      AuthProxyConfiguration.builder().build()
    );
    assertThrows(AuthException.class, imsTokenProvider::getTokenResponse);
  }

}
