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
import com.adobe.platform.streaming.sink.DataPublisher;
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
class DCSPublisher implements DataPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(DCSPublisher.class);
  private static final String DCS_ENDPOINT = "dcs.endpoint";
  private static final String DCS_CONNECTION_TIMEOUT = "dcs.connection.timeout";
  private static final String DCS_CONNECTION_MAX_RETRIES = "dcs.connection.maxRetries";
  private static final String DCS_CONNECTION_MAX_RETRIES_BACKOFF = "dcs.connection.retryBackoff";
  private static final String DCS_CONNECTION_READ_TIMEOUT = "dcs.connection.readTimeout";

  private final HttpProducer producer;
  private final Gson gson;

  DCSPublisher(Map<String, String> props) {
    gson = new GsonBuilder().create();
    producer = HttpProducer.newBuilder(props.get(DCS_ENDPOINT))
      .withConnectTimeout(SinkUtils.getProperty(props, DCS_CONNECTION_TIMEOUT, 5000))
      .withReadTimeout(SinkUtils.getProperty(props, DCS_CONNECTION_READ_TIMEOUT, 60000))
      .withMaxRetries(SinkUtils.getProperty(props, DCS_CONNECTION_MAX_RETRIES, 3))
      .withRetryBackoff(SinkUtils.getProperty(props, DCS_CONNECTION_MAX_RETRIES_BACKOFF, 300))
      .build();
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

      LOG.debug("Successfully published data to DCS: {}", response);
    } catch (HttpException httpException) {
      LOG.error("Failed to publish data to DCS", httpException);
    }
  }

}
