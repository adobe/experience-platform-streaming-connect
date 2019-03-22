# What is Kafka Connect?

"[Kafka Connect](https://docs.confluent.io/current/connect/index.html)", an open source component of Apache Kafka, is a framework for connecting Kafka with external systems such as databases, key-value stores, search indexes, and file systems."

Kafka Connect is basically a set of connectors developed by the open source community around Apache Kafka, that allows developers to get data from their databases directly into Kafka, and then take that data from Kafka and put it into other systems like Elastic Search.

# Adobe Experience Platform

Adobe Experience Platform weaves all your critical customer data together in real time. Not just CRM or other first-party data. All of it — behavioral, transactional, financial, operational, and third-party data. Bring in your data, standardize it, make it smarter, and act on it across channels.
 
# Adobe Experience Platform Sink Connector

Adobe Experience Platform Sink connector allows users to stream real time events into Streaming Connections created in the [Platform](https://platform.adobe.com).

AEP Stream connector is based on Kafka Connect. Use this library to stream JSON events from a Kafka topic in your datacenter into a AEP Experience Platform endpoint.

As Adobe Platform customer, one can create an Streaming Connection and stream events to Adobe Experience Platform.

### Before you begin

First, you need to get an API Key and IMS Token for accessing Adobe Cloud Platform APIs.  We recommend you start with [this tutorial](https://www.adobe.io/apis/experienceplatform/home/tutorials/alltutorials.html#!api-specification/markdown/narrative/tutorials/authenticate_to_acp_tutorial/authenticate_to_acp_tutorial.md).  There's also a [super helpful blogpost](https://medium.com/adobetech/using-postman-for-jwt-authentication-on-adobe-i-o-7573428ffe7f) to better guide you through this process. 

### Create a Streaming Connection

In order to send streaming data, you must first request a Streaming Connection from Adobe by providing some essential properties.  Data Inlet Registration APIs are behind adobe.io gateway, so the first step in requesting a new endpoint, is to either use your existing authentication token and API key combination, or to create a new integration through (I/O console)[https://console.adobe.io/].  More information about adobe.io based authentication is available here [link](https://www.adobe.io/apis/cloudplatform/console/authentication/gettingstarted.html). 

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

### Developer Guide
For running experience-platform-streaming-connect locally refer [Developer Guide](./DEVELOPER_GUIDE.md)

#### Future Additions

* Support for Authentication
* Support for compression of JSON messages ( JSON Smile ) 
* Support for Batching of requests ( for reduced network calls ) 

