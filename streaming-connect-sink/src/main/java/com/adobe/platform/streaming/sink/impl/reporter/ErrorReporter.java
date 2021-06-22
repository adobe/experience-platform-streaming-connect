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

package com.adobe.platform.streaming.sink.impl.reporter;

import com.adobe.platform.streaming.http.serializer.SerializerDeserializerUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Adobe Inc.
 */
public interface ErrorReporter<T extends List<? extends Future<?>>> {

  String ERROR_ID = "errorId";
  String ERROR_CODE = "errorCode";
  String ERROR_REASON = "errorMessage";

  default Map<String, String> getErrorContext(ErrorPair... errorPairs) {
    return Arrays.stream(errorPairs)
        .filter(errorPair -> Objects.nonNull(errorPair.getName()))
        .filter(errorPair -> Objects.nonNull(errorPair.getValue()))
        .collect(Collectors.toMap(ErrorPair::getName, ErrorPair::getValue));
  }

  default Map<String, String> getErrorContext(String errorId, String errorMessage, String errorCode) {
    return getErrorContext(new ErrorPair().setName(ERROR_ID).setValue(errorId),
      new ErrorPair().setName(ERROR_CODE).setValue(errorCode),
      new ErrorPair().setName(ERROR_REASON).setValue(errorMessage));
  }

  default JsonNode errorMessage(Object payload, Map<String, String> errorContext) {
    ObjectNode errorNode = SerializerDeserializerUtil.createObjectNode();

    errorNode.set("payload", SerializerDeserializerUtil.convertStringToJsonNode(payload));
    errorNode.set("errorContext", SerializerDeserializerUtil.convertToJsonNode(errorContext));

    return errorNode;
  }

  T report(List<?> records, Exception exception);

  /**
   * @author Adobe Inc.
   */
  class ErrorPair {

    String name;
    String value;

    public String getName() {
      return name;
    }

    public ErrorPair setName(String name) {
      this.name = name;
      return this;
    }

    public String getValue() {
      return value;
    }

    public ErrorPair setValue(String value) {
      this.value = value;
      return this;
    }
  }
}
