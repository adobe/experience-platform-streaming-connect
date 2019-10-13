#!/bin/sh
##
 # Copyright 2019 Adobe. All rights reserved.
 # This file is licensed to you under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License. You may obtain a copy
 # of the License at http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software distributed under
 # the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 # OF ANY KIND, either express or implied. See the License for the specific language
 # governing permissions and limitations under the License.
##

set -e

totalMessages=$1
imsOrg=$2
schemaRef=$3
datasetId=$4
topicName=$5

. ${PWD}/application.conf

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
  ecid="10000000000000000000"$(printf "%018d" $i)
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
  response=$(curl -X POST http://kafka-rest-proxy:8082/topics/${topicName} \
    -H 'Content-Type: application/vnd.kafka.json.v2+json' \
    -H 'Host: kafka-rest-proxy:8082' \
    -d ${messageToPublish} 2> /dev/null)
done

echo "Published ${totalMessages} messages"
