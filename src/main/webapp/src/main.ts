import Vue from 'vue';
import '@/fontawesome_init';
import { store } from '@/store';
import App from '@/App.vue';
import router from '@/router';
import '@/vue_instance_properties';
import '@/vue_directives';
import '@/services/ws_notifications/notifications';
import '@/global.scss';

Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: h => h(App),
}).$mount('#app');
