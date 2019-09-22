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

#### Features

* Seamlessly ingest events from your Kafka topic to Adobe Experience Platform
* Support for Authenticated streaming connections with integration with Adobe's Identity Management Service 
* Support for Batching of requests (for reduced network calls)


### Quick Start
For running experience-platform-streaming-connect with minimal commands. Following figure illustrates steps simulated by setup script.
![AEP Sink Connector setup](./docs/resources/aep_sink_connector_setup.png)

#### Prerequisite
* Install [docker][docker]
* Install [jq][jq-tool]
```bash
brew install jq
```

#### Build Docker and Run
```bash
./gradlew build copyDependencies
docker build -t streaming-connect .
docker-compose up -d
```

#### Create Required Resources and send data
```bash
cd scripts
./setup.sh
```
The output would be like following
```
Enter IMS ORG
<IMS-ORG>
Enter Client ID
***
Enter Client Secret
***
Enter JWT Token
***

Making call to create schema https://platform.adobe.io/ with name Test Schema_2019_09_21_18_42_57
Schema ID: https://ns.adobe.com/<tenant>/schemas/55f4ad0f534b3ffcbe18be78b01e0651
Data Set: ["@/dataSets/5d86d1a29ba7e11648cc3afb"]
Streaming Connection: https://dcs-int.adobedc.net/collection/1e58b84cb62853b333b54980c45bdb40fc3bf80bc47022da0f76eececb2f9237
AEP Sink Connector aep-sink-connector-2019_09_21_18_42_57
Enter the number of Experience events to publish
5
Publishing 5 messages for Data set 5d86d1a29ba7e11648cc3afb and schema https://ns.adobe.com/<tenant>/schemas/55f4ad0f534b3ffcbe18be78b01e0651
Published 5 messages
```

#### Send data to Adobe with quick-start script
In case the resources are already created then you can run only data generation script to send data to Adobe
```
cd scripts
./generate_data.sh <schemaUrl> <datasetId> <count>
Example: ./generate_data.sh https://ns.adobe.com/<tenant>/schemas/55f4ad0f534b3ffcbe18be78b01e0651 5d86d1a29ba7e11648cc3afb 500
```

> Note: To debug logs you may use following command in different terminal
```bash
docker logs experience-platform-streaming-connect_kafka-connect_1 -f
```

#### Verify Data landing into AEP
To verify your data is landing into platform, login to [AEP][aep] and follow [documentation][monitor-streaming-data-flows] for monitoring your streaming data flows.

### Developer Guide
For running experience-platform-streaming-connect locally step-by-step refer [Developer Guide](./DEVELOPER_GUIDE.md)

[aep]: https://platform.adobe.com
[docker]: https://www.docker.com/
[jq-tool]: https://stedolan.github.io/jq/download/
[monitor-streaming-data-flows]: https://www.adobe.io/apis/experienceplatform/home/data-ingestion/data-ingestion-services.html#!api-specification/markdown/narrative/technical_overview/streaming_ingest/e2e-monitor-streaming-data-flows.md
