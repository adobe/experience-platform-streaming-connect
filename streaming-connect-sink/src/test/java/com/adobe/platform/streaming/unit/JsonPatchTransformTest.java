/*
 * Copyright 2023 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adobe.platform.streaming.unit;

import com.adobe.platform.streaming.JacksonFactory;
import com.adobe.platform.streaming.sink.transformation.JsonPatchTransform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import mockit.Mocked;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.ConnectSchema;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.connect.data.Schema.Type.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Adobe Inc.
 */
public class JsonPatchTransformTest {

  @Mocked
  protected ConnectHeaders headers;

  private final String jsonPatchString = "[\n" +
    "  {\n" +
    "    \"op\": \"add\",\n" +
    "    \"path\": \"/header\",\n" +
    "    \"value\": {\"operations\":{\"data\":\"merge\", \"identity\":\"create\", " +
    "\"identityDatasetId\":\"2474b6c6c8bb6a1c72643ac8\"}}\n" +
    "  }\n" +
    "]";

  private String recordObjectString = "{\n" +
    "  \"payload\": {\n" +
    "    \"crmId\": \"VM-121-SHARE\",\n" +
    "    \"id\": 2388398\n" +
    "  }\n" +
    "}";

  private static JsonPatchTransform jsonPatchTransform;
  private static SourceRecord record;

  @BeforeEach
  public void initJsonPatchTransform() throws Exception {
    final Map<String, String> configs = new HashMap<>();
    configs.put("operations", jsonPatchString);

    jsonPatchTransform = new JsonPatchTransform();
    jsonPatchTransform.configure(configs);

    record = new SourceRecord(new HashMap<>(), new HashMap<>(), "test-topic", 1, new ConnectSchema(STRING),
                              "", new ConnectSchema(STRING), recordObjectString, 2L, headers);
  }

  @Test
  public void testJsonPatch() throws JsonProcessingException {
    final ConnectRecord transformedRecord = jsonPatchTransform.apply(record);
    final JsonNode recordValueNode = JacksonFactory.OBJECT_MAPPER
      .readTree(transformedRecord.value().toString());
    final JsonNode operations = recordValueNode.get("header").get("operations");
    final JsonNode dataNode = operations.get("data");
    final JsonNode identity = operations.get("identity");

    assertNotNull(operations);
    assertNotNull(dataNode);
    assertNotNull(identity);

    assertEquals("merge", dataNode.asText());
    assertEquals("create", identity.asText());

  }

}
