# Docker

## Installation

1. [Install Docker][]
1. [Install Docker Compose][]

`Windows:` *For all Docker commands in the following sections: Do not use cygwin, powershell or the command prompt. Use the Docker Quickstart Terminal.*

## Running the MySQL Container

Start the MySQL container:

    docker-compose -f src/main/docker/mysql.yml up -d

Stop the MySQL container:

    docker-compose -f src/main/docker/mysql.yml down

The first time you run the *up* command, the MySQL image will be downloaded and installed. Subsequent start-ups will be much faster. The *-d* option will run the container in the background.

## Running the praisewm Container

To run praisewm in a Docker container, you must first build a Docker image that contains the praisewm application. The command will perform a clean build of the application and then install a Docker image.

The following command must be run from the command line and MySQL must already be running. Running it from without IntelliJ as a Gradle task will not work.

        ./gradlew buildDocker

Start the praisewm container:

    docker-compose -f src/main/docker/praisewm.yml up -d

Stop the praisewm container:

    docker-compose -f src/main/docker/praisewm.yml down

Unless you already have the openjdk Docker image installed, it will be downloaded and installed the first time you run the *up* command. Subsequent start-ups will be faster. The *-d* option will run the container in the background.

## Starting/Stopping All Containers with a single command.

    docker-compose -f src/main/docker/app.yml up -d
    docker-compose -f src/main/docker/app.yml down

If the containers do not seem to start correctly, run the *up* command without the *-d* option so that you can see the program initialization output. If you see that *MySQL* is still initializing when the *praisewm* application starts, increase the *PRAISEWM_SLEEP* time in [app.yml].

`NOTE`: By default, the praisewm docker container will create a mount point of *~/praisewm/data* that is used by the praisewm server for access to external data files. The server will also write its log files to a *logs* subdirectory. To change the mount point, set the environmental variable *PRAISEWM_DATA_DIRECTORY* to a complete path to a directory on your system. If the directory does not yet exist, it will be created on startup.


## Using Deployed Docker Images.

If using a docker image of praisewm that has been deployed to a remote repository, be sure to set the environmental variable *PRAISEWM_IMAGE_NAME* to the correct image name prior to running [praisewm.yml].

See [uploadImage.sh] for the image name used for Artifactory.


## Docker Commands
List all containers:

    docker ps -a

Get container statistics:

    docker stats

Formatted container statistics:

    docker stats $(docker container ps --format={{.Names}})

Stop a specific container:

    docker stop <container_id | container_name>

Delete a container:

    docker rm <container_id>

[A Docker Cheat Sheet][]

[Install Docker]: https://docs.docker.com/engine/installation/
[Install Docker Compose]: https://docs.docker.com/compose/install/
[app.yml]: ../src/main/docker/app.yml
[praisewm.yml]: ../src/main/docker/praisewm.yml
[uploadImage.sh]: ../src/main/docker/uploadImage.sh
[A Docker Cheat Sheet]: https://www.digitalocean.com/community/tutorials/how-to-remove-docker-images-containers-and-volumes