import mutations from './mutations';
import { VuexNotificationsState } from './types';
import { Module } from 'vuex';
import { RootState } from '@/store/types';

const state: VuexNotificationsState = {
  showNotificationsUi: false,
  // true if a message has arrived and the notification window is not open
  notificationUiHasNewMsg: false,
  notificationsForUi: [],
  httpErrors: [],
};

export const module: Module<VuexNotificationsState, RootState> = {
  namespaced: true,
  state,
  mutations,
};

export default module;
