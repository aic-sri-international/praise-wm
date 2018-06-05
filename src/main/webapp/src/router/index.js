// @flow

import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/components/login/Login';
import UserMaintenance from '@/components/usermaint/UserMaintenance';
import ModelEditor from '@/components/editor/ModelEditor';

Vue.use(Router);

// const Home = {
//   template: '<div><h1>Welcome To praisewm !</h1></div>',
//   name: 'home',
// };

export const paths = {
  LOGIN: '/',
  HOME: '/ModelEditor',
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
      component: ModelEditor,
    },
    {
      path: paths.USER_MAINT,
      component: UserMaintenance,
    },
  ],
});
