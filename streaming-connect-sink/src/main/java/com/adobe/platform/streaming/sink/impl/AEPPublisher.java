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

package com.adobe.platform.streaming.sink.impl;

import com.adobe.platform.streaming.AEPStreamingException;
import com.adobe.platform.streaming.JacksonFactory;
import com.adobe.platform.streaming.http.ContentHandler;
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpProducer;
import com.adobe.platform.streaming.http.HttpUtil;
import com.adobe.platform.streaming.sink.AbstractAEPPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Adobe Inc.
 */
public class AEPPublisher extends AbstractAEPPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(AEPPublisher.class);

  private static final String MESSAGES_KEY = "messages";
  private static final String RESPONSES_KEY = "responses";
  private static final String STATUS_KEY = "status";
  private static final String XACTIONID_KEY = "xactionId";
  private static final String DASH = "-";

  private int count;
  private final HttpProducer producer;
  private final ErrantRecordReporter errorReporter;

  AEPPublisher(Map<String, String> props, ErrantRecordReporter errantRecordReporter) throws AEPStreamingException {
    count = 0;
    producer = getHttpProducer(props);
    errorReporter = errantRecordReporter;
  }

  @Override
  public void publishData(List<Pair<String, SinkRecord>> messages) throws AEPStreamingException {
    if (CollectionUtils.isEmpty(messages)) {
      LOG.debug("No messages to publish");
      return;
    }
    int failedMessageCount = 0;
    int successMessageCount = 0;
    int totalMessageCount;

    final ArrayNode jsonMessages = JacksonFactory.OBJECT_MAPPER.createArrayNode();

    try {
      messages.stream()
          .map(Pair::getKey)
          .map(key -> {
            try {
              return JacksonFactory.OBJECT_MAPPER.readTree(key);
            } catch (JsonProcessingException e) {
              LOG.debug("Found invalid JSON record in messages: {}", key);
              return null;
            }
          })
          .filter(Objects::nonNull)
          .forEach(jsonMessages::add);

      final JsonNode payload = JacksonFactory.OBJECT_MAPPER.createObjectNode()
        .set(MESSAGES_KEY, jsonMessages);

      totalMessageCount = jsonMessages.size();

      final JsonNode response = producer.post(
        StringUtils.EMPTY,
        JacksonFactory.OBJECT_MAPPER.writeValueAsBytes(payload),
        ContentHandler.jsonHandler()
      );

      count++;

      if (!response.isNull() && !response.isEmpty()) {
        JsonNode publishMessagesResponses = response.get(RESPONSES_KEY);
        for (JsonNode messageResponse : publishMessagesResponses) {
          if (messageResponse.hasNonNull(STATUS_KEY)) {
            failedMessageCount++;
            final Pair<String, SinkRecord> failedMessage = messages.get(getFailedMessageIndex(messageResponse));
            LOG.debug("Failed to publish message: {} to Adobe Experience Platform due to the error: {}",
              failedMessage, messageResponse);
            if (Objects.nonNull(errorReporter)) {
              final int responseCode = messageResponse.get(STATUS_KEY).asInt();
              errorReporter.report(failedMessage.getRight(),
                new HttpException(String.format("error response= %s", messageResponse), responseCode));
            }
          } else {
            successMessageCount++;
          }
        }
        LOG.debug("Total publish message count = {}. Success message count = {}. Failed " +
          "message count = {}.", totalMessageCount, successMessageCount, failedMessageCount);
      } else {
        LOG.error("Invalid Response received while publishing data to  Adobe Experience Platform: {}", response);
      }
    } catch (JsonProcessingException jsonException) {
      LOG.error("Failed to publish data to Adobe Experience Platform", jsonException);
      if (Objects.nonNull(errorReporter)) {
        messages.forEach(message -> errorReporter.report(message.getValue(), jsonException));
      } else {
        throw new AEPStreamingException("Failed to publish invalid JSON", jsonException);
      }
    } catch (HttpException httpException) {
      LOG.error("Failed to publish data to Adobe Experience Platform", httpException);
      
      final int responseCode = httpException.getResponseCode();
      if (Objects.nonNull(errorReporter)) {
        messages.forEach(message -> errorReporter.report(message.getValue(), httpException));
      } else {
        if (HttpUtil.is500(responseCode)) {
          throw new AEPStreamingException("Failed to publish", httpException);
        }
      }

      if (HttpUtil.isUnauthorized(responseCode)) {
        throw new AEPStreamingException("Failed to publish", httpException);
      }
    }
  }

  private Integer getFailedMessageIndex(final JsonNode messageResponse) throws HttpException {
    if (messageResponse.hasNonNull(XACTIONID_KEY)) {
      final String xactionId = messageResponse.get(XACTIONID_KEY).asText();
      return Integer.parseInt(xactionId.substring(xactionId.lastIndexOf(DASH) + 1));
    }
    throw new HttpException(String.format("xactionId is missing in the failed message error response : %s",
      messageResponse));
  }

  public void stop() {
    LOG.info("Stopping AEP Data Publisher after publishing {} messages", count);
  }

}
