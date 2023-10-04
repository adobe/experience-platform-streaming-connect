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

import com.adobe.platform.streaming.JacksonFactory;
import com.adobe.platform.streaming.http.ContentHandler;
import com.adobe.platform.streaming.http.HttpConnection;
import com.adobe.platform.streaming.http.HttpException;

import org.apache.http.entity.ContentType;

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

  private final transient Lock tokenUpdateLock = new ReentrantLock();
  private transient String accessToken;
  private transient volatile long expireTime;

  @Override
  public String getToken() throws AuthException, HttpException {
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
          return JacksonFactory.OBJECT_MAPPER.readValue(in, TokenResponse.class);
        } catch (IOException e) {
          throw new HttpException("Error parsing response", e);
        }
      }

      @Override
      public String getContentType() {
        return ContentType.APPLICATION_FORM_URLENCODED.getMimeType();
      }
    };
  }

  private boolean isExpired() {
    return System.currentTimeMillis() > expireTime;
  }

  private boolean requiresUpdate() {
    return System.currentTimeMillis() > (expireTime - DEFAULT_TOKEN_UPDATE_THRESHOLD);
  }

  private synchronized void refreshTokenIfNecessary() throws AuthException, HttpException {
    if (requiresUpdate()) {
      try {
        TokenResponse tokenResponse = getTokenResponse();
        expireTime = System.currentTimeMillis() + tokenResponse.getExpiresIn() - TOKEN_EXPIRATION_THRESHOLD;
        accessToken = tokenResponse.getAccessToken();
      } catch (IOException e) {
        throw new HttpException("Error parsing JWT Token", e);
      }
    }
  }

  protected abstract TokenResponse getTokenResponse() throws AuthException, IOException;

}
