# What is Kafka Connect?

"[Kafka Connect](https://docs.confluent.io/current/connect/index.html)", an open source component of Apache Kafka, is a framework for connecting Kafka with external systems such as databases, key-value stores, search indexes, and file systems."

Kafka Connect is basically a set of connectors developed by the open source community around Apache Kafka, that allows developers to get data from their databases directly into Kafka, and then take that data from Kafka and put it into other systems like Elastic Search.

# Adobe Experience Platform

Adobe Experience Platform weaves all your critical customer data together in real time. Not just CRM or other first-party data. All of it — behavioral, transactional, financial, operational, and third-party data. Bring in your data, standardize it, make it smarter, and act on it across channels.
 
# Adobe Experience Platform Sink Connector

Adobe Experience Platform Sink connector allows users to stream real time events into Streaming Connections created in the [Platform](https://platform.adobe.com).

AEP Stream connector is based on Kafka Connect. Use this library to stream JSON events from a Kafka topic in your datacenter into a AEP Experience Platform endpoint.

As Adobe Platform customer, one can create an Streaming Connection and stream events to Adobe Experience Platform.

#### Features of AEP streaming connect

* Support for Authenticated streaming connections with integration with Adobe's Identity Management service 
* Support for Batching of requests ( for reduced network calls ) 


### Developer Guide
For running experience-platform-streaming-connect locally refer [Developer Guide](./DEVELOPER_GUIDE.md)



