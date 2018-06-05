FROM openjdk:8-jre-alpine

ENV PRAISEWM_SLEEP=0 \
    JAVA_OPTS=""

COPY praisewm*.jar /praisewm.jar
COPY keystore.jks /

WORKDIR /work

EXPOSE 4567 4568
CMD echo "The application will start in ${PRAISEWM_SLEEP}seconds..." && \
    sleep ${PRAISEWM_SLEEP} && \
    java ${JAVA_OPTS} -jar /praisewm.jar
