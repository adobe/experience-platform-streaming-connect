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

package com.adobe.platform.streaming.sink;

import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * @author Adobe Inc.
 */
public abstract class AbstractSinkTask extends SinkTask {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractSinkConnector.class);

  protected DataPublisher publisher;

  @Override
  public String version() {
    return AppInfoParser.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    LOG.info("Started Sink Task with props: {}", props);
  }

  @Override
  public void stop() {
    LOG.info("Stopped Sink Task");
  }

  @Override
  public void put(Collection<SinkRecord> records) {
    for (SinkRecord record: records) {
      publisher.sendData(record);
    }
  }

}
