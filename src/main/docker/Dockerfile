FROM openjdk:8-jre-alpine

ENV PRAISEWM_SLEEP=0 \
    JAVA_OPTS=""

WORKDIR /opt/praisewm

COPY praisewm*.jar praisewm.jar
COPY keystore.jks keystore.jks

RUN mkdir -p /opt/praisewm/data/models
COPY data/models data/models

RUN ls -l /opt/praisewm/data/models

EXPOSE 4567 4568
CMD echo "The application will start in ${PRAISEWM_SLEEP}seconds..." && \
    sleep ${PRAISEWM_SLEEP} && \
    java ${JAVA_OPTS} -jar praisewm.jar
