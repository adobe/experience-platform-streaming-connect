FROM anapsix/alpine-java:jdk8

ENV SCALA_VERSION="2.12" \
    KAFKA_VERSION="2.3.0"
ENV KAFKA_HOME=/opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION}

ARG KAFKA_DIST=kafka_${SCALA_VERSION}-${KAFKA_VERSION}
ARG KAFKA_DIST_TGZ=${KAFKA_DIST}.tgz
ARG KAFKA_DIST_ASC=${KAFKA_DIST}.tgz.asc

RUN set -x && \
    apk add --no-cache unzip curl ca-certificates gnupg jq && \
    eval $(gpg-agent --daemon) && \
    MIRROR=`curl -sSL https://www.apache.org/dyn/closer.cgi\?as_json\=1 | jq -r '.preferred'` && \
    curl -sSLO "${MIRROR}kafka/${KAFKA_VERSION}/${KAFKA_DIST_TGZ}" && \
    curl -sSLO https://dist.apache.org/repos/dist/release/kafka/${KAFKA_VERSION}/${KAFKA_DIST_ASC} && \
    curl -sSL  https://kafka.apache.org/KEYS | gpg -q --import - && \
    gpg -q --verify ${KAFKA_DIST_ASC} && \
    mkdir -p /opt && \
    mv ${KAFKA_DIST_TGZ} /tmp && \
    tar xfz /tmp/${KAFKA_DIST_TGZ} -C /opt && \
    rm /tmp/${KAFKA_DIST_TGZ} && \
    apk del unzip curl ca-certificates gnupg

ENV PATH=$PATH:/${KAFKA_HOME}/bin \
    CONNECT_CFG=${KAFKA_HOME}/config/connect-distributed.properties \
    CONNECT_BIN=${KAFKA_HOME}/bin/connect-distributed.sh

ENV CONNECT_PORT=8083
ENV EXTRA_ARGS="-javaagent:$KAFKA_HOME/connectors/jmx_prometheus_javaagent-0.12.0.jar=9999:${KAFKA_HOME}/config/prometheus.yml"

EXPOSE 9999
EXPOSE ${CONNECT_PORT}

WORKDIR $KAFKA_HOME
COPY prometheus-agent.yml ${KAFKA_HOME}/config/prometheus.yml
COPY start-connect.sh $KAFKA_HOME/start-connect.sh
COPY docker-entrypoint.sh /
RUN mkdir -p $KAFKA_HOME/connectors

COPY streaming-connect-sink/build/libs/streaming-connect-sink.jar $KAFKA_HOME/connectors
COPY streaming-connect-sink/build/dependencies $KAFKA_HOME/connectors
ENTRYPOINT ["/docker-entrypoint.sh"]

CMD ["./start-connect.sh"]
