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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Adobe Inc.
 */
class HttpUtil {

  static boolean is2xx(int responseCode) {
    return (responseCode >= 200 && responseCode < 300);
  }

  static boolean is5xx(int responseCode) {
    return (responseCode >= 500 && responseCode < 600);
  }

  static String streamToString(InputStream in) throws HttpException {
    StringBuilder sb = new StringBuilder(128);
    try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      String str;
      while ((str = r.readLine()) != null) {
        sb.append(str);
      }
    } catch (IOException e) {
      throw new HttpException("problem reading string", e);
    }

    return sb.toString();
  }

  static void sleepUninterrupted(int sleepTime) {
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private HttpUtil() {}

}
