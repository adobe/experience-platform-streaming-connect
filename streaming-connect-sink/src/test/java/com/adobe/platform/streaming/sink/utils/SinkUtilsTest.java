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

package com.adobe.platform.streaming.sink.utils;

import com.adobe.platform.streaming.JacksonFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.storage.ConverterConfig;
import org.apache.kafka.connect.storage.ConverterType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Adobe Inc.
 */
@Tag("unit")
class SinkUtilsTest {

  private static final String TEST_KEY = "key";
  private static final int TEST_MULTIPLIER = 1000;
  private static final int TEST_VALUE = 3;
  private static final int TEST_DEFAULT_VALUE = 2;

  @Test
  void testGetPropertyMissingPropertyWithDefault() {
    assertEquals(TEST_DEFAULT_VALUE, SinkUtils.getProperty(new HashMap<>(), TEST_KEY, TEST_DEFAULT_VALUE));
  }

  @Test
  void testGetProperty() {
    assertEquals(TEST_VALUE, SinkUtils.getProperty(
      ImmutableMap.of(TEST_KEY, String.valueOf(TEST_VALUE)),
      TEST_KEY,
      TEST_DEFAULT_VALUE
    ));
  }

  @Test
  void testGetPropertyMissingPropertyWithMultiplier() {
    assertEquals(2000, SinkUtils.getProperty(new HashMap<>(), TEST_KEY, TEST_DEFAULT_VALUE, TEST_MULTIPLIER));
  }

  @Test
  void testGetPropertyWithMultiplier() {
    assertEquals(3000, SinkUtils.getProperty(
      ImmutableMap.of(TEST_KEY, String.valueOf(TEST_VALUE)),
      TEST_KEY,
      TEST_DEFAULT_VALUE,
      TEST_MULTIPLIER
    ));
  }

  static class PayloadTest {

    private static JsonConverter jsonConverter;

    @BeforeAll
    static void setup() {
      jsonConverter = new JsonConverter();
      final Map<String, Object> configs = new HashMap<>();
      configs.put(JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, false);
      configs.put(ConverterConfig.TYPE_CONFIG, ConverterType.VALUE.getName());

      jsonConverter.configure(configs);
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource({"primitiveSchemaPayloads", "objectSchemaPayloads"})
    void testPrimitiveValueSchemaAsJson(Schema valueSchema, Object value, String expectedJson) {
      SinkRecord sr = getSinkRecordWithValue(valueSchema, value);
      assertDoesNotThrow(() -> assertEquals(expectedJson, SinkUtils.getStringPayload(jsonConverter, sr)));
    }

    static Stream<Object[]> primitiveSchemaPayloads() throws JsonProcessingException {
      byte[] bytes = "secret".getBytes(StandardCharsets.UTF_8);
      return Stream.of(
          new Object[]{Schema.STRING_SCHEMA, "test", "test"},
          new Object[]{SchemaBuilder.string().defaultValue("defaultValue").build(), null, "\"defaultValue\""},
          new Object[]{Schema.OPTIONAL_STRING_SCHEMA, null, "null"},
          // preserves boolean JSON types
          new Object[]{Schema.BOOLEAN_SCHEMA, Boolean.TRUE, "true"},
          new Object[]{Schema.OPTIONAL_BOOLEAN_SCHEMA, null, "null"},
          // preserves byte types
          new Object[]{Schema.BYTES_SCHEMA, bytes, JacksonFactory.OBJECT_MAPPER.writeValueAsString(bytes)},
          new Object[]{Schema.OPTIONAL_BYTES_SCHEMA, null, "null"}
      );
    }

    static Stream<Object[]> objectSchemaPayloads() {
      final Schema nameValueStructSchema = SchemaBuilder.struct()
        .name("KeyValuePair")
        .field("name", Schema.STRING_SCHEMA)
        .field("value", Schema.OPTIONAL_STRING_SCHEMA)
        .build();
      // NOTE: expected, written JSON is compact
      return Stream.of(
          // array schema values must be Collection subclasses
          new Object[]{SchemaBuilder.array(Schema.INT32_SCHEMA).build(), Arrays.asList(1, 2, 3), "[1,2,3]"},
          new Object[]{nameValueStructSchema,
                       new Struct(nameValueStructSchema)
                           .put("name", "hello")
                           .put("value", null),
                       "{\"name\":\"hello\",\"value\":null}"}
      );
    }

    private SinkRecord getSinkRecordWithValue(Schema valueSchema, Object value) {
      return new SinkRecord("test", 0, null, null, valueSchema, value, 0L);
    }
  }
}

