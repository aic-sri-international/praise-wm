FROM openjdk:8-jre-slim

ENV PRAISEWM_SLEEP=0 \
    JAVA_OPTS="-Xms500m -Xmx1g"

WORKDIR /opt/praisewm

COPY praisewm*.jar praisewm.jar
COPY keystore.jks keystore.jks

EXPOSE 4567 4568
CMD echo "The application will start in ${PRAISEWM_SLEEP}seconds..." && \
    sleep ${PRAISEWM_SLEEP} && \
    java ${JAVA_OPTS} -jar praisewm.jar
