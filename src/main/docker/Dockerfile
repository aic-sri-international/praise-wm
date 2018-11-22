FROM openjdk:8-jre-slim

# Switched from 8-jre-alpine to 8-jre-slim to fix NPE when JFreeChart
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

EXPOSE 4567 4568
CMD echo "The application will start in ${PRAISEWM_SLEEP}seconds..." && \
    sleep ${PRAISEWM_SLEEP} && \
    java ${JAVA_OPTS} -jar praisewm.jar
