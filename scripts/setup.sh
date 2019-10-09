 #
 # Copyright 2019 Adobe. All rights reserved.
 # This file is licensed to you under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License. You may obtain a copy
 # of the License at http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software distributed under
 # the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 # OF ANY KIND, either express or implied. See the License for the specific language
 # governing permissions and limitations under the License.
 #

#!/bin/sh
set -e

IMS_HOST=${IMS_HOST:-"ims-na1.adobelogin.com"}
PLATFORM_GATEWAY=${PLATFORM_GATEWAY:-"https://platform.adobe.io/"}

IMS_ORG=${IMS_ORG:-""}
if [ -z "${IMS_ORG}" ]; then
  echo "Enter IMS ORG"
  read IMS_ORG
fi

CLIENT_ID=${CLIENT_ID:-""}
if [ -z "${CLIENT_ID}" ]; then
  echo "Enter Client ID"
  read CLIENT_ID
fi

CLIENT_SECRET=${CLIENT_SECRET:-""}
if [ -z "${CLIENT_SECRET}" ]; then
  echo "Enter Client Secret"
  read CLIENT_SECRET
fi

JWT_TOKEN=${JWT_TOKEN}
if [ -z "${JWT_TOKEN}" ]; then
  echo "Enter JWT Token"
  read JWT_TOKEN
fi

# Fetch Access token
access_token=$(curl -X POST \
  https://${IMS_HOST}/ims/exchange/jwt/ \
  -H "content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" \
  -F client_id=${CLIENT_ID} \
  -F client_secret=${CLIENT_SECRET} \
  -F jwt_token=${JWT_TOKEN} 2>/dev/null | jq -r ".access_token")

dateString=`date +%Y%m%d%H%M%S`

# Create A Schema
SCHEMA_NAME=${SCHEMA_NAME:-"Streaming_test_profile_api"}-${dateString}
echo "Making call to create schema ${PLATFORM_GATEWAY} with name ${SCHEMA_NAME}"
schema=$(curl -X POST \
  ${PLATFORM_GATEWAY}data/foundation/schemaregistry/tenant/schemas \
  -H "Authorization: Bearer ${access_token}" \
  -H "Content-Type: application/json" \
  -H "x-api-key: ${CLIENT_ID}" \
  -H "x-gw-ims-org-id: ${IMS_ORG}" \
  -d '{
    "type": "object",
    "title": "'"${SCHEMA_NAME}"'",
    "description": "AEP Streaming Connect Schema",
    "allOf": [
      {
        "$ref": "https://ns.adobe.com/xdm/context/profile"
      },
      {
        "$ref": "https://ns.adobe.com/xdm/context/profile-person-details"
      },
      {
        "$ref": "https://ns.adobe.com/xdm/context/identitymap"
      },
      {
        "$ref": "https://ns.adobe.com/xdm/context/profile-work-details"
      }
    ],
    "meta:extends": [
      "https://ns.adobe.com/xdm/context/profile",
      "https://ns.adobe.com/xdm/context/identitymap",
      "https://ns.adobe.com/xdm/context/profile-person-details",
      "https://ns.adobe.com/xdm/context/profile-work-details"
    ],
    "meta:immutableTags": [
      "union"
    ]
}' 2>/dev/null | jq -r '.["$id"]')

echo "Schema ID: "${schema}

# Create a dataset for the schema
dataSetName="Streaming Ingest Test-${dateString}"
dataSet=$(curl -X POST \
  ${PLATFORM_GATEWAY}data/foundation/catalog/dataSets \
  -H "Authorization: Bearer ${access_token}" \
  -H "Content-Type: application/json" \
  -H "x-api-key: ${CLIENT_ID}" \
  -H "x-gw-ims-org-id: ${IMS_ORG}" \
  -d '{
    "name": "'"${dataSetName}"'",
    "description": "Test for ingesting streaming data into profile",
    "schemaRef": {
      "id": "'"${schema}"'",
      "contentType": "application/vnd.adobe.xed-full+json;version=1"
    },
    "fileDescription": {
      "persisted": true,
      "containerFormat": "parquet",
      "format": "parquet"
    },
    "streamingIngestionEnabled": "true",
    "tags": {
      "unifiedIdentity": ["enabled:true"],
      "unifiedProfile": ["enabled:true"]
    }
  }' 2> /dev/null)

echo "Data Set: "${dataSet}

# Create a streaming connection
INLET_NAME="My Streaming Endpoint-${dateString}"
INLET_SOURCE="AEP_Streaming_Connection_${dateString}"
streamingEndpoint=$(curl POST \
  ${PLATFORM_GATEWAY}data/core/edge/inlet \
  -H "Authorization: Bearer ${access_token}" \
  -H "Content-Type: application/json" \
  -H "x-api-key: ${CLIENT_ID}" \
  -H "x-gw-ims-org-id: ${IMS_ORG}" \
  -d '{
    "name" : "'"${INLET_NAME}"'",
    "description" : "Collects streaming data from my website",
    "sourceId" : "'"${INLET_SOURCE}"'",
    "dataType": "xdm"
}' 2> /dev/null | jq -r ".inletUrl" )

echo "Streaming Connection: "${streamingEndpoint}

# Create a Connect Instance
connectTopicName="connect-test-${dateString}"
docker exec experience-platform-streaming-connect_kafka1_1 /usr/bin/kafka-topics --bootstrap-server localhost:9092 \
 --create --replication-factor 1 --partitions 1 --topic ${connectTopicName}

aemSinkConnectorName="aep-sink-connector-${dateString}"
aemSinkConnector=$(curl -s -X POST \
  http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "'"${aemSinkConnectorName}"'",
    "config": {
      "topics": "'"${connectTopicName}"'",
      "connector.class": "com.adobe.platform.streaming.sink.impl.AEPSinkConnector",
      "key.converter.schemas.enable": "false",
      "value.converter.schemas.enable": "false",
      "aep.endpoint": "'"${streamingEndpoint}"'"
    }
}')

echo "AEP Sink Connector ${aemSinkConnectorName}"

datasetId=`echo ${dataSet} | cut -d'"' -f2 | cut -d'/' -f3`

# Generate sample data for the given schema to the inlet
echo "Enter the number of Experience events to publish"
read count

echo "org=${IMS_ORG}" > ./application.conf
echo "schema=${schema}" >> ./application.conf
echo "dataset=${datasetId}" >> ./application.conf
echo "topic=${connectTopicName}" >> ./application.conf

./generate_data.sh ${count} ${IMS_ORG} ${schema} ${datasetId}  ${connectTopicName}
