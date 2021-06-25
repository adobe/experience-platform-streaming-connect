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

package com.adobe.platform.streaming.http.serializer;

import com.adobe.platform.streaming.AEPStreamingRuntimeException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Adobe Inc.
 */
public class SerializerDeserializerUtil {

  private static final Logger errorLogger = LoggerFactory.getLogger(SerializerDeserializerUtil.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static String serialize(Object value) {
    try {
      return MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      errorLogger.error("Failed to serialize value : {}", value);
      throw new AEPStreamingRuntimeException("Failed to serialize value", value);
    }
  }

  public static JsonNode convertToJsonNode(Object value) {
    return MAPPER.convertValue(value, JsonNode.class);
  }

  public static JsonNode convertStringToJsonNode(Object value) {
    try {
      return MAPPER.readTree(value.toString());
    } catch (IOException e) {
      errorLogger.error("Failed to serialize value : {}", value);
      throw new AEPStreamingRuntimeException("Failed to serialize value", value);
    }
  }

  public static ObjectNode createObjectNode() {
    return MAPPER.createObjectNode();
  }
}
