#!/usr/bin/env bash

# Get the parent image id for a child image

CHILD=$1

if [ -z $CHILD ]; then
 echo Arg1 must = docker image id
 exit 1
fi

docker inspect --format='{{.Id}} {{.Parent}}' \
    $(docker images --filter since=${CHILD} --quiet)