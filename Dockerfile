FROM registry.gitlab.com/clarin-eric/docker-alpine-supervisor-java-base:openjdk11-2.2.0 AS build

RUN apk --no-cache add maven

# install OAI Harvester
# Fetch and unpack the OAI Harvester
WORKDIR /tmp/oai
RUN curl -L -o /tmp/oai-harvest-manager.tar.gz https://github.com/clarin-eric/harvest-manager/releases/download/v2.0-RC2/harvest-manager-2.0-RC2-SNAPSHOT.fb13eb.tar.gz && \
    tar -xzf /tmp/oai-harvest-manager.tar.gz

# build our own code first
COPY . /tmp
RUN cd /tmp && \
    mvn clean package

WORKDIR /tmp/oai
RUN tar -xzf /tmp/target/harvest-manager-*.tar.gz && \
    cp /tmp/target/harvest-manager-*.jar /tmp/oai/lib/

### Package stage

FROM registry.gitlab.com/clarin-eric/docker-alpine-supervisor-java-base:openjdk11-1.2.12

# app workdir
RUN mkdir -p /app/workdir &&\
    mkdir -p /app/oai
WORKDIR /app/oai

COPY --from=build /tmp/oai /app/oai

ENTRYPOINT ["/app/oai/run-harvester.sh"]

# cleanup
RUN rm -rf /var/lib/apt/lists/* && \
    rm -rf /tmp/*
