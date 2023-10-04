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
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * @author Adobe Inc.
 */
@Tag("integration")
public class AEPSinkConnectorErrorReporterTest extends AbstractConnectorTest {

  private static final Logger LOG = LoggerFactory.getLogger(AEPSinkConnectorTest.class);
  private static final String AEP_KAFKA_ERROR_CONNECTOR_CONFIG = "aep-connector-error-reporter.json";
  private static final String AEP_KAFKA_ERROR_CONNECTOR_HEADER_CONFIG = "aep-connector-error-reporter-header.json";
  private static final String XDM_PAYLOAD_FILE = "xdm-data.json";
  private static final String DEAD_LETTER_TOPIC = "errors.deadletterqueue.topic.name";
  private static final String ERROR_CLASS_NAME = "__connect.errors.exception.class.name";
  private static final String ERROR_HEADER_MESSAGE = "__connect.errors.exception.message";
  private static final String EXPECTED_EXCEPTION_CLASS = "com.adobe.platform.streaming.http.HttpException";

  @BeforeEach
  @Override
  public void setup() throws JsonProcessingException {
    super.setup();
    inletFailedResponse();
  }

  @Test
  public void kafkaErrorReporterTest() throws HttpException, IOException, InterruptedException {
    getConnect().kafka().createTopic(TOPIC_NAME, TOPIC_PARTITION);

    // Create error topic to dump failed data
    Map<String, String> connectorConfig = connectorConfig(AEP_KAFKA_ERROR_CONNECTOR_CONFIG);
    getConnect().kafka().createTopic(connectorConfig.get(DEAD_LETTER_TOPIC), TOPIC_PARTITION);

    LOG.info("Starting connector cluster with connector : {}", CONNECTOR_NAME);
    getConnect().configureConnector(CONNECTOR_NAME, connectorConfig);

    String xdmData = xdmData();
    getConnect().kafka().produce(TOPIC_NAME, xdmData);
    waitForConnectorStart(CONNECTOR_NAME, 1, 8000);

    // Check if error record sent to error topic
    ConsumerRecords<byte[], byte[]> consumerRecords = getConnect().kafka()
      .consume(1, 8000, connectorConfig.get(DEAD_LETTER_TOPIC));

    Assertions.assertEquals(1, consumerRecords.count());

    ConsumerRecord<byte[], byte[]> consumerRecord = consumerRecords.iterator().next();
    JsonNode record = JacksonFactory.OBJECT_MAPPER.readTree(consumerRecord.value());

    Assertions.assertEquals(JacksonFactory.OBJECT_MAPPER.readTree(xdmData).toString(), record.toString());

    // Verify inlet endpoint received 1 XDM record
    getWiremockServer().verify(postRequestedFor(urlEqualTo(getRelativeUrl()))
      .withRequestBody(equalToJson(payloadReceivedXdmData())));
  }

  @Test
  public void kafkaErrorReporterWithHeadersTest() throws HttpException, IOException, InterruptedException {
    getConnect().kafka().createTopic(TOPIC_NAME, TOPIC_PARTITION);

    // Create error topic to dump failed data
    Map<String, String> connectorConfig = connectorConfig(AEP_KAFKA_ERROR_CONNECTOR_HEADER_CONFIG);
    getConnect().kafka().createTopic(connectorConfig.get(DEAD_LETTER_TOPIC), TOPIC_PARTITION);

    LOG.info("Starting connector cluster with connector : {}", CONNECTOR_NAME);
    getConnect().configureConnector(CONNECTOR_NAME, connectorConfig);

    String xdmData = xdmData();
    getConnect().kafka().produce(TOPIC_NAME, xdmData);
    waitForConnectorStart(CONNECTOR_NAME, 1, 8000);

    // Check if error record sent to error topic
    ConsumerRecords<byte[], byte[]> consumerRecords = getConnect().kafka()
      .consume(1, 8000, connectorConfig.get(DEAD_LETTER_TOPIC));

    Assertions.assertEquals(1, consumerRecords.count());

    ConsumerRecord<byte[], byte[]> consumerRecord = consumerRecords.iterator().next();
    JsonNode record = JacksonFactory.OBJECT_MAPPER.readTree(consumerRecord.value());

    Assertions.assertEquals(JacksonFactory.OBJECT_MAPPER.readTree(xdmData).toString(), record.toString());

    final Headers errorHeaders = consumerRecord.headers();
    errorHeaders.headers(ERROR_CLASS_NAME)
      .forEach(header -> Assertions.assertEquals(EXPECTED_EXCEPTION_CLASS, new String(header.value())));
    errorHeaders.headers(ERROR_HEADER_MESSAGE).forEach(header ->
      Assertions.assertTrue(new String(header.value()).contains(String.valueOf(HTTP_SERVER_SIDE_ERROR_CODE))));

    // Verify inlet endpoint received 1 XDM record
    getWiremockServer().verify(postRequestedFor(urlEqualTo(getRelativeUrl()))
      .withRequestBody(equalToJson(payloadReceivedXdmData())));
  }

  public String payloadReceivedXdmData() throws HttpException, JsonProcessingException {
    String xdmData = xdmData();
    ObjectNode messageNode = JacksonFactory.OBJECT_MAPPER.createObjectNode();
    ArrayNode xdmDataValues = JacksonFactory.OBJECT_MAPPER.createArrayNode();
    xdmDataValues.add(JacksonFactory.OBJECT_MAPPER.readTree(xdmData));

    messageNode.set("messages", xdmDataValues);
    return JacksonFactory.OBJECT_MAPPER.writeValueAsString(messageNode);
  }

  public String xdmData() throws HttpException {
    return HttpUtil.streamToString(this.getClass().getClassLoader().getResourceAsStream(XDM_PAYLOAD_FILE));
  }

  public Map<String, String> connectorConfig(String configFile) throws HttpException, JsonProcessingException {
    String connectorProperties = String.format(HttpUtil.streamToString(this.getClass().getClassLoader()
      .getResourceAsStream(configFile)),
      NUMBER_OF_TASKS,
      getInletUrl());

    Map<String, String> connectorConfig = JacksonFactory.OBJECT_MAPPER.readValue(connectorProperties, MAP_TYPE_JSON);
    connectorConfig.put("name", CONNECTOR_NAME);
    return connectorConfig;
  }
}
