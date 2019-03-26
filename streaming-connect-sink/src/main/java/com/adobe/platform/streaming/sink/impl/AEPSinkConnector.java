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

import com.adobe.platform.streaming.sink.AbstractSinkConnector;
import org.apache.kafka.connect.connector.Task;

/**
 * @author Adobe Inc.
 */
public class AEPSinkConnector extends AbstractSinkConnector {

  @Override
  public Class<? extends Task> taskClass() {
    return AEPSinkTask.class;
  }

}
