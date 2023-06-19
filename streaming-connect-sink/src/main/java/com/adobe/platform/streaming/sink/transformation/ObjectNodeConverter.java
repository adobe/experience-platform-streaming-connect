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

package com.adobe.platform.streaming.sink.transformation;

import com.adobe.platform.streaming.AEPStreamingRuntimeException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author Adobe Inc.
 */
public class ObjectNodeConverter {

  private static final Logger LOG = LoggerFactory.getLogger(ObjectNodeConverter.class);
  private final ObjectMapper objectMapper;

  public ObjectNodeConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public ObjectNode convert(Object from) {
    if (Objects.isNull(from)) {
      return null;
    }
    ObjectNode convertedVal = null;
    try {
      if (from instanceof String) {
        convertedVal = (ObjectNode) objectMapper.readTree(from.toString());
      } else if (from instanceof byte[]) {
        convertedVal = (ObjectNode) objectMapper.readTree((byte[]) from);
      } else if (from instanceof Map) {
        convertedVal = objectMapper.convertValue(from, ObjectNode.class);
      } else if (from instanceof ObjectNode) {
        return (ObjectNode) from;
      } else {
        throw new AEPStreamingRuntimeException("Unsupported type for conversion to objectNode");
      }
    } catch (IOException e) {
      LOG.error("Exception occurred while de-serializing to ObjectNode", e);
    }
    return convertedVal;
  }

}
