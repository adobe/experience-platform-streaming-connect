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

package com.adobe.platform.streaming.http;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Adobe Inc.
 */
public abstract class ContentHandler<T> {

  private static ContentHandler<Void> NULL_HANDLER = new ContentHandler<Void>() {
    @Override
    public Void getContent(HttpConnection conn) {
      return null;
    }
  };

  private static ContentHandler<JSONObject> JSON_HANDLER = new ContentHandler<JSONObject>() {
    @Override
    public JSONObject getContent(HttpConnection conn) throws HttpException {
      try (InputStream in = conn.getInputStream()) {
        return new JSONObject(HttpUtil.streamToString(in));
      } catch (IOException e) {
        throw new HttpException("Error parsing content", e);
      }
    }
  };

  public static ContentHandler<Void> nullHandler() {
    return NULL_HANDLER;
  }

  public static ContentHandler<JSONObject> jsonHandler() {
    return JSON_HANDLER;
  }

  public abstract T getContent(HttpConnection conn) throws HttpException;

}
