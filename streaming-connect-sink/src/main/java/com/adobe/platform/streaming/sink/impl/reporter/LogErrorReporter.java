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

import com.adobe.platform.streaming.http.HttpException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Adobe Inc.
 */
public class LogErrorReporter implements ErrorReporter<List<Future<Boolean>>> {

  private final Logger logger;

  public LogErrorReporter(Class<?> reporterClass) {
    this.logger = LoggerFactory.getLogger(reporterClass);
  }

  public LogErrorReporter(String reporterName) {
    this.logger = LoggerFactory.getLogger(reporterName);
  }

  @Override
  public List<Future<Boolean>> report(List<?> records, Exception error) {
    String errorId = UUID.randomUUID().toString();
    logger.error("Number of records failed for errorId {} - {}", errorId, records.size());
    final Map<String, String> errorContext;
    if (error instanceof HttpException) {
      HttpException httpException = (HttpException) error;
      errorContext = getErrorContext(errorId,
        httpException.getMessage(),
        String.valueOf(httpException.getResponseCode()));
    } else {
      errorContext = getErrorContext(errorId, error.getMessage(), null);
    }

    records.forEach(record -> logger.error("Error id {} : Payload {}", errorId,
      errorMessage(record, errorContext)));

    return Collections.singletonList(CompletableFuture.completedFuture(true));
  }
}
