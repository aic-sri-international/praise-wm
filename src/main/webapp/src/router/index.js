// @flow

import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/components/login/Login';
import UserMaintenance from '@/components/usermaint/UserMaintenance';
// import ModelEditor from '@/components/editor/ModelEditor';
import SegmentedModelView from '@/components/editor/SegmentedModelView';

Vue.use(Router);

// const Home = {
//   template: '<div><h1>Welcome To praisewm !</h1></div>',
//   name: 'home',
// };

export const paths = {
  LOGIN: '/',
  HOME: '/SegmentedModelView',
  // MODEL_EDITOR: '/ModelEditor',
  SEGMENTED_MODEL_VIEW: '/SegmentedModelView',
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
      component: SegmentedModelView,
    },
    {
      path: paths.SEGMENTED_MODEL_VIEW,
      component: SegmentedModelView,
    },
    // {
    //   path: paths.MODEL_EDITOR,
    //   component: ModelEditor,
    // },
    {
      path: paths.USER_MAINT,
      component: UserMaintenance,
    },
  ],
});
