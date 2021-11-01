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

package com.adobe.platform.streaming.auth.impl;

/**
 * @author Adobe Inc.
 */
public class AuthProxyConfiguration {

  private final String proxyHost;
  private final int proxyPort;
  private final String proxyUsername;
  private final String proxyPassword;

  public AuthProxyConfiguration(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    this.proxyUsername = proxyUsername;
    this.proxyPassword = proxyPassword;
  }

  public String getProxyHost() {
    return proxyHost;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  public String getProxyUsername() {
    return proxyUsername;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public static AuthProxyConfiguration.AuthProxyConfigurationBuilder builder() {
    return new AuthProxyConfiguration.AuthProxyConfigurationBuilder();
  }

  /**
   * @author Adobe Inc.
   */
  public static class AuthProxyConfigurationBuilder {
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    AuthProxyConfigurationBuilder() {
    }

    public AuthProxyConfiguration.AuthProxyConfigurationBuilder proxyHost(String proxyHost) {
      this.proxyHost = proxyHost;
      return this;
    }

    public AuthProxyConfiguration.AuthProxyConfigurationBuilder proxyPort(int proxyPort) {
      this.proxyPort = proxyPort;
      return this;
    }

    public AuthProxyConfiguration.AuthProxyConfigurationBuilder proxyUsername(String proxyUsername) {
      this.proxyUsername = proxyUsername;
      return this;
    }

    public AuthProxyConfiguration.AuthProxyConfigurationBuilder proxyPassword(String proxyPassword) {
      this.proxyPassword = proxyPassword;
      return this;
    }

    public AuthProxyConfiguration build() {
      return new AuthProxyConfiguration(this.proxyHost, this.proxyPort, this.proxyUsername, this.proxyPassword);
    }
  }

}
