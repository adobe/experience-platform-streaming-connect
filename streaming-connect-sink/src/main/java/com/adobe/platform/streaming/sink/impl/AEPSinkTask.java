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
import com.adobe.platform.streaming.sink.AbstractSinkTask;
import com.adobe.platform.streaming.sink.DataPublisher;

import java.util.List;
import java.util.Map;

/**
 * @author Adobe Inc.
 */
public class AEPSinkTask extends AbstractSinkTask<String> {

  private DataPublisher publisher;

  @Override
  public void init(Map<String, String> props) throws AEPStreamingException {
    publisher = new AEPPublisher(props);
    publisher.start();
  }

  @Override
  public String getDataToPublish(String sinkRecord) {
    return sinkRecord;
  }

  @Override
  public int getPayloadLength(String dataToPublish) {
    return dataToPublish.length();
  }

  @Override
  public void publishData(List<String> eventDataList) {
    publisher.publishData(eventDataList);
  }

  @Override
  public void stop() {
    super.stop();
    publisher.stop();
  }

}
