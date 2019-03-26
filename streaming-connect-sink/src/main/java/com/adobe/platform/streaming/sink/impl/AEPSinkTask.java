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
