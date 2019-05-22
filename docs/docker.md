# Docker

## Installation

1. [Install Docker][]
1. [Install Docker Compose][] if not already installed during the Docker installation.

## Running the MySQL Container

Start the MySQL container:

    docker-compose -f src/main/docker/mysql.yml up -d

Stop the MySQL container:

    docker-compose -f src/main/docker/mysql.yml down

The first time you run the *up* command, the MySQL image will be downloaded and installed. Subsequent start-ups will be much faster. The *-d* option will run the container in the background.

## Running the praise-wm Container

Start the praise-wm container:

    docker-compose -f src/main/docker/praisewm.yml up -d

Stop the praise-wm container:

    docker-compose -f src/main/docker/praisewm.yml down

The first time you run the *up* command it will download the Docker images and install them in your local Docker cache.

A new praise-wm Docker image is created automatically by the [praise-wm GitLab Pipeline][] each time files are pushed into the repository. To get the most up-to-date version from the repository, you must run the following command which will download the most up-to-date version if it differs from what is in your local Docker cache.

    docker-compose -f src/main/docker/praisewm.yml pull

#### Environmental Variable Overrides for praisewm.yml
[praisewm.yml][] supports the following environmental variables that you can set prior to starting the container:

    PRAISEWM_IMAGE_NAME - Override the repository path to the praise-wm image

The default uses the image created for the main branch. To use an image from an alternate branch, set PRAISEWM_IMAGE_NAME to the default path in [praisewm.yml][] substituting the name of the branch for the *latest* tag.

    PRAISEWM_DATA_DIRECTORY - Override the host directory setting that will be used by the container.

By default, the praise-wm docker container will use *~/praisewm/data*  on the host system for data files. The server will also write its log files to a *logs* subdirectory. To change the mount point, set the environmental variable *PRAISEWM_DATA_DIRECTORY* to a complete path to a directory on your system. If the directory does not yet exist, it will be created on startup.

See [praisewm.yml][] for details and an example of setting PRAISEWM_DATA_DIRECTORY on Windows 10.

## Starting/Stopping All Containers with a single command.

    docker-compose -f src/main/docker/app.yml up -d
    docker-compose -f src/main/docker/app.yml down

If the containers do not seem to start correctly, run the *up* command without the *-d* option so that you can see the program initialization output. If you see that *MySQL* is still initializing when the *praise-wm* application starts, increase the *PRAISEWM_SLEEP* time in [app.yml].

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

[praise-wm GitLab Pipeline]: ../.gitlab-ci.yml
[Install Docker]: https://docs.docker.com/engine/installation/
[Install Docker Compose]: https://docs.docker.com/compose/install/
[app.yml]: ../src/main/docker/app.yml
[praisewm.yml]: ../src/main/docker/praisewm.yml
[uploadImage.sh]: ../src/main/docker/uploadImage.sh
[A Docker Cheat Sheet]: https://www.digitalocean.com/community/tutorials/how-to-remove-docker-images-containers-and-volumes
