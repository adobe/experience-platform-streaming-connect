#!/bin/sh
set -e

schemaRef=$1
DATASET_ID=$2

message='{
  "header": {
    "schemaRef": {
      "id": "'"${schemaRef}"'",
      "contentType": "application/vnd.adobe.xed-full+json;version=1.3"
    },
    "imsOrgId": "'"${IMS_ORG}"'",
    "source": {
      "name": "GettingStarted"
    },
    "datasetId": "'"${DATASET_ID}"'"
  },
  "body": {
    "xdmMeta": {
      "schemaRef": {
        "id": "'"${schemaRef}"'",
        "contentType": "application/vnd.adobe.xed-full+json;version=1.3"
      }
    },
    "xdmEntity": {
      "identityMap": {
        "ecid": [
          {
            "id": "10000000000000000000000000000000000001"
          }
        ],
        "email": [
          {
            "id": "janejoe@adobe.com",
            "primary": true
          }
        ]
      },
      "person": {
        "name": {
          "firstName": "Jane",
          "middleName": "F",
          "lastName": "Doe"
        },
        "birthDate": "1967-01-03",
        "gender": "female"
      },
      "workEmail": {
        "primary": true,
        "address": "janejoe@adobe.com",
        "type": "work",
        "status": "active"
      }
    }
  }
}'

messageToPublish=`echo ${message} | jq -c .`

echo "Publishing $3 messages for Data set $2 and schema $1"
for i in `seq 1 $3`;
do
  docker exec experience-platform-streaming-connect_kafka-connect_1 bash -c "echo '${messageToPublish}' >> /tmp/test.txt"
done

echo "Published $3 messages"
