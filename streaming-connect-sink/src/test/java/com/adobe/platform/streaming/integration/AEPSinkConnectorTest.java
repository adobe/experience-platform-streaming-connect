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

import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * @author Adobe Inc.
 */
@Tag("integration")
public class AEPSinkConnectorTest extends AbstractConnectorTest {

  private static final Logger LOG = LoggerFactory.getLogger(AEPSinkConnectorTest.class);

  private static final String AEP_CONNECTOR_CONFIG = "aep-connector.json";
  private static final String AEP_CONNECTOR_IMS_AUTH_CONFIG = "aep-connector-with-ims-auth.json";
  private static final String AEP_CONNECTOR_IMS_AUTH_PROXY_CONFIG = "aep-connector-with-ims-auth-proxy.json";
  private static final String AEP_CONNECTOR_JWT_AUTH_CONFIG = "aep-connector-with-jwt-token.json";
  private static final String AEP_CONNECTOR_JWT_AUTH_PROXY_CONFIG = "aep-connector-with-jwt-token-proxy.json";
  private static final String AEP_CONNECTOR_CONFIG_WITH_PROXY = "aep-connector-with-proxy.json";
  private static final String XDM_PAYLOAD_FILE = "xdm-data.json";

  @BeforeEach
  @Override
  public void setup() throws JsonProcessingException {
    super.setup();
    inletSuccessfulResponse();
    inletSuccessfulResponseViaProxy();
  }

  @Test
  public void aepSinkConnectorTest() throws HttpException, JsonProcessingException, InterruptedException {
    getConnect().kafka().createTopic(TOPIC_NAME, TOPIC_PARTITION);

    LOG.info("Starting connector cluster with connector : {}", CONNECTOR_NAME);
    getConnect().configureConnector(CONNECTOR_NAME, connectorConfig());

    // Send single XDM data to aep sink connector
    getConnect().kafka().produce(TOPIC_NAME, xdmData());

    waitForConnectorStart(CONNECTOR_NAME, 1, 1000);

    // Verify inlet endpoint received 1 XDM record
    getWiremockServer().verify(postRequestedFor(urlEqualTo(getRelativeUrl()))
      .withRequestBody(equalToJson(payloadReceivedXdmData())));
  }

  @Test
  public void aepSinkConnectorIMSAuthenticationTest() throws HttpException, JsonProcessingException,
      InterruptedException {
    inletIMSAuthenticationSuccessfulResponse();
    getConnect().kafka().createTopic(TOPIC_NAME, TOPIC_PARTITION);

    LOG.info("Starting connector cluster with connector : {}", CONNECTOR_NAME);
    getConnect().configureConnector(CONNECTOR_NAME, connectorConfigWithIMSConfig());

    // Send single XDM data to aep sink connector
    getConnect().kafka().produce(TOPIC_NAME, xdmData());

    waitForConnectorStart(CONNECTOR_NAME, 1, 1000);

    // Verify inlet endpoint received 1 XDM record
    getWiremockServer().verify(postRequestedFor(urlEqualTo(getRelativeUrl()))
      .withRequestBody(equalToJson(payloadReceivedXdmData())));
  }

  @Test
  public void aepSinkConnectorJWTAuthenticationTest() throws HttpException, JsonProcessingException,
      InterruptedException {
    inletJWTAuthenticationSuccessfulResponse();
    getConnect().kafka().createTopic(TOPIC_NAME, TOPIC_PARTITION);

    LOG.info("Starting connector cluster with connector : {}", CONNECTOR_NAME);
    getConnect().configureConnector(CONNECTOR_NAME, connectorConfigWithJWTConfig());

    // Send single XDM data to aep sink connector
    getConnect().kafka().produce(TOPIC_NAME, xdmData());

    waitForConnectorStart(CONNECTOR_NAME, 1, 1000);

    // Verify inlet endpoint received 1 XDM record
    getWiremockServer().verify(postRequestedFor(urlEqualTo(getRelativeUrl()))
      .withRequestBody(equalToJson(payloadReceivedXdmData())));
  }

  @Test
  public void aepSinkConnectorIMSAuthenticationProxyTest() throws HttpException, JsonProcessingException,
      InterruptedException {
    inletIMSAuthenticationSuccessfulResponse();
    inletIMSAuthenticationSuccessfulResponseViaProxy();

    getConnect().kafka().createTopic(TOPIC_NAME, TOPIC_PARTITION);

    LOG.info("Starting connector cluster with connector : {}", CONNECTOR_NAME);
    getConnect().configureConnector(CONNECTOR_NAME, connectorConfigWithIMSConfigProxy());

    // Send single XDM data to aep sink connector
    getConnect().kafka().produce(TOPIC_NAME, xdmData());

    waitForConnectorStart(CONNECTOR_NAME, 1, 1000);

    getWiremockServerViaProxy().verify(postRequestedFor(urlEqualTo(getRelativeAuthUrl())));
    // Verify inlet endpoint received 1 XDM record
    getWiremockServerViaProxy().verify(postRequestedFor(urlEqualTo(getRelativeUrl()))
      .withRequestBody(equalToJson(payloadReceivedXdmData())));
    // Check if request from proxy server forward to AEP endpoint
    getWiremockServer().verify(postRequestedFor(urlEqualTo(getRelativeUrl())));
  }

  @Test
  public void aepSinkConnectorJWTAuthenticationProxyTest() throws HttpException, JsonProcessingException,
      InterruptedException {
    inletJWTAuthenticationSuccessfulResponse();
    inletJWTAuthenticationSuccessfulResponseViaProxy();

    getConnect().kafka().createTopic(TOPIC_NAME, TOPIC_PARTITION);

    LOG.info("Starting connector cluster with connector : {}", CONNECTOR_NAME);
    getConnect().configureConnector(CONNECTOR_NAME, connectorConfigWithJWTConfigProxy());

    // Send single XDM data to aep sink connector
    getConnect().kafka().produce(TOPIC_NAME, xdmData());

    waitForConnectorStart(CONNECTOR_NAME, 1, 1000);

    getWiremockServerViaProxy().verify(postRequestedFor(urlEqualTo(getRelativeJWTAuthUrl())));
    // Verify inlet endpoint received 1 XDM record
    getWiremockServerViaProxy().verify(postRequestedFor(urlEqualTo(getRelativeUrl()))
      .withRequestBody(equalToJson(payloadReceivedXdmData())));

    // Check if request from proxy server forward to AEP endpoint
    getWiremockServer().verify(postRequestedFor(urlEqualTo(getRelativeUrl())));
  }

  @Test
  public void aepSinkConnectorTestViaProxy() throws HttpException, JsonProcessingException, InterruptedException {
    getConnect().kafka().createTopic(TOPIC_NAME, TOPIC_PARTITION);

    LOG.info("Starting connector cluster with connector : {}", CONNECTOR_NAME);
    getConnect().configureConnector(CONNECTOR_NAME, connectorConfigWithProxy());

    // Send single XDM data to aep sink connector
    getConnect().kafka().produce(TOPIC_NAME, xdmData());

    waitForConnectorStart(CONNECTOR_NAME, 1, 1000);

    // Verify inlet endpoint received 1 XDM record
    getWiremockServerViaProxy().verify(postRequestedFor(urlEqualTo(getRelativeUrl()))
      .withRequestBody(equalToJson(payloadReceivedXdmData())));
    // Check if request from proxy server forward to AEP endpoint
    getWiremockServer().verify(postRequestedFor(urlEqualTo(getRelativeUrl())));
  }

  @AfterEach
  @Override
  public void stop() {
    getConnect().deleteConnector(CONNECTOR_NAME);
    super.stop();
  }

  public String xdmData() throws HttpException {
    return HttpUtil.streamToString(this.getClass().getClassLoader().getResourceAsStream(XDM_PAYLOAD_FILE));
  }

  public String payloadReceivedXdmData() throws HttpException, JsonProcessingException {
    String xdmData = xdmData();
    ObjectNode messageNode = MAPPER.createObjectNode();
    ArrayNode xdmDataValues = MAPPER.createArrayNode();
    xdmDataValues.add(MAPPER.readTree(xdmData));

    messageNode.set("messages", xdmDataValues);
    return MAPPER.writeValueAsString(messageNode);
  }

  public Map<String, String> connectorConfigWithIMSConfig() throws HttpException, JsonProcessingException {
    String connectorProperties = String.format(HttpUtil.streamToString(this.getClass().getClassLoader()
      .getResourceAsStream(AEP_CONNECTOR_IMS_AUTH_CONFIG)),
      NUMBER_OF_TASKS,
      getInletUrl(),
      getBaseUrl());

    Map<String, String> connectorConfig = MAPPER.readValue(connectorProperties, MAP_TYPE_JSON);
    connectorConfig.put("name", CONNECTOR_NAME);
    return connectorConfig;
  }

  public Map<String, String> connectorConfigWithJWTConfig() throws HttpException, JsonProcessingException {
    String connectorProperties = String.format(HttpUtil.streamToString(this.getClass().getClassLoader()
      .getResourceAsStream(AEP_CONNECTOR_JWT_AUTH_CONFIG)),
      NUMBER_OF_TASKS,
      getInletUrl(),
      getBaseUrl(),
      this.getClass().getClassLoader().getResource("secret.key").getPath());

    Map<String, String> connectorConfig = MAPPER.readValue(connectorProperties, MAP_TYPE_JSON);
    connectorConfig.put("name", CONNECTOR_NAME);
    return connectorConfig;
  }

  public Map<String, String> connectorConfigWithIMSConfigProxy() throws HttpException, JsonProcessingException {
    String connectorProperties = String.format(HttpUtil.streamToString(this.getClass().getClassLoader()
      .getResourceAsStream(AEP_CONNECTOR_IMS_AUTH_PROXY_CONFIG)),
      NUMBER_OF_TASKS,
      getInletUrl(),
      PORT_VIA_PROXY,
      getBaseUrl());

    Map<String, String> connectorConfig = MAPPER.readValue(connectorProperties, MAP_TYPE_JSON);
    connectorConfig.put("name", CONNECTOR_NAME);
    return connectorConfig;
  }

  public Map<String, String> connectorConfigWithJWTConfigProxy() throws HttpException, JsonProcessingException {
    String connectorProperties = String.format(HttpUtil.streamToString(this.getClass().getClassLoader()
      .getResourceAsStream(AEP_CONNECTOR_JWT_AUTH_PROXY_CONFIG)),
      NUMBER_OF_TASKS,
      getInletUrl(),
      PORT_VIA_PROXY,
      getBaseUrl(),
      this.getClass().getClassLoader().getResource("secret.key").getPath());

    Map<String, String> connectorConfig = MAPPER.readValue(connectorProperties, MAP_TYPE_JSON);
    connectorConfig.put("name", CONNECTOR_NAME);
    return connectorConfig;
  }

  public Map<String, String> connectorConfig() throws HttpException, JsonProcessingException {
    String connectorProperties = String.format(HttpUtil.streamToString(this.getClass().getClassLoader()
      .getResourceAsStream(AEP_CONNECTOR_CONFIG)),
      NUMBER_OF_TASKS,
      getInletUrl());

    Map<String, String> connectorConfig = MAPPER.readValue(connectorProperties, MAP_TYPE_JSON);
    connectorConfig.put("name", CONNECTOR_NAME);
    return connectorConfig;
  }

  public Map<String, String> connectorConfigWithProxy() throws HttpException, JsonProcessingException {
    String connectorProperties = String.format(HttpUtil.streamToString(this.getClass().getClassLoader()
      .getResourceAsStream(AEP_CONNECTOR_CONFIG_WITH_PROXY)),
      NUMBER_OF_TASKS,
      getInletUrl(),
      PORT_VIA_PROXY);

    Map<String, String> connectorConfig = MAPPER.readValue(connectorProperties, MAP_TYPE_JSON);
    connectorConfig.put("name", CONNECTOR_NAME);
    return connectorConfig;
  }

}
