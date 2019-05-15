import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/components/login/Login.vue';
import ModelView from '@/components/model/ModelView.vue';
import paths from './paths';
import { logout } from '@/components/login/dataSourceProxy';
import { store } from '@/store';
import { VuexUserState } from '@/store/user/types';

Vue.use(Router);

const router = new Router({
  mode: 'history',
  base: process.env.BASE_URL,
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
      // route level code-splitting
      // this generates a separate chunk (UserMaintenance.[hash].js) for this route which is
      // lazy-loaded when the route is visited.
      component: () => import(/* webpackChunkName: "UserMaintenance" */'@/components/usermaint/UserMaintenance.vue'),
    },
  ],
});

const isLoggedIn = () => ((store.state as any).user as VuexUserState).isLoggedIn;

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
