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

/**
 * @author Adobe Inc.
 */
public final class AuthUtils {

  public static final String AUTH_ENDPOINT = "AUTH_ENDPOINT";
  public static final String AUTH_CLIENT_ID = "auth.client.id";
  public static final String AUTH_CLIENT_CODE = "auth.client.code";
  public static final String AUTH_CLIENT_SECRET = "auth.client.secret";

  public static final String AUTH_IMS_ORG_ID = "auth.client.imsOrgId";
  public static final String AUTH_TECHNICAL_ACCOUNT_ID = "auth.client.technicalAccountKey";
  public static final String AUTH_META_SCOPE = "auth.client.metaScope";
  public static final String AUTH_PRIVATE_KEY_FILE_PATH = "auth.client.filePath";

  private AuthUtils() {}
}
