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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Test
    void testRecordPayloadThrowsException() {
      SinkRecord r = new SinkRecord("test", 0, null, null, SchemaBuilder.struct().build(), )
      SinkUtils.getStringPayload(objectMapper, )
    }
  }
}

