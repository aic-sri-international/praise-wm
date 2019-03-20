FROM openjdk:11.0.2-jre-slim

# Note that with adoptopenjdk/openjdk11:jdk-11.0.2.9-slim you will get an NPE when JFreeChart
# makes a call that leads to sun.awt.FontConfiguration.getVersion(FontConfiguration.java:1264)
# See: https://github.com/docker-library/openjdk/issues/73

# Related to same issue as above; edit accessibility.properties to eliminate
# 'Assistive Technology not found' error.
# See https://askubuntu.com/questions/695560/assistive-technology-not-found-awterror

RUN sed -i 's/^assistive_technologies=/#&/' /etc/java-*-openjdk/accessibility.properties

ENV PRAISEWM_SLEEP=0 \
    JAVA_OPTS="-Xms500m -Xmx1g -Djava.awt.headless=true"

WORKDIR /opt/praisewm

COPY praisewm*.jar praisewm.jar
COPY keystore.jks keystore.jks
COPY application.properties application.properties
COPY logback.xml logback.xml

EXPOSE 4567
CMD echo "The application will start in ${PRAISEWM_SLEEP}seconds..." && \
    sleep ${PRAISEWM_SLEEP} && \
    java ${JAVA_OPTS} -jar praisewm.jar
