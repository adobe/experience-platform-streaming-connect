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

import com.google.common.collect.ImmutableMap;
import kafka.server.ConfigType;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Adobe Inc.
 */
public abstract class AbstractSinkConnector extends SinkConnector {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractSinkConnector.class);

  private Map<String, String> connectorProps;

  @Override
  public String version() {
    return AppInfoParser.getVersion();
  }

  @Override
  public void start(Map<String, String> props) {
    connectorProps = props;
    LOG.info("Started Sink Connector with props {}", connectorProps);
  }

  @Override
  public List<Map<String, String>> taskConfigs(int maxTasks) {
    List<Map<String, String>> configs = new ArrayList<>();
    for (int i = 0; i < maxTasks; i++) {
      configs.add(new ImmutableMap.Builder<String, String>().putAll(connectorProps).build());

    }

    return configs;
  }

  @Override
  public void stop() {
    LOG.info("Stopped Sink Connector");
  }

  @Override
  public ConfigDef config() {



    ConfigDef configDef = new ConfigDef();
    configDef.define("aep.connection.auth.keyValue", ConfigDef.Type.PASSWORD,null,null,"");
    return new ConfigDef();
  }

}
