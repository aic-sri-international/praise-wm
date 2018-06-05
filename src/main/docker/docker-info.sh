#!/usr/bin/env bash

# List all running containers with CPU, Memory, Networking I/O and Block I/O stats.

docker container stats $(docker container ps --format={{.Names}})