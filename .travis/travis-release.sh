#!/bin/sh -e
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

if [ -z "$DOCKERHUB_PASSWORD" ]; then
  echo "This is supposed to be executed on travis. Use .travis/release.sh to trigger a release job."
  exit 1
fi

echo "Performing docker login..."
echo $DOCKERHUB_PASSWORD | docker login -u $DOCKERHUB_USERNAME --password-stdin

echo "Checkout master branch explicitly, as we run the release with a in detached head."
git checkout -qf master;

echo "Starting gradle build..."
./gradlew clean build

TAG_NAME=${TAG_NAME:-"latest"}
echo "Starting docker build for tag name ${TAG_NAME}..."
docker build -t adobe/experience-platform-streaming-connect:${TAG_NAME} .
docker push adobe/experience-platform-streaming-connect:${TAG_NAME}
