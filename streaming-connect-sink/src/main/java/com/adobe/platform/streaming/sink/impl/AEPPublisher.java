/*
 *  ADOBE CONFIDENTIAL
 *  __________________
 *
 *  Copyright 2019 Adobe
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe.
 */

package com.adobe.platform.streaming.sink.impl;

import com.adobe.platform.streaming.http.ContentHandler;
import com.adobe.platform.streaming.http.HttpException;
import com.adobe.platform.streaming.http.HttpProducer;
import com.adobe.platform.streaming.sink.AbstractAEPPublisher;
import com.adobe.platform.streaming.sink.utils.SinkUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.kafka.connect.sink.SinkRecord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Adobe Inc.
 */
class AEPPublisher extends AbstractAEPPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(AEPPublisher.class);

  private int count;
  private final HttpProducer producer;
  private final Gson gson;

  AEPPublisher(Map<String, String> props) {
    count = 0;
    gson = new GsonBuilder().create();
    producer = getHttpProducer(props);
  }

  @Override
  public void start() {
    LOG.info("Starting AEP publisher");
  }

  @Override
  public void sendData(SinkRecord record) {
    try {
      JSONObject response = producer.post(
        StringUtils.EMPTY,
        SinkUtils.getBytePayload(gson, record),
        ContentType.APPLICATION_JSON.getMimeType(),
        ContentHandler.jsonHandler()
      );

      count++;
      LOG.debug("Successfully published data to Adobe Experience Platform: {}", response);
    } catch (HttpException httpException) {
      LOG.error("Failed to publish data to Adobe Experience Platform", httpException);
    }
  }

  public void stop() {
    LOG.info("Stopping AEP Data Publisher after publishing {} messages", count);
  }

}
