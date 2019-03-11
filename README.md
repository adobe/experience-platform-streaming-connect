# experience-platform-streaming-connect
Experience platform Streaming Connectors for Data Ingress and Egress

Streaming Data Access for 3rd Party Developers using Kafka Connect. This includes support of sinking data to/from Adobe
to external systems such as databases, key-value stores, search indexes, and file systems.

# Sink Connectors
Apart from OOTB Sink connectors, supported by Kafka, we have enabled HTTP based DCS Sink Connector.

### Data Collection Services (DCS)
As Adobe customer, one can create an inlet and sink data to Adobe Pipeline using the DCS publish endpoint.

# How to Setup
<TBA>

### DCS Sink Connector
```
curl -s -X POST \
-H "Content-Type: application/json" \
--data '{
  "name": "DCS-connector",
  "config": {
    "topics": "connect-test",
    "connector.class": "com.adobe.platform.streaming.sink.impl.DCSSinkConnector",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false",
    "dcs.endpoint": "https://dcs-dev.data.adobe.net/collection/33bd38e1d58b5f379ace3399aa34a32d5caf6fec9ed27924c5fc6f12d592d7c9"
  }
}' http://streaming-connect:8083/connectors
```

### Developer Guide
For running experience-platform-streaming-connect locally refer [Developer Guide](./DEVELOPER_GUIDE.md)

#### Future Additions
We can add other Destinations Sink easily
[Google Cloud Platform](https://github.com/GoogleCloudPlatform/pubsub)
