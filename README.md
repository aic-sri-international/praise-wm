# praise-wm

## Table of Contents

1.  [Preface](#preface)
1.  [Setup](#setup)
1.  [Development](#development)
1.  [Gradle Tasks](#gradle-tasks)
1.  [Build Warnings](#build-warnings)
1.  [Docker Deployment](docs/docker.md)
1.  [Version Updates](#version-updates)
1.  [Joint Development with AIC Dependencies](#joint-development-with-aic-dependencies)

## Preface
This document covers the praise-wm project setup and its development tools.

[Gradle][] is used as the build tool and is pre-installed within the project.

The command used to run gradle will differ depending on the O/S.

    MacOS or Linux:  ./gradlew
    Windows: ./gradlew.bat

All examples will use the shorter of the two.

Areas that are specific to an O/S will be listed in *italics* and prefaced by the name of the O/S.

*Windows*  **Permissions Issues**

If using Windows 10, unless running as Administrator, you may encounter errors during a Gradle setup task.

You can get around this problem by running IntelliJ IDEA as administrator and running the gradle tasks from an IntelliJ terminal console:

To start a terminal console from within IntelliJ: View | Tool Windows | Terminal


## Setup

1.  Run the following from the command line

        ./gradlew yarnSetup yarn

1.  (Optional) Run the following command to convert all the markdown documents to HTML to make it easier to read the instructions.

        ./gradlew yarn_readme

    The above command will generate HTML from this and related markdown files and then launch the documentation in your default browser.

    *Windows*: If the browser does not launch, open project-root/build/md-docs/README.html.


1. [MySql](docs/mysql.md)
1. Start MySQL
1. [SSL Configuration](docs/ssl.md)
1.  Run the following from the command line

        ./gradlew yarnSetup yarn deploy
1.  Start the praise-wm application

        java -jar build/libs/praisewm-2.5-all.jar

1. Access the praise-wm application using a browser

        https://localhost:4567

    The following processing takes place when the gradle tasks are run:

    1. **yarnSetup** will download and install [Node.js](https://nodejs.org/en/) and [Yarn](https://yarnpkg.com/en/) into your project. As a final step in the node installation, a directory called *node* will be created within your project's home directory that contains a link to the node executable. The directory should never be checked into the version control repository.
    1. **yarn** will download and install all project-specific 3rd party client libraries
    1. **deploy** will perform a clean build, tests the praise-wm application, and creates the build/libs/praisewm-\<version\>-all.jar shadow jar.

1. [IDE Configuration](docs/IntelliJ_IDEA.md)

## Development
[Client Toolset](docs/client_toolset.md)
[Server Toolset](docs/server_toolset.md)

The [Developer Test Panel](docs/developer_test_panel.md) can be used to test notifications and other messages pushed to the client. 

The [WebPack DevServer](docs/webpack_dev_server.md) configuration will need a minor adjustment to run praise-wm under SSL.

JavaScript uses the [Airbnb Javascript Style Guide][] and the [Vue.js Style Guide][].

Java uses the [Google Java Style Guide][].

DateTime should typically be represented throughout the server application as instances of java.time.Instant.

The client code is located under directory src/main/webapp

A curated list of [Vue resources][].

#### Gradle Tasks

**Common Tasks**

1. **run** - Prepares server code as required, then runs the application
1. **yarn_run_dev** -  Runs the client application with hot reload at localhost:8080
    This requires that the java application is already running, unless the java server is not being accessed.
1. **yarn_run_unit_tests** - Runs the client unit tests
1. **test** - Runs server unit tests
1.  **deploy** -
    1. Deletes the build directory
    1. Runs client unit tests
    1. Packages the client artifacts
    1. Runs flywayMigrate
    1. Creates JOOQ classes
    1. Compiles Java classes and process resources
    1. Builds the shadow jar *build/libs/praisewm-\<version\>-all.jar*
    1. Runs server unit tests
1. **flywayClean** - Drops all objects in the configured database schemas. Run if needed due to developer modifications to existing flyway scripts.
1. **clean** - Deletes the java build directory
1. **javadoc** - Generates JavaDoc HTML in the *build/docs/javadoc* directory.

Run the following to display a list of all gradle tasks:

    ./gradlew tasks

 **Note** that the following *Rules* displayed at the bottom of the list when the command is run are unfortunate artifacts from one of the plugins and should **never** be used

    Pattern: "npm_<command>": Executes an NPM command.
    Pattern: "yarn_<command>": Executes an Yarn command.

#### Build Warnings

##### Yarn

The application uses Bootstrap-vue which uses CSS from bootstrap, however, it does not use jquery and it does not use popper.js via bootstrap.

    warning " > bootstrap@4.3.1" has unmet peer dependency "jquery@1.9.1 - 3".
    warning " > bootstrap@4.3.1" has unmet peer dependency "popper.js@^1.14.7".

The build uses the vetted versions of the following dependencies as published by vue-cli so that Vue and its dependencies will function correctly. The warnings should be eliminate after upgrading with a subsequent release of vue-cli.

    warning "@vue/cli-plugin-unit-jest > vue-jest@3.0.4" has unmet peer dependency "babel-core@^6.25.0 || ^7.0.0-0".
    warning "@vue/eslint-config-airbnb > eslint-import-resolver-webpack@0.10.1" has unmet peer dependency "webpack@>=1.11.0".
    warning " > sass-loader@7.1.0" has unmet peer dependency "webpack@^3.0.0 || ^4.0.0".
    warning " > ts-jest@23.10.5" has unmet peer dependency "jest@>=22 <24".

Warnings about the size of various assets will be displayed during a build deployment.

When running the jest unit tests you will see the following warning:

    console.warn node_modules/bootstrap-vue/dist/bootstrap-vue.common.js:80
    [BootstrapVue warn]: Multiple instances of Vue detected!
    See: https://bootstrap-vue.js.org/docs#using-module-bundlers 
    
It should be fixed in a [subsequent release of Bootstap-Vue][]. The version set within package.json for Bootstrap-vue is currently pinned to an earlier version due to a TypeScript bug contained in the next release. The rc.22 release should fix both the above warning and the TypeScript bug.

##### jOOQ Schema Build

JOOQ's schema generator has a dependency on jaxb. The following problem should be resolved once a production version of jaxb-impl-2.4 is 
released, and JOOQ has upgraded their generator to use the new release.

    WARNING: Illegal reflective access by com.sun.xml.bind.v2.runtime.reflect.opt.Injector  on jaxb-impl-2.3.0.1.jar

##### Gradle

The yarn task from com.moowork.gradle:gradle-node-plugin is removed and replaced with a custom yarn task. Gradle has deprecated the task removal feature, which causes the following warning. A different approach will need to be implemented before upgrading to Gradle 6.

    Deprecated Gradle features were used in this build, making it incompatible with Gradle 6.0.
    
## Version Updates

 Update the appropriate property in [gradle.properties][] and run its associated task:

| Product | Property  | Command line |
| -------- | ---------- | --------- |
| gradle   | gradle_version | ./gradlew wrapper |
| node     | gradle_node_plugin_node_version | ./gradlew nodeSetup |
| yarn     | gradle_node_plugin_yarn_version | ./gradlew yarnSetup |


## Joint Development with AIC Dependencies

If you are also doing local builds of the AIC library dependencies, you will probably want to change the repository search order so that your local maven repository will be searched prior to the deployed [AIC library repository][]. To change the search order, edit the repositories section towards the bottom of [build.gradle][] as noted in its comments.


[Gradle]: https://gradle.org/
[Airbnb Javascript Style Guide]: https://github.com/airbnb/javascript
[Vue.js Style Guide]: https://vuejs.org/v2/style-guide/
[Google Java Style Guide]: https://google.github.io/styleguide/javaguide.html
[Vue resources]: https://github.com/vuejs/awesome-vue
[gradle.properties]: ./gradle.properties
[AIC library repository]: https://oss.sonatype.org/content/repositories/snapshots/
[build.gradle]: ./build.gradle
[subsequent release of Bootstap-Vue]: https://github.com/bootstrap-vue/bootstrap-vue/issues/3303
