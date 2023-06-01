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

FROM gradle:7.6.1-jdk11-alpine AS builder

WORKDIR /work

COPY . /work
RUN ["gradle", "clean", "build"]

FROM alpine AS kafka_deps

ARG SCALA_VERSION="2.12"
ARG KAFKA_VERSION="2.8.0"
ENV SCALA_VERSION=$SCALA_VERSION \
    KAFKA_VERSION=$KAFKA_VERSION
ENV KAFKA_HOME=/opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION}

ARG KAFKA_DIST=kafka_${SCALA_VERSION}-${KAFKA_VERSION}
ARG KAFKA_DIST_TGZ=${KAFKA_DIST}.tgz
ARG KAFKA_DIST_ASC=${KAFKA_DIST}.tgz.asc

RUN set -x && \
    apk add --no-cache unzip curl ca-certificates gnupg jq && \
    eval $(gpg-agent --daemon) && \
    curl -sSLO "https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/${KAFKA_DIST_TGZ}" && \
    curl -sSLO https://dist.apache.org/repos/dist/release/kafka/${KAFKA_VERSION}/${KAFKA_DIST_ASC} && \
    curl -sSL  https://kafka.apache.org/KEYS | gpg -q --import - && \
    mkdir -p /opt && \
    mv ${KAFKA_DIST_TGZ} /tmp && \
    tar xfz /tmp/${KAFKA_DIST_TGZ} -C /opt && \
    rm /tmp/${KAFKA_DIST_TGZ} && \
    apk del unzip ca-certificates gnupg

FROM adoptopenjdk/openjdk11:jre-11.0.11_9-alpine

ARG SCALA_VERSION="2.12"
ARG KAFKA_VERSION="2.8.0"
ENV SCALA_VERSION=$SCALA_VERSION \
    KAFKA_VERSION=$KAFKA_VERSION

COPY --from=kafka_deps /opt /opt

RUN apk add --no-cache bash

ENV KAFKA_HOME=/opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION}

WORKDIR $KAFKA_HOME

ENV PATH=$PATH:/${KAFKA_HOME}/bin \
    CONNECT_CFG=${KAFKA_HOME}/config/connect-distributed.properties \
    CONNECT_BIN=${KAFKA_HOME}/bin/connect-distributed.sh \
    CONNECT_LOG_CFG=$KAFKA_HOME/config/log4j.properties

ENV CONNECT_PORT=8083
ENV EXTRA_ARGS="-javaagent:$KAFKA_HOME/connectors/jmx_prometheus_javaagent-0.12.0.jar=9999:${KAFKA_HOME}/config/prometheus.yml"

EXPOSE 9999
EXPOSE ${CONNECT_PORT}

COPY prometheus-agent.yml ${KAFKA_HOME}/config/prometheus.yml
COPY start-connect.sh $KAFKA_HOME/start-connect.sh

COPY setup.sh $KAFKA_HOME/setup.sh
COPY generate_data.sh $KAFKA_HOME/generate_data.sh
COPY application.conf $KAFKA_HOME/application.conf

COPY docker-entrypoint.sh /
RUN mkdir -p $KAFKA_HOME/connectors

COPY --from=builder /work/streaming-connect-sink/build/libs/jmx_prometheus_javaagent-*.jar $KAFKA_HOME/connectors
COPY --from=builder /work/streaming-connect-sink/build/libs/streaming-connect-sink-*.jar $KAFKA_HOME/connectors

ENTRYPOINT ["/docker-entrypoint.sh"]

CMD ["./start-connect.sh"]
