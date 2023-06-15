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
import com.adobe.platform.streaming.http.serializer.SerializerDeserializerUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.InvalidJsonPatchException;
import com.flipkart.zjsonpatch.JsonPatch;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author Adobe Inc.
 */
public class JsonPatchTransform<R extends ConnectRecord<R>> implements Transformation<R> {

  private static final Logger LOG = LoggerFactory.getLogger(JsonPatchTransform.class);
  private static final ObjectMapper MAPPER = SerializerDeserializerUtil.getMapper();
  private static final ObjectNodeConverter objectNodeConverter = new ObjectNodeConverter(MAPPER);
  private static final String JSON_PATCH_CONFIG = "operations";
  private static final String JSON_PATHS_DOC = "Json pointers to evaluate for filtering";
  private static final ConfigDef CONFIG_DEF = new ConfigDef().define(
    JSON_PATCH_CONFIG,
    ConfigDef.Type.STRING,
    ConfigDef.NO_DEFAULT_VALUE,
    ConfigDef.Importance.HIGH,
    JSON_PATHS_DOC
  );
  private JsonNode jsonPatch;

  @Override
  public R apply(R record) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received record in Patch Transformation {} ", record.value());
    }

    final Object value = record.value();
    if (Objects.isNull(value)) {
      return record;
    }
    try {
      final ObjectNode jsonValue = objectNodeConverter.convert(value);
      final JsonNode transformedValue = JsonPatch.apply(jsonPatch, jsonValue);
      Object out = MAPPER.writeValueAsString(transformedValue);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Transformed record {}", jsonValue);
      }
      return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(),
                              record.key(), record.valueSchema(), out, System.currentTimeMillis());
    } catch (InvalidJsonPatchException patchAppException) {
      LOG.error("Message validation failed.", patchAppException);
    } catch (Exception ex) {
      LOG.error("Record failed during transformation.", ex);
    }
    return record;
  }

  @Override
  public ConfigDef config() {
    return CONFIG_DEF;
  }

  @Override
  public void close() {

  }

  @Override
  public void configure(Map<String, ?> configs) {
    final SimpleConfig config = new SimpleConfig(CONFIG_DEF, configs);
    try {
      jsonPatch = MAPPER.readTree(config.getString(JSON_PATCH_CONFIG));
    } catch (JsonProcessingException e) {
      throw new AEPStreamingRuntimeException("Failed to read json patch config.", e);
    }
  }

}
