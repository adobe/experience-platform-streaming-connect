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
