#!/bin/sh
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
ims_response=$(curl -i -o - --silent -X POST \
  https://${IMS_HOST}/ims/exchange/jwt/ \
  -H "Content-Type: multipart/form-data" \
  -F client_id=${CLIENT_ID} \
  -F client_secret=${CLIENT_SECRET} \
  -F jwt_token=${JWT_TOKEN} 2>/dev/null)

ims_response_code=$(echo "$ims_response" | grep -v '100 Continue' | grep HTTP |  awk '{print $2}')
if [[ "${ims_response_code}" -ge "400" ]]; then
  echo "Error: Unable to fetch access token from IMS, response code: ${ims_response_code}";
  exit 1;
fi

access_token=$(echo "${ims_response}" | grep 'access_token' | jq -r ".access_token");

dateString=`date +%Y%m%d%H%M%S`

DEFAULT_SCHEMA_NAME="Streaming_Connect_Schema_${dateString}"
if [[ -z "${SCHEMA_NAME}" ]]; then
  echo "Enter Schema Name: [default: ${DEFAULT_SCHEMA_NAME}]"
  read SCHEMA_NAME
fi

SCHEMA_NAME=${SCHEMA_NAME:-${DEFAULT_SCHEMA_NAME}}
echo "Making call to create schema to ${PLATFORM_GATEWAY} with name ${SCHEMA_NAME}"

schema_response=$(curl -i -o - --silent -X POST \
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
}' 2>/dev/null)

schema_response_code=$(echo "$schema_response" | grep -v '100 Continue' | grep HTTP |  awk '{print $2}')
if [[ "${schema_response_code}" -ge "400" ]]; then
  echo "Error: Unable to create schema, response code: ${schema_response_code}";
  exit 1;
fi

schema=$(echo "${schema_response}" | grep 'meta:resourceType' | jq -r '.["$id"]')
echo "Schema ID: ${schema}"

# Create a dataset for the schema
DEFAULT_DATASET_NAME="Streaming_Ingest_Test_${dateString}"
if [[ -z "${DATASET_NAME}" ]]; then
  echo "Enter Dataset Name: [default: ${DEFAULT_DATASET_NAME}]"
  read DATASET_NAME
fi

DATASET_NAME=${DATASET_NAME:-${DEFAULT_DATASET_NAME}}

echo "Making call to create dataset to ${PLATFORM_GATEWAY} with name ${DATASET_NAME}"
dataSet=$(curl -X POST \
  ${PLATFORM_GATEWAY}data/foundation/catalog/dataSets \
  -H "Authorization: Bearer ${access_token}" \
  -H "Content-Type: application/json" \
  -H "x-api-key: ${CLIENT_ID}" \
  -H "x-gw-ims-org-id: ${IMS_ORG}" \
  -d '{
    "name": "'"${DATASET_NAME}"'",
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
DEFAULT_INLET_NAME="My Streaming Connection-${dateString}"
if [ -z "${STREAMING_CONNECTION_NAME}" ]; then
  echo "Enter Streaming Connection Name: [default: ${DEFAULT_INLET_NAME}]"
  read STREAMING_CONNECTION_NAME
fi

INLET_NAME=${STREAMING_CONNECTION_NAME:-${DEFAULT_INLET_NAME}}

DEFAULT_INLET_SOURCE="My Streaming Source-${dateString}"
if [[ -z "${STREAMING_CONNECTION_SOURCE}" ]]; then
  echo "Enter Streaming Connection Source: [default: ${DEFAULT_INLET_SOURCE}]"
  read STREAMING_CONNECTION_SOURCE
fi
INLET_SOURCE=${STREAMING_CONNECTION_SOURCE:-${DEFAULT_INLET_SOURCE}}

echo "Making call to create streaming connection to ${PLATFORM_GATEWAY} with name ${INLET_NAME} and source ${INLET_SOURCE}"

connection_response=$(curl -X POST \
  ${PLATFORM_GATEWAY}data/foundation/flowservice/connections \
  -H "Authorization: Bearer ${access_token}" \
  -H "Content-Type: application/json" \
  -H "x-api-key: ${CLIENT_ID}" \
  -H "x-gw-ims-org-id: ${IMS_ORG}" \
  -d '
   {
    "name": "'"${INLET_NAME}"'",
    "providerId": "521eee4d-8cbe-4906-bb48-fb6bd4450033",
    "description": "Streaming Connection from kafka topic",
    "connectionSpec": {
        "id": "bc7b00d6-623a-4dfc-9fdb-f1240aeadaeb",
        "version": "1.0"
    },
    "auth": {
        "specName": "Streaming Connection",
        "params": {
            "sourceId": "'"${INLET_SOURCE}"'",
            "dataType": "xdm",
            "name": "'"${INLET_NAME}"'"
        }
    }
   }' 2> /dev/null)

connection_response_code=$(echo "${connection_response}" | grep -v '100 Continue' | grep HTTP  |  awk '{print $2}')
if [[ "${connection_response_code}" -ge "400" ]]; then
  echo "Error: Unable to create streaming connection, response code: ${connection_response_code}";
  exit 1;
fi
streamingConnectionId=$(echo "${connection_response}" | grep 'id' | jq -r ".id")

echo "Streaming Connection: ${streamingConnectionId}"

inlet_response=$(curl -i -o - --silent \
  ${PLATFORM_GATEWAY}data/foundation/flowservice/connections/${streamingConnectionId} \
  -H "Authorization: Bearer ${access_token}" \
  -H "Content-Type: application/json" \
  -H "x-api-key: ${CLIENT_ID}" \
  -H "x-gw-ims-org-id: ${IMS_ORG}"
  2> /dev/null)

inlet_response_code=$(echo "$inlet_response" | grep -v '100 Continue' | grep HTTP  |  awk '{print $2}')
if [[ "${inlet_response_code}" -ge "400" ]]; then
  echo "Error: Unable to fetch connection info, response code: ${inlet_response_code}";
  exit 1;
fi
streamingEndpoint=$(echo "${inlet_response}" | grep "inletUrl" | jq -r ".items[0].auth.params.inletUrl")

echo "Streaming Connection: "${streamingEndpoint}

# Create a Connect Instance
connectTopicName="connect-test-${dateString}"
${KAFKA_HOME}/bin/kafka-topics.sh \
 --bootstrap-server kafka1:19092 \
 --create --replication-factor 1 \
 --partitions 1 \
 --topic ${connectTopicName}

aemSinkConnectorName="aep-sink-connector-${dateString}"
aem_connector_response=$(curl -i -o - --silent -X POST \
  http://kafka-connect:8083/connectors \
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

aem_connector_response_code=$(echo "$aem_connector_response" | grep -v '100 Continue'| grep HTTP |  awk '{print $2}')
if [[ "${aem_connector_response_code}" -ge "400" ]]; then
  echo "Error: Unable to create streaming connector, response code: ${aem_connector_response_code}";
  exit 1;
fi

echo "AEP Sink Connector ${aemSinkConnectorName}"

datasetId=`echo ${dataSet} | cut -d'"' -f2 | cut -d'/' -f3`

# Generate sample data for the given schema to the inlet
echo "Enter the number of Experience events to publish"
read count

echo "org=${IMS_ORG}" > ${PWD}/application.conf
echo "schema=${schema}" >> ${PWD}/application.conf
echo "dataset=${datasetId}" >> ${PWD}/application.conf
echo "topic=${connectTopicName}" >> ${PWD}/application.conf

${PWD}/generate_data.sh ${count} ${IMS_ORG} ${schema} ${datasetId}  ${connectTopicName}
