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


### Before you begin

First, you need to get an API Key and IMS Token for accessing Adobe Cloud Platform APIs.  We recommend you start with [this tutorial](https://www.adobe.io/apis/experienceplatform/home/tutorials/alltutorials.html#!api-specification/markdown/narrative/tutorials/authenticate_to_acp_tutorial/authenticate_to_acp_tutorial.md).  There's also a [super helpful blogpost](https://medium.com/adobetech/using-postman-for-jwt-authentication-on-adobe-i-o-7573428ffe7f) to better guide you through this process. 

### Create a Streaming Connection

In order to send streaming data, you must first request a Streaming Connection from Adobe by providing some essential properties.  Data Inlet Registration APIs are behind adobe.io gateway, so the first step in requesting a new endpoint, is to either use your existing authentication token and API key combination, or to create a new integration through [Adobe I/O console](https://console.adobe.io/).  More information about adobe.io based authentication is available here [link](https://www.adobe.io/apis/cloudplatform/console/authentication/gettingstarted.html). 

Once you have an IMS access token and API key, it needs to be provided as part of the POST request.

```curl -X POST https://platform.adobe.io/data/core/edge/inlet \
     -H 'Authorization: Bearer <ims_token>' \
     -H 'Cache-Control: no-cache' \
     -H 'Content-Type: application/json' \
     -H 'x-api-key: <api_key>' \
     -H 'x-gw-ims-org-id: <ims_org_id>' \
     -d '{
       "name" : "<data_inlet_name>",
       "description" : "<data_inlet_description>",
       "sourceId" : "<identifier_of_device_or_source_that_helps_you_identify_it>",
       "dataType": "xdm"
   }'
```

If the request was successful a new Data Inlet should be created for you.  The response will looking similar to the one below

```
{
  "inletId": "d212ea1db6c896ef6c59c7443c717d05232e8f85bfffb0988000d68fe46dd373",
  "imsOrg": "<ims_org_id>",
  "sourceId": "<identifier_of_device_or_source__that_helps_you_identify_it>",
  "dataType": "xdm",
  "name": "<data_inlet_name>",
  "description": "<data_inlet_description>.",
  "createdBy": "<api_key>",
  "authenticationRequired": false,
  "createdDate": 1532624324022,
  "modifiedDate": 1532624324022,
  "streamingEndpointUrl": "https://dcs.data.adobe.net/collection/d212ea1db6c896ef6c59c7443c717d05232e8f85bfffb0988000d68fe46dd373"
}
```

The `streamingEndpointUrl` in the response above is the AEP Streaming Connection to which the real time events will be getting sinked to.


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

Use the command below to set up an Sink connector to a Authenticated Streaming Connection:


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

Use the command below to set up an Sink connector to batch up requests and reduce the number of network calls 

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
