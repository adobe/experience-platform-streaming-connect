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

package com.adobe.platform.streaming.sink;

import com.adobe.platform.streaming.AEPStreamingException;
import com.adobe.platform.streaming.sink.utils.SinkUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Adobe Inc.
 */
public abstract class AbstractSinkTask<T> extends SinkTask {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractSinkConnector.class);

  private static final String FLUSH_INTERVAL_SECS = "aep.flush.interval.seconds";
  private static final String FLUSH_BYTES_KB = "aep.flush.bytes.kb";
  private static final int DEFAULT_FLUSH_INTERVAL = 1;
  private static final int DEFAULT_FLUSH_BYTES_KB = 4;
  private static final int MILLIS_IN_A_SEC = 1000;
  private static final int BYTES_IN_A_KB = 1024;

  private Gson gson;
  private int bytesRead = 0;
  private int flushIntervalMillis;
  private int flushBytesCount;
  private long lastFlushMilliSec = System.currentTimeMillis();

  @Override
  public String version() {
    return AppInfoParser.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    LOG.info("Started Sink Task with props: {}", props);

    try {
      gson = new GsonBuilder().create();

      flushIntervalMillis = SinkUtils.getProperty(props, FLUSH_INTERVAL_SECS, DEFAULT_FLUSH_INTERVAL, MILLIS_IN_A_SEC);
      flushBytesCount = SinkUtils.getProperty(props, FLUSH_BYTES_KB, DEFAULT_FLUSH_BYTES_KB, BYTES_IN_A_KB);

      init(props);
      LOG.info("Connection created with flush interval {} secs and flush bytes {} KB",
        flushIntervalMillis / MILLIS_IN_A_SEC, flushBytesCount / BYTES_IN_A_KB);
    } catch (AEPStreamingException aepStreamingException) {
      LOG.error("ConnectorSinkTask: Exception while creating connection {}", aepStreamingException);
    }
  }

  @Override
  public void stop() {
    LOG.info("Stopped Sink Task");
  }

  @Override
  public void put(Collection<SinkRecord> records) {
    if (CollectionUtils.isEmpty(records)) {
      return;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("ConnectorSinkTask: {} sink records received", records.size());
    }

    List<T> eventsToPublish = new ArrayList<>();
    for (SinkRecord record : records) {
      T dataToPublish = getDataToPublish(SinkUtils.getStringPayload(gson, record));
      eventsToPublish.add(dataToPublish);
      bytesRead += getPayloadLength(dataToPublish);

      long tempCurrTime = System.currentTimeMillis();
      if (flushNow(tempCurrTime)) {
        publishAndLogIfRequired(eventsToPublish);
        if (LOG.isDebugEnabled()) {
          LOG.debug("ConnectorSinkTask: {} events flushed partially", eventsToPublish.size());
        }
        reset(eventsToPublish, tempCurrTime);
      }
    }

    if (!eventsToPublish.isEmpty()) {
      publishAndLogIfRequired(eventsToPublish);
      if (LOG.isDebugEnabled()) {
        LOG.debug("ConnectorSinkTask: {} events flushed finally", eventsToPublish.size());
      }
      reset(eventsToPublish, System.currentTimeMillis());
    }
  }

  public abstract void init(Map<String, String> properties) throws AEPStreamingException;

  public abstract T getDataToPublish(String sinkRecord);

  public abstract int getPayloadLength(T dataToPublish);

  public abstract void publishData(List<T> eventsToPublish);

  private boolean flushNow(long tempCurrTime) {
    return tempCurrTime >= lastFlushMilliSec + flushIntervalMillis || bytesRead >= flushBytesCount;
  }

  private void publishAndLogIfRequired(List<T> eventsToPublish) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("ConnectorSinkTask: {} events sent to destination", eventsToPublish.size());
    }
    publishData(eventsToPublish);
  }

  private void reset(List<T> eventsToPublish, long tempCurrentTime) {
    lastFlushMilliSec = tempCurrentTime;
    bytesRead = 0;
    eventsToPublish.clear();
  }
}
