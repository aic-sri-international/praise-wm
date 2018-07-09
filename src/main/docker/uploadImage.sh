#!/bin/bash

# Need to login with Docker login  worldmodelers.cse.sri.com and use your LDAP username and docker generated API key

# image is create via './gradlew buildDocker' command as 'praisewm:latest', tag the image as below,
# using appropriate version number, then use the following command to push it into the repo.

docker push worldmodelers.cse.sri.com/praise-wm:2.3

