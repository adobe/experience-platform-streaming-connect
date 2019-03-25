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

import com.adobe.platform.streaming.auth.AbstractAuthProvider;
import com.adobe.platform.streaming.auth.AuthException;
import com.adobe.platform.streaming.auth.AuthUtils;
import com.adobe.platform.streaming.auth.TokenResponse;
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpProducer;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Adobe Inc.
 */
public class IMSTokenProvider extends AbstractAuthProvider {

  private static final Logger LOG = LoggerFactory.getLogger(IMSTokenProvider.class);
  private static final String IMS_ENDPOINT_PATH = "/ims/token/v1";

  private String endpoint = System.getenv(AuthUtils.AUTH_ENDPOINT);
  private final String clientId;
  private final String clientCode;
  private final String clientSecret;

  IMSTokenProvider(String clientId, String clientCode, String clientSecret) {
    this.clientId = clientId;
    this.clientCode = clientCode;
    this.clientSecret = clientSecret;
  }

  IMSTokenProvider(String endpoint, String clientId, String clientCode, String clientSecret) {
    this(clientId, clientCode, clientSecret);
    this.endpoint = endpoint;
  }

  @Override
  protected TokenResponse getTokenResponse() throws AuthException {
    LOG.debug("refreshing expired accessToken: {}", clientId);
    StringBuffer params = new StringBuffer()
      .append("grant_type=authorization_code")
      .append("&client_id=").append(clientId)
      .append("&client_secret=").append(clientSecret)
      .append("&code=").append(clientCode);

    try {
      return HttpProducer.newBuilder(endpoint).build().post(
        IMS_ENDPOINT_PATH,
        params.toString().getBytes(),
        ContentType.APPLICATION_FORM_URLENCODED.getMimeType(),
        getContentHandler()
      );
    } catch (HttpException httpException) {
      throw new AuthException("Exception while fetching access token", httpException);
    }
  }

}
