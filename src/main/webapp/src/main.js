// @flow

// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import '@/fontawesome_init';
import { store } from '@/store';
import App from '@/App';
import { router } from '@/router';
import '@/vue_instance_properties';
import '@/vue_directives';
import '@/services/ws_notifications/notifications';
import '@/global.css';

Vue.config.productionTip = false;

// eslint-disable-next-line no-new
new Vue({
  el: '#app',
  router,
  store,
  template: '<App/>',
  components: { App },
});

