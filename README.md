# What is Kafka Connect?

"[Kafka Connect](https://docs.confluent.io/current/connect/index.html)", an open source component of Apache Kafka, is a framework for connecting Kafka with external systems such as databases, key-value stores, search indexes, and file systems.

Kafka Connect is a framework which enables connectors developed by the open source community around Apache Kafka. It allows developers to easily import data from their data sources directly into Kafka, and then take that data from Kafka and then feed it into other systems like Elastic Search.

# Adobe Experience Platform

Adobe Experience Platform weaves all your critical customer data together in real time. This include behavioral, transactional, financial, operational, and third-party data and not just CRM or other first-party data.

Bring in your data, standardize it, make it smarter. See what your customer wants right now and build experiences to match.
 
# Adobe Experience Platform Sink Connector

Adobe Experience Platform Stream connector is based on Kafka Connect. Use this library to stream JSON events from Kafka topics in your datacenter directly into a Adobe Experience Platform in real-time.

#### Features of AEP streaming connect

* Seamlessly ingest events from your Kafka in seconds into Unified Profile to power real-time CDP use cases
* Support for Authenticated streaming connections with integration with Adobe's Identity Management Service 
* Support for Batching of requests (for reduced network calls)


### Developer Guide
For running experience-platform-streaming-connect locally refer [Developer Guide](./DEVELOPER_GUIDE.md)
