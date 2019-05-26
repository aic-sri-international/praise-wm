import Vue from 'vue';
import '@/styles/application.scss';
import '@/store/storeConfig';
import '@/fontawesome_init';
import App from '@/App.vue';
import router from '@/router';
import '@/vue_instance_properties';
import '@/vue_directives';
import '@/services/ws_notifications/notifications';
import { getStore } from '@/store/store';

Vue.config.productionTip = false;


new Vue({
  router,
  store: getStore(),
  render: h => h(App),
}).$mount('#app');
