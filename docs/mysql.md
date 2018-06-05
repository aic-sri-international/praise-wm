# MySQL


You can either install and run MySQL in your host operating system or within a Docker container. Using Docker can simplify your setup.

* [With Docker](docker.md)


* [Without Docker](#installing-mysql-without-docker)


## Installing MySQL without Docker
1. Install [MySQL Community Server][]

    **Windows:** *If you do not see a 'Next' option on the server installation/configuration, change your Windows text from 125% (medium) to 100% (smaller)*

1. Create a MySQL database account with DBA privileges.

    The user/password for the account must match those found in [com.praisewm.cfg][].



[MySQL Community Server]: https://dev.mysql.com/downloads/
[com.praisewm.cfg]: ../src/main/resources/com.praisewm.cfg
