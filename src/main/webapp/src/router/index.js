// @flow

import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/components/login/Login';
import UserMaintenance from '@/components/usermaint/UserMaintenance';
import ModelView from '@/components/model/ModelView';
import {
  store,
  vxcFp,
  USER_VXC as UC,
} from '@/store';
import { logout } from '@/components/login/dataSourceProxy';

Vue.use(Router);

export const paths = {
  LOGIN: '/',
  HOME: '/ModelView',
  MODEL_VIEW: '/ModelView',
  USER_MAINT: '/UserMaintenance',
};

export const router = new Router({
  mode: 'history',
  routes: [
    {
      path: paths.LOGIN,
      component: Login,
    },
    {
      path: paths.HOME,
      component: ModelView,
    },
    {
      path: paths.MODEL_VIEW,
      component: ModelView,
    },
    {
      path: paths.USER_MAINT,
      component: UserMaintenance,
    },
  ],
});

const isLoggedIn = () => store.getters[vxcFp(UC, UC.GET.USER)].isLoggedIn;

router.beforeEach(async (to, from, next) => {
  if (to.path !== paths.LOGIN && !isLoggedIn()) {
    next({ path: paths.LOGIN });
  } else if (to.path === paths.LOGIN && isLoggedIn()) {
    await logout();
    next();
  } else {
    next();
  }
});

store.watch(isLoggedIn, (loggedIn) => {
  if (!loggedIn) {
    router.push(paths.LOGIN);
  }
});

export default router;
