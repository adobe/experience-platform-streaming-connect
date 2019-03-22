# Developer Guide

## Build
```./gradlew build copyDependencies```

## Build docker

```docker build -t streaming-connect .```

## Running Docker
```docker-compose up -d```

## Tail Docker logs
```docker logs streaming-connect -f```

## Manage running connectors

Kafka Connect exposes a set of [REST APIs](https://docs.confluent.io/current/connect/references/restapi.html) to manage
connect instances.

### List of running connectors

```docker exec streaming-connect curl -X GET http://localhost:8083/connectors```

## Create a connector instance

Once the Connect server is running on port 8083, you can use REST APIs to launch multiple instances of connectors.

### Adobe Experience Platform Sink Connector

Before you begin make sure you have create a Streaming Connection endpoint by following the steps outlines in the [readme](./README.md)

```
curl -s -X POST \
-H "Content-Type: application/json" \
--data '{
  "name": "aep-sink-connector",
  "config": {
    "topics": "connect-test",
    "connector.class": "com.adobe.platform.streaming.sink.impl.DCSSinkConnector",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false",
    "dcs.endpoint": "https://dcs-dev.data.adobe.net/collection/33bd38e1d58b5f379ace3399aa34a32d5caf6fec9ed27924c5fc6f12d592d7c9"
  }
}' http://localhost:8083/connectors
```

### Debug using Console Sink Connector

For debugging we can use Console Sink connector, which would print the messages on console

```
curl -s -X POST \
-H "Content-Type: application/json" \
--data '{
  "name": "TestConsoleSinkConnector",
  "config": {
    "topics": "connect-test",
    "connector.class": "com.adobe.platform.pipeline.connect.sink.console.ConsoleSinkConnector"
  }
}' http://localhost:8083/connectors
```

```
connect | [2018-12-05 17:29:06,779] INFO Received sink record with metadata {kafka.partition=0, kafka.offset=1267, kafka.timestamp=1544030946379, kafka.topic=connect-test, key=} (com.adobe.platform.pipeline.connect.sink.console.ConsoleSinkTask)
connect | [2018-12-05 17:29:06,779] INFO Sink record: SinkRecord{kafkaOffset=1267, timestampType=CreateTime} ConnectRecord{topic='connect-test', kafkaPartition=0, key=, value={a=441, b=315}, timestamp=1544030946379, headers=ConnectHeaders(headers=)} (com.adobe.platform.pipeline.connect.sink.console.ConsoleSinkTask)
```
