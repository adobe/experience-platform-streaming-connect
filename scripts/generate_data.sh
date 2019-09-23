#!/bin/sh
set -e

totalMessages=$1
imsOrg=$2
schemaRef=$3
datasetId=$4
topicName=$5

. ./application.conf

if [ -z "${imsOrg}" ]; then
  if [ -z "${org}" ]; then
    echo "Enter IMS ORG"
    read imsOrg
  else
    echo "IMS ORG: ${org}"
    imsOrg=${org}
  fi
fi

if [ -z "${schemaRef}" ]; then
  if [ -z "${schema}" ]; then
    echo "Enter Schema ref"
    read schemaRef
  else
    echo "Schema ref: ${schema}"
    schemaRef=${schema}
  fi
fi

if [ -z "${datasetId}" ]; then
  if [ -z "${dataset}" ]; then
    echo "Enter Dataset ID"
    read datasetId
  else
    echo "Dataset ID: ${dataset}"
    datasetId=${dataset}
  fi
fi

if [ -z "${topicName}" ]; then
  if [ -z "${topic}" ]; then
    echo "Enter Topic"
    read topicName
  else
    echo "Topic Name: ${topic}"
    topicName=${topic}
  fi
fi

echo "Publishing ${totalMessages} messages for Data set ${datasetId} and schema ${schemaRef}"
for i in `seq 1 ${totalMessages}`;
do
  ecid="100000000000000000000000000000000000$1"
  message='{
    "records": [{
      "value": {
        "header": {
          "schemaRef": {
            "id": "'"${schemaRef}"'",
            "contentType": "application/vnd.adobe.xed-full+json;version=1.3"
          },
          "imsOrgId": "'"${imsOrg}"'",
          "source": {
            "name": "GettingStarted"
          },
          "datasetId": "'"${datasetId}"'"
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
                  "id": "'"${ecid}"'"
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
      }
    }]
  }'
  messageToPublish=`echo ${message} | jq -c .`
  response=$(curl -X POST http://localhost:8082/topics/${topicName} \
    -H 'Content-Type: application/vnd.kafka.json.v2+json' \
    -H 'Host: localhost:8082' \
    -d ${messageToPublish} 2> /dev/null)
done

echo "Published ${totalMessages} messages"
