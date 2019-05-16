# WebPack DevServer

The following command will start the [WebPack DevServer][].

        ./gradlew yarn_run_dev

You should not normally need to modify the WebPack configuration. However, you will need to make the following modifications if you want to develop with the WebPack DevServer
using SSL.

* Edit [vue.config.js][] and change the devServer.proxy.target entries from *http* to *https* and *ws* to *wss*.

[vue.config.js]: ../src/main/webapp/vue.config.js
[WebPack DevServer]: https://webpack.js.org/configuration/dev-server/
