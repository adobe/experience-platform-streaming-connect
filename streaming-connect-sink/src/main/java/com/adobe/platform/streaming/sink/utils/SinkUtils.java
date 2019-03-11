/*
 *  ADOBE CONFIDENTIAL
 *  __________________
 *
 *  Copyright 2019 Adobe
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe.
 */

package com.adobe.platform.streaming.sink.utils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
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

}
