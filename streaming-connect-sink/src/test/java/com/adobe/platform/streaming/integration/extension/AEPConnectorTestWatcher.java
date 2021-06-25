/*
 * Copyright 2021 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adobe.platform.streaming.integration.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adobe Inc.
 */
public class AEPConnectorTestWatcher implements BeforeEachCallback, AfterEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    Logger logger = LoggerFactory.getLogger(context.getRequiredTestClass());
    context.getTestMethod().ifPresent(method -> logger.info("Starting connector test : " + method.getName()));
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Logger logger = LoggerFactory.getLogger(context.getRequiredTestClass());
    context.getTestMethod().ifPresent(method -> logger.info("Finished connector test : " + method.getName()));
  }

}
