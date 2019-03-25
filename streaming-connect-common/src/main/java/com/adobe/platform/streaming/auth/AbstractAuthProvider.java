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

import com.adobe.platform.streaming.http.ContentHandler;
import com.adobe.platform.streaming.http.HttpConnection;
import com.adobe.platform.streaming.http.HttpException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Adobe Inc.
 */
public abstract class AbstractAuthProvider implements AuthProvider {

  private static final long TOKEN_EXPIRATION_THRESHOLD = 30000;
  private static final long DEFAULT_TOKEN_UPDATE_THRESHOLD = 60000;
  private static final ObjectMapper mapper = new ObjectMapper();

  private final transient Lock tokenUpdateLock = new ReentrantLock();
  private transient String accessToken;
  private transient volatile long expireTime;

  @Override
  public String getToken() throws AuthException {
    if (isExpired()) {
      refreshTokenIfNecessary();
    } else if (requiresUpdate()) {
      if (tokenUpdateLock.tryLock()) {
        try {
          refreshTokenIfNecessary();
        } finally {
          tokenUpdateLock.unlock();
        }
      }
    }

    return accessToken;
  }

  protected ContentHandler<TokenResponse> getContentHandler() {
    return new ContentHandler<TokenResponse>() {
      @Override
      public TokenResponse getContent(HttpConnection conn) throws HttpException {
        try (InputStream in = conn.getInputStream()) {
          return mapper.readValue(in, TokenResponse.class);
        } catch (HttpException | IOException e) {
          throw new HttpException("Error parsing response", e);
        }
      }
    };
  }

  private boolean isExpired() {
    return System.currentTimeMillis() > expireTime;
  }

  private boolean requiresUpdate() {
    return System.currentTimeMillis() > (expireTime - DEFAULT_TOKEN_UPDATE_THRESHOLD);
  }

  private synchronized void refreshTokenIfNecessary() throws AuthException {
    if (requiresUpdate()) {
      TokenResponse tokenResponse = getTokenResponse();
      expireTime = System.currentTimeMillis() + tokenResponse.getExpiresIn() - TOKEN_EXPIRATION_THRESHOLD;
      accessToken = tokenResponse.getAccessToken();
    }
  }

  protected abstract TokenResponse getTokenResponse() throws AuthException;

}
