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
    publisher.stop();
  }

  @Override
  public void put(Collection<SinkRecord> records) {
    for (SinkRecord record: records) {
      publisher.sendData(record);
    }
  }

}
