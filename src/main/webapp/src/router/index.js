// @flow

import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/components/login/Login';
import UserMaintenance from '@/components/usermaint/UserMaintenance';
import ModelEditor from '@/components/editor/ModelEditor';
import SegmentedModelEditor from '@/components/editor/SegmentedModelEditor';

Vue.use(Router);

// const Home = {
//   template: '<div><h1>Welcome To praisewm !</h1></div>',
//   name: 'home',
// };

export const paths = {
  LOGIN: '/',
  HOME: '/SegmentedModelEditor',
  MODEL_EDITOR: '/ModelEditor',
  SEGMENTED_MODEL_EDITOR: '/SegmentedModelEditor',
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
      component: SegmentedModelEditor,
    },
    {
      path: paths.SEGMENTED_MODEL_EDITOR,
      component: SegmentedModelEditor,
    },
    {
      path: paths.MODEL_EDITOR,
      component: ModelEditor,
    },
    {
      path: paths.USER_MAINT,
      component: UserMaintenance,
    },
  ],
});
