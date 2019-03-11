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

/**
 * @author Adobe Inc.
 */
public class HttpException extends Exception {

  public HttpException() {}

  public HttpException(String message) {
    super(message);
  }

  public HttpException(String message, Throwable cause) {
    super(message, cause);
  }

}
