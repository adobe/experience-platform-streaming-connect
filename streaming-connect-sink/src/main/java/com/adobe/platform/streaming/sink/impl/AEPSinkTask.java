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

import com.adobe.platform.streaming.sink.AbstractSinkTask;

import java.util.Map;

/**
 * @author Adobe Inc.
 */
public class AEPSinkTask extends AbstractSinkTask {
  private static final String AEP_BATCH_ENABLED = "aep.batch.enabled";
  private static final String AEP_BATCH_DISABLED_VALUE = "false";
  private static final String AEP_BATCH_ENABLED_VALUE = "true";

  @Override
  public void start(Map<String, String> props) {
    super.start(props);
    publisher = props.getOrDefault(AEP_BATCH_ENABLED, AEP_BATCH_DISABLED_VALUE).equals(AEP_BATCH_ENABLED_VALUE) ?
      new BatchAEPPublisher(props) :
      new AEPPublisher(props);
    publisher.start();
  }

}
