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
import com.fasterxml.jackson.core.type.TypeReference;
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
  private static final String XDM_PAYLOAD_FILE = "xdm-data.json";

  @BeforeEach
  @Override
  public void setup() throws JsonProcessingException {
    super.setup();
    inletSuccessfulResponse();
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

  public Map<String, String> connectorConfig() throws HttpException, JsonProcessingException {
    String connectorProperties = String.format(HttpUtil.streamToString(this.getClass().getClassLoader()
      .getResourceAsStream(AEP_CONNECTOR_CONFIG)),
      NUMBER_OF_TASKS,
      getInletUrl());

    Map<String, String> connectorConfig = MAPPER.readValue(connectorProperties,
      new TypeReference<Map<String, String>>() {});
    connectorConfig.put("name", CONNECTOR_NAME);
    return connectorConfig;
  }

}
