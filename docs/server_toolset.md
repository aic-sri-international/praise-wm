# Server Toolset

* [Spark][] Is used for the REST framework.

* [Guava][] Google Core Libraries for Java.

* [Commons Lang3][] Helper utilities for the java.lang API.

* [JsonPath][] Java JsonPath implementation.

* [MySQL Community Server][] for the database server.

     Database temporal columns should be defined using the MySQL DATETIME type and represent UTC.

* [Flyway][] is used for database migration.

     The migration files are located within the src/resources/db/migration folder.

* [HikariCP][] for the database connection pool.

* [JOOQ][] for the database abstaction layer.

    The build system is configured so that the database Timestamp type is represented as java.time.Instant in the JOOQ generated files.

* [MapStruct][] for conversion between JOOQ POJOs and developer defined DTOs.

* [Jackson][]  for JSON conversion.

    All conversions should typically be done using the com.praisewm.util.JsonConverter to assure expected conversion behavior throughout the application. For example, conversion support between java.time.Instant and java.lang.String.

* [Logback][] logging API that natively implements the SLF4J API.

* [Google's Java Formatting tool][] for code formatting.

    If using IntelliJ, the IntelliJ options for *Optimize Imports* and *Rearrange entries* need to be checked, however, the *Commit Default* options need to be unchecked.

    The formatter will be used when you select the *Reformat Code* right-mouse-click menu option from the Project tab panel. The IntelliJ Java Code Style settings are used if you've checked the *Before Commit* options. Although there is a [Google Style](https://github.com/google/styleguide) for IntelliJ whose modifications are very close to those done by the formatter, they are not identical. The formatter has the advantage of being able to be run stand-alone, so it can be integrated into CI.

* [JUnit][] Unit testing framework

* [AssertJ][] Assertions library for unit testing

[Spark]: http://sparkjava.com/
[Guava]: https://github.com/google/guava
[Commons Lang3]: https://commons.apache.org/proper/commons-lang/
[JsonPath]: https://github.com/json-path/JsonPath/
[MySQL Community Server]: https://dev.mysql.com/downloads/
[Flyway]: https://flywaydb.org/documentation/gradle/
[HikariCP]: https://github.com/brettwooldridge/HikariCP
[JOOQ]: https://www.jooq.org/learn/
[MapStruct]: http://mapstruct.org/
[Jackson]: https://github.com/FasterXML/jackson
[Logback]: https://logback.qos.ch/
[Google's Java Formatting tool]: https://github.com/google/google-java-format
[JUnit]: http://junit.org/junit4/
[AssertJ]: http://joel-costigliola.github.io/assertj/index.html
