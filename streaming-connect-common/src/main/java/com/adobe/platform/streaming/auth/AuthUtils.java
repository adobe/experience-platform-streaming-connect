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

import com.adobe.platform.streaming.utils.AbstractStreamingUtils;

/**
 * @author Adobe Inc.
 */
public final class AuthUtils extends AbstractStreamingUtils {

  public static final String AUTH_ENDPOINT = "AUTH_ENDPOINT";
  public static final String AUTH_CLIENT_ID = "auth.client.id";
  public static final String AUTH_CLIENT_CODE = "auth.client.code";
  public static final String AUTH_CLIENT_SECRET = "auth.client.secret";

  public static final String AUTH_IMS_ORG_ID = "auth.client.imsOrgId";
  public static final String AUTH_TECHNICAL_ACCOUNT_ID = "auth.client.technicalAccountKey";
  public static final String AUTH_META_SCOPE = "auth.client.metaScope";
  public static final String AUTH_PRIVATE_KEY_FILE_PATH = "auth.client.filePath";
  public static final String AUTH_PRIVATE_KEY_VALUE = "auth.client.keyValue";

  private AuthUtils() {}
}
