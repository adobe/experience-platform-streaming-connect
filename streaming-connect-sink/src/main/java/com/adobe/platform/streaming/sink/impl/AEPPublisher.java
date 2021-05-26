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
import com.adobe.platform.streaming.http.ContentHandler;
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpProducer;
import com.adobe.platform.streaming.sink.AbstractAEPPublisher;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Adobe Inc.
 */
class AEPPublisher extends AbstractAEPPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(AEPPublisher.class);
  private static final String MESSAGES_KEY = "messages";

  private int count;
  private final HttpProducer producer;

  AEPPublisher(Map<String, String> props) throws AEPStreamingException {
    count = 0;
    producer = getHttpProducer(props);
  }

  @Override
  public void publishData(List<String> messages) throws AEPStreamingException {
    if (CollectionUtils.isEmpty(messages)) {
      LOG.debug("No messages to publish");
      return;
    }

    try {
      JSONArray jsonMessages = new JSONArray();
      messages.stream().map(JSONObject::new).forEach(jsonMessages::put);
      JSONObject payload = new JSONObject();
      payload.put(MESSAGES_KEY, jsonMessages);

      JSONObject response = producer.post(
        StringUtils.EMPTY,
        payload.toString().getBytes(),
        ContentType.APPLICATION_JSON.getMimeType(),
        ContentHandler.jsonHandler()
      );

      count++;
      LOG.debug("Successfully published data to Adobe Experience Platform: {}", response);
    } catch (HttpException httpException) {
      LOG.error("Failed to publish data to Adobe Experience Platform", httpException);
      throw new AEPStreamingException("Failed to publish", httpException);
    }
  }

  public void stop() {
    LOG.info("Stopping AEP Data Publisher after publishing {} messages", count);
  }

}
