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
class IMSTokenProviderTest {

  private static final String TEST_ENDPOINT = "https://ims-na1.adobelogin.com";
  private static final String TEST_CLIENT_ID = "testClientId";
  private static final String TEST_CLIENT_CODE = "testClientCode";
  private static final String TEST_CLIENT_SECRET = "testClientSecret";

  @Test
  void testGetTokenInvalidClientId() {
    IMSTokenProvider imsTokenProvider = new IMSTokenProvider(TEST_ENDPOINT, null, TEST_CLIENT_CODE, TEST_CLIENT_SECRET);
    assertThrows(AuthException.class, imsTokenProvider::getToken);
  }

  @Test
  void testGetTokenInvalidClientCode() {
    IMSTokenProvider imsTokenProvider = new IMSTokenProvider(TEST_ENDPOINT, TEST_CLIENT_ID, null, TEST_CLIENT_SECRET);
    assertThrows(AuthException.class, imsTokenProvider::getToken);
  }

  @Test
  void testGetTokenInvalidSecret() {
    IMSTokenProvider imsTokenProvider = new IMSTokenProvider(TEST_ENDPOINT, TEST_CLIENT_ID, TEST_CLIENT_CODE, null);
    assertThrows(AuthException.class, imsTokenProvider::getToken);
  }

  @Test
  void testGetToken() {
    IMSTokenProvider imsTokenProvider = new IMSTokenProvider(
      TEST_ENDPOINT,
      TEST_CLIENT_ID,
      TEST_CLIENT_CODE,
      TEST_CLIENT_SECRET
    );
    assertThrows(AuthException.class, imsTokenProvider::getTokenResponse);
  }

}
