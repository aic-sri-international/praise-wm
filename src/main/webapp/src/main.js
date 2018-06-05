// @flow

// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import '@/fontawesome_init';
import { store, newWatchLogin } from '@/store';
import App from '@/App';
import { router, paths } from '@/router';
import '@/vue_instance_properties';
import '@/vue_directives';
import '@/services/ws_notifications/notifications';
import '@/global.css';

Vue.config.productionTip = false;

router.beforeEach((to, from, next) => {
  if (to.path !== paths.LOGIN && !store.state.user.isLoggedIn) {
    next({ path: paths.LOGIN });
  } else {
    next();
  }
});

// eslint-disable-next-line no-new
new Vue({
  el: '#app',
  router,
  store,
  template: '<App/>',
  components: { App },
});

newWatchLogin((isLoggedIn) => {
  if (!isLoggedIn) {
    router.push(paths.LOGIN);
  }
});
