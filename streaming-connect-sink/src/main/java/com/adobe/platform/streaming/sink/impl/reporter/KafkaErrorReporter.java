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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Adobe Inc.
 */
public class KafkaErrorReporter implements ErrorReporter<List<Future<RecordMetadata>>> {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaErrorReporter.class);
  private static final int REQUEST_TIMEOUT_MS_CONFIG = 15000;
  private final KafkaProducer<byte[], byte[]> errorProducer;
  private final String errorTopic;

  public KafkaErrorReporter(String bootstrapServers, String errorTopic) {
    this.errorProducer = new KafkaProducer<>(kafkaProperties(bootstrapServers));
    this.errorTopic = errorTopic;
  }

  public Map<String, Object> kafkaProperties(String bootstrapServers) {
    Map<String, Object> producerProps = new HashMap<>();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.ByteArraySerializer");
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.ByteArraySerializer");
    producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Integer.toString(REQUEST_TIMEOUT_MS_CONFIG));
    producerProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Long.toString(REQUEST_TIMEOUT_MS_CONFIG));
    producerProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, Integer.toString(REQUEST_TIMEOUT_MS_CONFIG));
    producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, "aep-client-id");

    return producerProps;
  }

  @Override
  public List<Future<RecordMetadata>> report(List<?> records, Exception error) {
    String errorId = UUID.randomUUID().toString();
    final Map<String, String> errorContext;
    if (error instanceof HttpException) {
      HttpException httpException = (HttpException) error;
      errorContext = getErrorContext(errorId,
        httpException.getMessage(),
        String.valueOf(httpException.getResponseCode()));
    } else {
      errorContext = getErrorContext(errorId, error.getMessage(), null);
    }

    List<Future<RecordMetadata>> recordSendFuture = new ArrayList<>();

    records.forEach(record -> {
      ProducerRecord<byte[], byte[]> errorRecord = new ProducerRecord<>(errorTopic,
        null,
        null,
        null,
        errorMessage(record, errorContext).toString().getBytes(StandardCharsets.UTF_8),
        new RecordHeaders(errorContext.entrySet().stream()
        .map(errorMessage -> new RecordHeader(errorMessage.getKey(),
        errorMessage.getValue().getBytes(StandardCharsets.UTF_8)))
        .collect(Collectors.toList())));

      recordSendFuture.add(errorProducer.send(errorRecord, (metadata, exception) ->
        LOG.error("Failed to send error records to topic {}, Record value : {}", metadata.topic(),
        errorRecord, exception)));
    });

    return recordSendFuture;
  }
}
