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

/**
 * @author Adobe Inc.
 */
public class HttpException extends Exception {

  private int responseCode = 500;

  public HttpException(String message) {
    super(message);
  }

  public HttpException(String message, int responseCode) {
    super(message);
    this.responseCode = responseCode;
  }

  public HttpException(String message, Throwable cause) {
    super(message, cause);
  }

  public HttpException(String message, Throwable cause, int responseCode) {
    super(message, cause);
    this.responseCode = responseCode;
  }

  public int getResponseCode() {
    return responseCode;
  }
}
