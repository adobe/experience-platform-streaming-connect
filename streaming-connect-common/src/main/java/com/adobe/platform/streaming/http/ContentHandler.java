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
