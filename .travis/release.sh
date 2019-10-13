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

echo "This will trigger a release job on Travis, building the version of master as a release. Are you sure you want to continue? (Y/n)"

read CONTINUE_RELEASE

if [ "$CONTINUE_RELEASE" = "Y" ]; then
  TRAVIS_TOKEN=$(travis token)
  TRAVIS_REQUEST='{
   "request": {
     "message": "Perform Release",
     "branch":"master",
     "config": {
       "script": ".travis/travis-release.sh"
      }
    }
  }'

  curl -s -X POST \
   -H "Content-Type: application/json" \
   -H "Accept: application/json" \
   -H "Travis-API-Version: 3" \
   -H "Authorization: token $TRAVIS_TOKEN" \
   -d "$TRAVIS_REQUEST" \
   https://api.travis-ci.org/repo/adobe%2Fexperience-platform-streaming-connect/requests
else
  echo "Aborted."
fi
