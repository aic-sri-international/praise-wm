# WebPack DevServer

The following command will start the [WebPack DevServer][].

        ./gradlew yarn_run_dev

You should not normally need to modify the WebPack configuration files. However, you will need to make the following modifications if you want to develop with the WebPack DevServer
using SSL.

* Edit [webapp/config/index.js][] and change the dev.proxyTable entries from *http* to *https* and *ws* to *wss*.

[webapp/config/index.js]: ../src/main/webapp/config/index.js
[WebPack DevServer]: https://webpack.js.org/configuration/dev-server/