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
    "connector.class": "com.adobe.platform.streaming.sink.impl.AEPSinkConnector",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false",
    "aep.endpoint": "https://dcs-dev.data.adobe.net/collection/33bd38e1d58b5f379ace3399aa34a32d5caf6fec9ed27924c5fc6f12d592d7c9"
  }
}' http://localhost:8083/connectors
```

### Adobe Experience Platform Sink Connector (Authentication Enabled)

```
curl -s -X POST \
-H "Content-Type: application/json" \
--data '{
  "name": "aep-auth-sink-connector",
  "config": {
    "topics": "connect-test",
    "connector.class": "com.adobe.platform.streaming.sink.impl.AEPSinkConnector",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false",
    "aep.endpoint": "https://dcs-dev.data.adobe.net/collection/33bd38e1d58b5f379ace3399aa34a32d5caf6fec9ed27924c5fc6f12d592d7c9",
    "aep.connection.auth.enabled": true,
    "aep.connection.auth.token.type": "access_token",
    "aep.connection.auth.client.id": "<client_id>",
    "aep.connection.auth.client.code": "<client_code>",
    "aep.connection.auth.client.secret": "<client_secret>"
  }
}' http://localhost:8083/connectors
```

### Adobe Experience Platform Sink Connector (Batching Enabled)

```
curl -s -X POST \
-H "Content-Type: application/json" \
--data '{
  "name": "aep-batch-sink-connector",
  "config": {
    "topics": "connect-test",
    "connector.class": "com.adobe.platform.streaming.sink.impl.AEPSinkConnector",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false",
    "aep.endpoint": "https://dcs-dev.data.adobe.net/collection/33bd38e1d58b5f379ace3399aa34a32d5caf6fec9ed27924c5fc6f12d592d7c9",
    "aep.batch.enabled": true,
    "aep.batch.size": 2
  }
}' http://localhost:8083/connectors
```
