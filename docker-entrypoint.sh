#!/usr/bin/env bash
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

tag="[docker-entrypoint.sh]"

function info {
  echo "$tag (INFO) : $1"
}
function warn {
  echo "$tag (WARN) : $1"
}
function error {
  echo "$tag (ERROR): $1"
}

set -e

# Verify envs
if [[ -z "$CONNECT_BOOTSTRAP_SERVERS" ]]; then
  error "EMPTY ENV 'CONNECT_BOOTSTRAP_SERVERS'"; exit 1
fi

if [[ -z "$CONNECT_REST_ADVERTISED_HOST_NAME" ]]; then
  warn "EMPTY ENV 'CONNECT_REST_ADVERTISED_HOST_NAME'"; unset $CONNECT_REST_ADVERTISED_HOST_NAME
fi

if [[ -z "$CONNECT_REST_ADVERTISED_PORT" ]]; then
  warn "EMPTY ENV 'CONNECT_REST_ADVERTISED_PORT'"; unset $CONNECT_REST_ADVERTISED_PORT
fi
if [[ -z "$CONNECT_GROUP_ID" ]]; then
  warn "EMPTY ENV 'CONNECT_GROUP_ID'. USE DEFAULT VALUE"; unset $CONNECT_GROUP_ID
fi

export KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote=true \
  -Djava.rmi.server.hostname=${CONNECT_REST_ADVERTISED_HOST_NAME} \
  -Dcom.sun.management.jmxremote.rmi.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false"

#
# Configure the log files ...
#
if [[ -n "$CONNECT_LOG4J_LOGGERS" ]]; then
  sed -i -r -e "s|^(log4j.rootLogger)=.*|\1=${CONNECT_LOG4J_LOGGERS}|g" $CONNECT_LOG_CFG
  unset CONNECT_LOG4J_LOGGERS
fi
env | grep '^CONNECT_LOG4J' | while read -r VAR;
do
  env_var=`echo "$VAR" | sed -r "s/([^=]*)=.*/\1/g"`
  prop_name=`echo "$VAR" | sed -r "s/^CONNECT_([^=]*)=.*/\1/g" | tr '[:upper:]' '[:lower:]' | tr _ .`
  prop_value=`echo "$VAR" | sed -r "s/^CONNECT_[^=]*=(.*)/\1/g"`
  if egrep -q "(^|^#)$prop_name=" $CONNECT_LOG_CFG; then
    #note that no config names or values may contain an '@' char
    sed -r -i "s@(^|^#)($prop_name)=(.*)@\2=${prop_value}@g" $CONNECT_LOG_CFG
  else
    echo "$prop_name=${prop_value}" >> $CONNECT_LOG_CFG
  fi
  if [[ "$SENSITIVE_PROPERTIES" = *"$env_var"* ]]; then
    echo "--- Setting logging property from $env_var: $prop_name=[hidden]"
  else
    echo "--- Setting logging property from $env_var: $prop_name=${prop_value}"
  fi
  unset $env_var
done

if [[ -n "$LOG_LEVEL" ]]; then
  sed -i -r -e "s|=INFO, stdout|=$LOG_LEVEL, stdout|g" $CONNECT_LOG_CFG
  sed -i -r -e "s|^(log4j.appender.stdout.threshold)=.*|\1=${LOG_LEVEL}|g" $CONNECT_LOG_CFG
fi

# Extend CLASSPATH for custom connectors
export CLASSPATH=${CLASSPATH}:${KAFKA_HOME}/connectors/*

# Configure properties
echo -e "\n" >> $CONNECT_CFG
for VAR in `env`
do
  if [[ $VAR =~ ^CONNECT_ && ! $VAR =~ ^CONNECT_CFG && ! $VAR =~ ^CONNECT_BIN ]]; then
    connect_name=`echo "$VAR" | sed -r "s/CONNECT_(.*)=.*/\1/g" | tr '[:upper:]' '[:lower:]' | tr _ .`
    env_var=`echo "$VAR" | sed -r "s/(.*)=.*/\1/g"`
    if egrep -q "(^|^#)$connect_name=" $CONNECT_CFG; then
        sed -r -i "s@(^|^#)($connect_name)=(.*)@\2=${!env_var}@g" $CONNECT_CFG
    else
        echo "$connect_name=${!env_var}" >> $CONNECT_CFG
    fi
  fi
done

exec "$@"
