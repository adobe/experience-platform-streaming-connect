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

import com.adobe.platform.streaming.sink.AbstractAEPPublisher;
import com.adobe.platform.streaming.sink.utils.SinkUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.runtime.WorkerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Adobe Inc.
 */
public class CompositeErrorReporter implements ErrorReporter<List<Future<?>>> {

  private static final Logger LOG = LoggerFactory.getLogger(CompositeErrorReporter.class);
  private final List<ErrorReporter<?>> errorReporters;

  public CompositeErrorReporter(Map<String, String> properties) {
    this.errorReporters = getErrorReporter(properties);
  }

  private List<ErrorReporter<?>> getErrorReporter(Map<String, String> properties) {
    String reporterType = SinkUtils.getProperty(properties, AbstractAEPPublisher.AEP_ERROR_LOGGER, "NONE");
    Reporter reporter = Reporter.valueOf(reporterType.toUpperCase());

    switch (reporter) {
      case KAFKA:
        return getKafkaReporter(properties.get(WorkerConfig.BOOTSTRAP_SERVERS_CONFIG),
          properties.get(AbstractAEPPublisher.AEP_ERROR_TOPIC));
      case LOG:
        return getLogErrorReporter();
      case BOTH:
        List<ErrorReporter<?>> errorReporters = new ArrayList<>(getKafkaReporter(
          properties.get(WorkerConfig.BOOTSTRAP_SERVERS_CONFIG),
          properties.get(AbstractAEPPublisher.AEP_ERROR_TOPIC)));
        errorReporters.addAll(getLogErrorReporter());
        return errorReporters;
      case NONE:
      default:
        return Collections.emptyList();
    }
  }

  private List<ErrorReporter<?>> getKafkaReporter(String bootstrapServers, String topic) {
    if (StringUtils.isBlank(bootstrapServers) || StringUtils.isBlank(topic)) {
      LOG.error("Bootstrap server or topic for error record reporting is empty");
      return Collections.emptyList();
    }
    return Collections.singletonList(new KafkaErrorReporter(bootstrapServers, topic));
  }

  private List<ErrorReporter<?>> getLogErrorReporter() {
    return Collections.singletonList(new LogErrorReporter("AEPErrorReporter"));
  }

  @Override
  public List<Future<?>> report(List<?> records, Exception exception) {
    return errorReporters.stream().flatMap(errorReporter -> errorReporter.report(records, exception).stream())
        .collect(Collectors.toList());
  }
}
