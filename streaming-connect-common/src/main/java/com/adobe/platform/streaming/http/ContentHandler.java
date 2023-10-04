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

import com.adobe.platform.streaming.JacksonFactory;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Adobe Inc.
 */
public abstract class ContentHandler<T> {

  private static final ContentHandler<Void> NULL_HANDLER = new ContentHandler<Void>() {
    @Override
    public Void getContent(HttpConnection conn) {
      return null;
    }

    @Override
    public String getContentType() {
      return null;
    }
  };

  private static final ContentHandler<JsonNode> JSON_HANDLER = new ContentHandler<JsonNode>() {

    @Override
    public JsonNode getContent(HttpConnection conn) throws HttpException {
      try (InputStream in = conn.getInputStream()) {
        return JacksonFactory.OBJECT_MAPPER.readTree(in);
      } catch (IOException e) {
        throw new HttpException("Error parsing content", e, 405);
      }
    }

    @Override
    public String getContentType() {
      return ContentType.APPLICATION_JSON.getMimeType();
    }
  };

  public static ContentHandler<Void> nullHandler() {
    return NULL_HANDLER;
  }

  public static ContentHandler<JsonNode> jsonHandler() {
    return JSON_HANDLER;
  }

  public abstract T getContent(HttpConnection conn) throws HttpException;

  public abstract String getContentType();

}
