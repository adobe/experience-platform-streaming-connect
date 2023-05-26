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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.Map;

/**
 * @author Adobe Inc.
 */
public class SinkUtils {

  public static String getStringPayload(ObjectMapper objectMapper, SinkRecord record) throws JsonProcessingException {
    final Object value = record.value();
    if (value instanceof String) {
      return (String) value;
    }
    final Schema valueSchema = record.valueSchema();
    if (valueSchema == null) {
      return objectMapper.writeValueAsString(value);
    }
    if (valueSchema.equals(Schema.STRING_SCHEMA)) {
        return value.toString();
    } else {
      throw new ConnectException("Value must be a plain-string or value schema must be non-optional string-type");
    }
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

  public static String getProperty(Map<String, String> props, String propertyName, String defaultValue) {
    return props != null ? (StringUtils.isNotBlank(props.get(propertyName)) ? props.get(propertyName) : defaultValue)
      : defaultValue;
  }

  public static int getProperty(Map<String, String> properties, String key, int defaultValue, int multiplier) {
    int propertyValue = getProperty(properties, key, defaultValue) * multiplier;
    if (propertyValue < 1) {
      return defaultValue * multiplier;
    }

    return propertyValue;
  }

  private SinkUtils() {
  }

}
