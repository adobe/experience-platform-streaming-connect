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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Adobe Inc.
 */
public class WiremockExtension implements BeforeAllCallback, AfterAllCallback {

  private final WireMockServer wireMockServer;

  public WiremockExtension(int port) {
    WireMockConfiguration wireMockConfiguration = new WireMockConfiguration();
    wireMockConfiguration.port(port);
    this.wireMockServer = new WireMockServer(wireMockConfiguration);
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    wireMockServer.start();
  }

  @Override
  public void afterAll(ExtensionContext context) {
    wireMockServer.stop();
  }

  public WireMockServer getWireMockServer() {
    return wireMockServer;
  }
}
