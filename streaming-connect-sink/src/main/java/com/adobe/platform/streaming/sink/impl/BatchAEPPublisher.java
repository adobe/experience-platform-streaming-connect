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
import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.kafka.connect.sink.SinkRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Adobe Inc.
 */
public class BatchAEPPublisher extends AbstractAEPPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(BatchAEPPublisher.class);
  private static final String MESSAGES_KEY = "messages";
  private static final String AEP_BATCH_SIZE = "aep.batch.size";
  private static final String AEP_BATCH_INITIAL_DELAY = "aep.batch.initial.delay";
  private static final String AEP_BATCH_SCHEDULE_PERIOD = "aep.batch.schedule.period";

  private Queue<SinkRecord> recordsToPublish = new ArrayDeque<>();
  private final ScheduledExecutorService executorService;
  private final HttpProducer producer;
  private final int batchSize;
  private final int initialDelay;
  private final int schedulingPreiod;

  private int count = 0;
  private int batchesPublished = 0;

  BatchAEPPublisher(Map<String, String> props) {
    int batchSize = SinkUtils.getProperty(props, AEP_BATCH_SIZE, 2);
    Preconditions.checkArgument(batchSize > 1, "Batch size should be greater that 1");
    this.batchSize = batchSize;
    initialDelay = SinkUtils.getProperty(props, AEP_BATCH_INITIAL_DELAY, 500);
    schedulingPreiod = SinkUtils.getProperty(props, AEP_BATCH_SCHEDULE_PERIOD, 1000);

    executorService = Executors.newSingleThreadScheduledExecutor();
    producer = getHttpProducer(props);
  }

  @Override
  public void start() {
    LOG.info("Starting AEP Batch publisher with batch size {}", batchSize);
    executorService.scheduleWithFixedDelay(this::publishBatch, initialDelay, schedulingPreiod, TimeUnit.MILLISECONDS);
  }

  @Override
  public void sendData(SinkRecord record) {
    LOG.debug("Received record to publish");
    count++;
    recordsToPublish.add(record);
  }

  @Override
  public void stop() {
    LOG.info("Stopping AEP Data Publisher after publishing {} messages in {} batches", count, batchesPublished);
    executorService.shutdown();
  }

  private void publishBatch() {
    try {
      List<SinkRecord> records = getBatch();

      if (CollectionUtils.isEmpty(records)) {
        LOG.debug("No data to publish");
        return;
      }

      JSONArray messages = new JSONArray();
      records.stream().map(JSONObject::new).forEach(messages::put);
      JSONObject payload = new JSONObject();
      payload.put(MESSAGES_KEY, messages);

      JSONObject response = producer.post(
        StringUtils.EMPTY,
        payload.toString().getBytes(),
        ContentType.APPLICATION_JSON.getMimeType(),
        ContentHandler.jsonHandler()
      );

      batchesPublished++;
      LOG.debug("Successfully published data to Adobe Experience Platform: {}", response);
    } catch (JSONException | HttpException exception) {
      LOG.error("Failed to publish data to Adobe Experience Platform", exception);
    }

  }

  private synchronized List<SinkRecord> getBatch() {
    if (recordsToPublish.size() >= batchSize) {
      List<SinkRecord> records = new ArrayList<>();
      while (records.size() < batchSize) {
        records.add(recordsToPublish.remove());
      }

      return records;
    }

    return null;
  }
}
