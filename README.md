# What is Kafka Connect?

"[Kafka Connect](https://docs.confluent.io/current/connect/index.html)", an open source component of Apache Kafka, is a framework for connecting Kafka with external systems such as databases, key-value stores, search indexes, and file systems.

Kafka Connect is a framework which enables connectors developed by the open source community around Apache Kafka. It allows developers to easily import data from their data sources directly into Kafka, and then take that data from Kafka and then feed it into other systems like Elastic Search.

# Adobe Experience Platform

Adobe Experience Platform weaves all your critical customer data together in real time. This include behavioral, transactional, financial, operational, and third-party data and not just CRM or other first-party data.

Bring in your data, standardize it, make it smarter. See what your customer wants right now and build experiences to match.
 
# Adobe Experience Platform Sink Connector

Adobe Experience Platform Stream connector is based on Kafka Connect. Use this library to stream JSON events from Kafka topics in your datacenter directly into a Adobe Experience Platform in real-time.

#### Architecture

AEP sink connector delivers data from Kafka topics to a registered endpoint of the Adobe Experience Platform.

![AEP Sink Connector](./docs/resources/aep_sink_connector.png)

#### Configuration Options

The AEP connector is a uber jar containing all the classfiles and its third-party dependencies.
To install the connector, drop the jar file into the plug in directory of Kafka connect installation.

AEP Sink connector configurations can be supplied in the call register the connector.


| Config Name                       | Description                               | Default                                                 | Required | Example |
|-----------------------------------|-------------------------------------------|---------------------------------------------------------|----------|---------|
| topics                            | comma separated list of topics                    |                                                         | yes      |         |
| connector.class                   | classname of impl                         | com.adobe.platform.streaming.sink.impl.AEPSinkConnector | yes      |         |
| key.converter.schemas.enable      | enables conversion of schemas             | false                                                   | no       |         |
| value.converter.schemas.enable    | enables conversion of schemas             | false                                                   | no       |         |
| aep.endpoint                      | aep streaming endpoint url                             |                                                         | yes      |         |
| aep.connection.auth.enabled       | required when authenticated streaming endpoint is used | false                                                   | no       |         |
| aep.connection.auth.token.type    | always set to access_token                | access_token                                            | no       |         |
| aep.connection.auth.client.id     | IMS client id                             |                                                         | no       |         |
| aep.connection.auth.client.code   | IMS client code                           |                                                         | no       |         |
| aep.connection.auth.client.secret | IME client secret                         |                                                         |          |         |

#### Features

* Seamlessly ingest events from your Kafka topic to Adobe Experience Platform
* Support for Authenticated streaming connections with integration with Adobe's Identity Management Service 
* Support for Batching of requests (for reduced network calls)


### Quick Start
For running experience-platform-streaming-connect locally refer [Developer Guide](./DEVELOPER_GUIDE.md)
