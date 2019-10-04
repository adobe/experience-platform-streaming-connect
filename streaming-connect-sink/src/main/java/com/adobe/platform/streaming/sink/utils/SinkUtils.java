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

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.sink.SinkRecord;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adobe Inc.
 */
public class SinkUtils {

  private static final String KAFKA_MESSAGE_KEY_ATTRIBUTE = "key";
  private static final String KAFKA_TOPIC_ATTRIBUTE = "kafka.topic";
  private static final String KAFKA_PARTITION_ATTRIBUTE = "kafka.partition";
  private static final String KAFKA_OFFSET_ATTRIBUTE = "kafka.offset";
  private static final String KAFKA_TIMESTAMP_ATTRIBUTE = "kafka.timestamp";

  public static Map<String, String> getRecordMetadata(SinkRecord record) {

    Map<String, String> attributes = new HashMap<>();
    if (record.key() != null) {
      String key = record.key().toString();
      attributes.put(KAFKA_MESSAGE_KEY_ATTRIBUTE, key);
    }
    attributes.put(KAFKA_TOPIC_ATTRIBUTE, record.topic());
    attributes.put(KAFKA_PARTITION_ATTRIBUTE, record.kafkaPartition().toString());
    attributes.put(KAFKA_OFFSET_ATTRIBUTE, Long.toString(record.kafkaOffset()));
    attributes.put(KAFKA_TIMESTAMP_ATTRIBUTE, record.timestamp().toString());

    return attributes;
  }

  public static int getProperty(Map<String, String> props, String propertyName, int defaultValue) {
    if (props != null) {
      String propertyValue = props.get(propertyName);
      if (StringUtils.isNotBlank(propertyValue)) {
        return Integer.parseInt(propertyValue);
      }
    }

    return defaultValue;
  }

  public static byte[] getBytePayload(Gson gson, SinkRecord record) {
    if (record.value() instanceof String) {
      return ((String) record.value()).getBytes();
    }

    return gson.toJson(record.value()).getBytes(StandardCharsets.UTF_8);
  }

  private SinkUtils() {}

}
