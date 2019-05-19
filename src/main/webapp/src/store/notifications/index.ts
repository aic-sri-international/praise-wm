import mutations from './mutations';
import {
  HttpError,
  NotificationMessage,
  VuexNotificationsState,
} from './types';
import { Module } from 'vuex';
import { RootState } from '@/store/types';

const getters = {
  httpErrors:
      (state: VuexNotificationsState): HttpError[] => state.httpErrors,
  notificationsForUi:
      (state: VuexNotificationsState): NotificationMessage[] => state.forUi,
  notificationForServer:
      (state: VuexNotificationsState) => state.forServer,
  notificationUiIsOpen:
      (state: VuexNotificationsState): boolean => state.ui.isOpen,
  notificationUiHasNewMsg:
      (state: VuexNotificationsState): boolean => state.ui.hasNewMsg,
};

const state: VuexNotificationsState = {
  ui: {
    // true if the notification window is open
    isOpen: false,
    // true if a message has arrived and the notification window is not open
    hasNewMsg: false,
  },
  forUi: [],
  forServer: [],
  httpErrors: [],
};

export const module: Module<VuexNotificationsState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
};

export default module;
