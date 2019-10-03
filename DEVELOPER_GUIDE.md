# Developer Guide

The docker setup comes with a full stack of Kafka tools and utilities including Kafka Connect

* Kafka broker
* Zookeeper
* Kafka Rest proxy
* Kafka Topics UI
* Kafka Connect, with the AEP Sink Connector installed.

Once the docker is running, you should be able to test the entire setup using a rest api to insert the message into
your local docker kafka topic.

## Configuration Options

The AEP connector is a uber jar containing all the classfiles and its third-party dependencies.
To install the connector, drop the jar file into the plug in directory of Kafka connect installation.

AEP Sink connector configurations can be supplied in the call register the connector.


| Config Name                       | Description                                   | Default                                                 | Required | Example |
|-----------------------------------|-----------------------------------------------|---------------------------------------------------------|----------|---------|
| topics                            | comma separated list of topics                |                                                         | yes      |         |
| connector.class                   | classname of impl                             | com.adobe.platform.streaming.sink.impl.AEPSinkConnector | yes      |         |
| key.converter.schemas.enable      | enables conversion of schemas                 | false                                                   | no       |         |
| value.converter.schemas.enable    | enables conversion of schemas                 | false                                                   | no       |         |
| aep.endpoint                      | aep streaming endpoint url                    |                                                         | yes      |         |
| aep.connection.auth.enabled       | required for authenticated streaming endpoint | false                                                   | no       |         |
| aep.connection.auth.token.type    | always set to access_token                    | access_token                                            | no       |         |
| aep.connection.auth.client.id     | IMS client id                                 |                                                         | no       |         |
| aep.connection.auth.client.code   | IMS client code                               |                                                         | no       |         |
| aep.connection.auth.client.secret | IME client secret                             |                                                         |          |         |

## Step-by-Step Workflow

### Build
```./gradlew clean build```

### Build docker

```docker build -t streaming-connect .```

### Running Docker
```docker-compose up -d```

### Tail Docker logs
```docker logs experience-platform-streaming-connect_kafka-connect_1 -f```

### Manage running connectors

Kafka Connect exposes a set of [REST APIs][connect-apis] to manage
connect instances.

#### List of running connectors

```curl -X GET http://localhost:8083/connectors```

### Create a connector instance

Once the Connect server is running on port 8083, you can use REST APIs to launch multiple instances of connectors.


#### Create a Streaming Connection

In order to send streaming data, you must first request a Streaming Connection from Adobe by providing some essential
 properties. Data Inlet Registration APIs are behind adobe.io gateway, so the first step in requesting a new endpoint,
is to either use your existing authentication token and API key combination, or to create a new integration through
[Adobe I/O console][io-console]. More information about adobe.io based authentication is available [here][io-auth]. 

Once you have an IMS access token and API key, it needs to be provided as part of the POST request.

```
CURL -X POST "https://platform.adobe.io/data/core/edge/inlet" \
  -H "Cache-Control: no-cache" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ACCESS_TOKEN}" \
  -H "x-api-key: {API_KEY}" \
  -H "x-gw-ims-org-id: {IMS_ORG}" \
  -d '{
       "name" : "<data_inlet_name>",
       "description" : "<data_inlet_description>",
       "sourceId" : "<identifier_of_device_or_source_that_helps_you_identify_it>",
       "dataType": "xdm"
   }'
```

If the request was successful a new Data Inlet should be created for you. The response will looking similar to
the one below

```
{
    "inletUrl": "https://dcs.adobedc.net/collection/{DATA_INLET_ID}",
    "inletId": "{DATA_INLET_ID}",
    "imsOrg": "{IMS_ORG}",
    "sourceId": "website",
    "dataType": "xdm",
    "name": "My Data Inlet",
    "description": "Collects streaming data from my website",
    "authenticationRequired": false,
    "createdBy": "{API_KEY}",
    "createdAt": "2019-01-11T21:03:49.090Z",
    "modifiedBy": "{API_KEY}",
    "modifiedAt": "2019-01-11T21:03:49.090Z"
}
```

The `inletUrl` in the response above is the AEP Streaming Connection to which the real time events will be getting
sinked to.

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
    "aep.endpoint": "https://dcs.adobedc.net/collection/{DATA_INLET_ID}"
  }
}' http://localhost:8083/connectors
```

#### Adobe Experience Platform Sink Connector (Authentication Enabled)

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
    "aep.endpoint": "https://dcs.adobedc.net/collection/{DATA_INLET_ID}",
    "aep.connection.auth.enabled": true,
    "aep.connection.auth.token.type": "access_token",
    "aep.connection.auth.client.id": "<client_id>",
    "aep.connection.auth.client.code": "<client_code>",
    "aep.connection.auth.client.secret": "<client_secret>"
  }
}' http://localhost:8083/connectors
```

#### Adobe Experience Platform Sink Connector (Batching Enabled)

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
    "aep.endpoint": "https://dcs.adobedc.net/collection/{DATA_INLET_ID}",
    "aep.batch.enabled": true,
    "aep.batch.size": 2
  }
}' http://localhost:8083/connectors
```

#### Use the Kafka Topics UI to view your topics

The docker setup comes with Topics UI to view the topic and messages within.
Open a browser and go to http://localhost:8000 and view the connect-test topic

![Topics UI](./docs/resources/topics-ui.png)

In order to test the flow, you can use the following curl command to post a message into the Kafka topic using the
Kafka rest proxy. Please ensure that the curl command uses your inlet endpoint, and the schema of the XDM message
corresponding to your setup.

```bash
curl -X POST \
  http://localhost:8082/topics/connect-test \
  -H 'Content-Type: application/vnd.kafka.json.v2+json' \
  -H 'Host: localhost:8082' \
  -d '{
  "records": [{
    "value": {
      "header": {
        "schemaRef": {
          "id": "<schema-id>",
          "contentType": "application/vnd.adobe.xed-full+json;version=1"
        },
        "msgId": "1553542044760:1153:5",
        "source": {
          "name": "POCEvent1122ew2"
        },
        "msgVersion": "1.0",
        "imsOrgId": "0DD379AC5B117F6E0A494106@AdobeOrg"
      },
      "body": {
        "xdmMeta": {
          "schemaRef": {
            "id": "<schema-id>",
            "contentType": "application/vnd.adobe.xed-full+json;version=1"
          }
        },
        "xdmEntity": {
          "identityMap": {
            "email": [{
              "id": "ninair@adobe.com"
            }]
          },
          "_id": "1553542044071",
          "timestamp": "2019-03-25T19:27:24Z",
          "_msft_cds_acp": {
            "productListItems": {
              "priceTotal": 10,
              "name": "prod1",
              "_id": "1212121",
              "SKU": "13455"
            }
          }
        }
      }
    }
  }]
}'
```

You will be able to see the message written to the "connect-test" topic in the Local Kafka cluster, which is picked up 
by the AEP Streaming Sink Connector and sent the AEP Streaming inlet.

[io-auth]: https://www.adobe.io/apis/cloudplatform/console/authentication/gettingstarted.html
[blogpost]: https://medium.com/adobetech/using-postman-for-jwt-authentication-on-adobe-i-o-7573428ffe7f
[connect-apis]: https://docs.confluent.io/current/connect/references/restapi.html
[io-console]: https://console.adobe.io/
[tutorial]: https://www.adobe.io/apis/experienceplatform/home/tutorials/alltutorials.html#!api-specification/markdown/narrative/tutorials/authenticate_to_acp_tutorial/authenticate_to_acp_tutorial.md
