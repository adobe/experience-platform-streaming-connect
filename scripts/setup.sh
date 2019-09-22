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

dateString=`date +%Y_%m_%d_%H_%M_%S`

# Create A Schema
SCHEMA_NAME=${SCHEMA_NAME:-"Streaming_test_profile_api"}_${dateString}
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
dataSetName="Streaming Ingest Test_${dateString}"
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
INLET_NAME="My Streaming Endpoint_${dateString}"
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
aemSinkConnectorName="aep-sink-connector-${dateString}"
aemSinkConnector=$(curl -s -X POST \
  http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "'"${aemSinkConnectorName}"'",
    "config": {
      "topics": "connect-test",
      "connector.class": "com.adobe.platform.streaming.sink.impl.AEPSinkConnector",
      "key.converter.schemas.enable": "false",
      "value.converter.schemas.enable": "false",
      "aep.endpoint": "'"${streamingEndpoint}"'"
    }
}')

echo "AEP Sink Connector ${aemSinkConnectorName}"

# Create a temp file to publish data to topic via FileStreamSourceConnector

docker exec experience-platform-streaming-connect_kafka-connect_1 rm /tmp/test.txt
docker exec experience-platform-streaming-connect_kafka-connect_1 touch /tmp/test.txt

# Create instance of FileStreamSourceConnector
fileSourceConnectorName="local-file-source-${dateString}"
fileSourceConnector=$(curl -s -X POST \
  http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "'"${fileSourceConnectorName}"'",
    "config": {
      "topic": "connect-test",
      "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
      "file": "/tmp/test.txt"
    }
}')

datasetId=`echo ${dataSet} | cut -d'"' -f2 | cut -d'/' -f3`

# Generate sample data for the given schema to the inlet
echo "Enter the number of Experience events to publish"
read count

./generate_data.sh ${schema} ${datasetId} ${count}
