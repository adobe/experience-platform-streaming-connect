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

package com.adobe.platform.streaming.integration;

import com.adobe.platform.streaming.JacksonFactory;
import com.adobe.platform.streaming.integration.extension.AEPConnectorTestWatcher;
import com.adobe.platform.streaming.integration.extension.WiremockExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.apache.kafka.connect.runtime.WorkerConfig.CONNECTOR_CLIENT_POLICY_CLASS_CONFIG;
import static org.apache.kafka.connect.runtime.WorkerConfig.OFFSET_COMMIT_INTERVAL_MS_CONFIG;

/**
 * @author Adobe Inc.
 */
@ExtendWith(AEPConnectorTestWatcher.class)
public abstract class AbstractConnectorTest {

  protected static final MapLikeType MAP_TYPE_JSON = JacksonFactory.OBJECT_MAPPER.getTypeFactory()
    .constructMapLikeType(TreeMap.class, String.class, String.class);
  private static final long OFFSET_COMMIT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);
  protected static final int HTTP_SERVER_SIDE_ERROR_CODE = 500;
  private static final String AUTH_TOKEN_RESPONSE = "{\"access_token\":\"accessToken\"," +
    "\"refresh_token\":\"refreshToken\",\"token_type\":\"bearer\",\"expires_in\":82399996}";
  private static final String AUTH_TOKEN_RESPONSE_OAUTH2 = "{\"access_token\":\"accessToken\"," +
    "\"token_type\":\"bearer\",\"expires_in\":86400}";

  private static final String AEP_CONNECTOR_INLET_SUCCESSFUL_RESPONSE =
    "aep-connector-inlet-successful-response.json";

  protected static final int TOPIC_PARTITION = 1;
  protected static final int NUMBER_OF_TASKS = 1;
  protected static final String CONNECTOR_NAME = "aep-sink-connector";
  protected static final String TOPIC_NAME = "connect-test";
  protected static final int PORT = 8089;
  protected static final int PORT_VIA_PROXY = 8090;
  private EmbeddedConnectCluster connect;

  private int numberOfWorkers = 1;
  private String inletId = "876e1041c16801b8b3038ec86bb4510e8c89356152191b587367b592e79d91d5";
  private String baseUrl;
  private String relativePath;
  private String relativeImsAuthPath;
  private String relativeJWTAuthPath;
  private String relativeOAuth2IMSAuthPath;

  @RegisterExtension
  public static final WiremockExtension wiremockExtension = new WiremockExtension(PORT);

  @RegisterExtension
  public static final WiremockExtension wiremockExtensionViaProxy = new WiremockExtension(PORT_VIA_PROXY);

  @BeforeEach
  public void setup() throws IOException {
    connect = new EmbeddedConnectCluster.Builder()
        .name("aep-connect-cluster")
        .numWorkers(numberOfWorkers)
        .workerProps(workerProperties())
        .brokerProps(brokerProperties())
        .maskExitProcedures(true)
        .build();
    connect.start();
    baseUrl = String.format("http://localhost:%s", PORT);
    relativePath = String.format("/collection/%s", inletId);
    relativeImsAuthPath = "/ims/token/v1";
    relativeJWTAuthPath = "/ims/exchange/jwt/";
    relativeOAuth2IMSAuthPath = "/ims/token/v3";
  }

  protected void waitForConnectorStart(String connector, int numberOfTask, int waitTimeMs) throws InterruptedException {
    getConnect().assertions().assertConnectorAndAtLeastNumTasksAreRunning(
      connector,
      numberOfTask,
      String.format("Failed to observe transition to 'RUNNING' state for connector '%s' in time", connector));

    Thread.sleep(waitTimeMs);
  }

  protected Map<String, String> workerProperties() {
    Map<String, String> workerProps = new HashMap<>();
    workerProps.put(OFFSET_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(OFFSET_COMMIT_INTERVAL_MS));
    workerProps.put(CONNECTOR_CLIENT_POLICY_CLASS_CONFIG, "All");

    return workerProps;
  }

  protected Properties brokerProperties() {
    Properties brokerProps = new Properties();
    brokerProps.put("auto.create.topics.enable", String.valueOf(false));
    return brokerProps;
  }

  @AfterEach
  public void stop() {
    connect.stop();
  }

  public void inletSuccessfulResponse() throws IOException {
    wiremockExtension.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition()
      .withJsonBody(JacksonFactory.OBJECT_MAPPER.readTree(this.getClass().getClassLoader()
      .getResourceAsStream(AEP_CONNECTOR_INLET_SUCCESSFUL_RESPONSE)))));
  }

  public void inletIMSAuthenticationSuccessfulResponse() throws JsonProcessingException {
    wiremockExtension.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeAuthUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition()
      .withJsonBody(JacksonFactory.OBJECT_MAPPER.readTree(AUTH_TOKEN_RESPONSE))));
  }

  public void inletJWTAuthenticationSuccessfulResponse() throws JsonProcessingException {
    wiremockExtension.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeJWTAuthUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition()
      .withJsonBody(JacksonFactory.OBJECT_MAPPER.readTree(AUTH_TOKEN_RESPONSE))));
  }

  public void inletOAuth2IMSAuthenticationSuccessfulResponse() throws JsonProcessingException {
    wiremockExtension.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeOAuth2IMSAuthUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition()
      .withJsonBody(JacksonFactory.OBJECT_MAPPER.readTree(AUTH_TOKEN_RESPONSE_OAUTH2))));
  }

  public void inletOAuth2IMSAuthenticationSuccessfulResponseViaProxy() throws JsonProcessingException {
    wiremockExtensionViaProxy.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeOAuth2IMSAuthUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition().proxiedFrom(baseUrl)));
  }

  public void inletIMSAuthenticationSuccessfulResponseViaProxy() {
    wiremockExtensionViaProxy.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeAuthUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition().proxiedFrom(baseUrl)));
  }

  public void inletJWTAuthenticationSuccessfulResponseViaProxy() {
    wiremockExtensionViaProxy.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeJWTAuthUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition().proxiedFrom(baseUrl)));
  }

  public void inletSuccessfulResponseViaProxy() {
    wiremockExtensionViaProxy.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition().proxiedFrom(baseUrl)));
  }

  public void inletFailedResponse() {
    wiremockExtension.getWireMockServer()
      .stubFor(WireMock
      .post(WireMock.urlEqualTo(getRelativeUrl()))
      .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(HTTP_SERVER_SIDE_ERROR_CODE)));
  }

  protected WireMockServer getWiremockServer() {
    return wiremockExtension.getWireMockServer();
  }

  protected WireMockServer getWiremockServerViaProxy() {
    return wiremockExtensionViaProxy.getWireMockServer();
  }

  protected String getRelativeUrl() {
    return relativePath.replace("/collection/", "/collection/batch/");
  }

  protected String getRelativeAuthUrl() {
    return relativeImsAuthPath;
  }

  protected String getRelativeJWTAuthUrl() {
    return relativeJWTAuthPath;
  }

  protected String getRelativeOAuth2IMSAuthUrl() {
    return relativeOAuth2IMSAuthPath;
  }

  protected String getInletUrl() {
    return baseUrl.concat(relativePath);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public int getNumberOfWorkers() {
    return numberOfWorkers;
  }

  public void setNumberOfWorkers(int numberOfWorkers) {
    this.numberOfWorkers = numberOfWorkers;
  }

  public EmbeddedConnectCluster getConnect() {
    return connect;
  }

  public void setInletId(String inletId) {
    this.inletId = inletId;
  }

  public String getInletId() {
    return inletId;
  }
}
