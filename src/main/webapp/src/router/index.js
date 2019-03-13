// @flow

import Vue from 'vue';
import Router from 'vue-router';
import Login from '@/components/login/Login';
import UserMaintenance from '@/components/usermaint/UserMaintenance';
// import ModelEditor from '@/components/model/editor/OldModelEditor';
import SegmentedModelView from '@/components/model/ModelView';

Vue.use(Router);

export const paths = {
  LOGIN: '/',
  HOME: '/SegmentedModelView',
  // OLD_MODEL_EDITOR: '/OldModelEditor',
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
      component: SegmentedModelView,
    },
    {
      path: paths.MODEL_VIEW,
      component: SegmentedModelView,
    },
    // {
    //   path: paths.OLD_MODEL_EDITOR,
    //   component: OldModelEditor,
    // },
    {
      path: paths.USER_MAINT,
      component: UserMaintenance,
    },
  ],
});
