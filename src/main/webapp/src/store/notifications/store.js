// @flow
import mutations from './mutations';
import NC from './constants';
import type {
  HttpError,
  NotificationMessage,
  VuexNotificationsState,
  VuexNotificationsStore,
} from './types';

const getters = {
  [NC.GET.HTTP_ERRORS]:
      (state: VuexNotificationsState): HttpError[] => state.httpErrors,
  [NC.GET.NOTIFICATIONS_FOR_UI]:
      (state: VuexNotificationsState): NotificationMessage[] => state.forUi,
  [NC.GET.NOTIFICATION_FOR_SERVER]:
      (state: VuexNotificationsState) => state.forServer,
  [NC.GET.UI_IS_OPEN]:
      (state: VuexNotificationsState): boolean => state.ui.isOpen,
  [NC.GET.UI_HAS_NEW_MSG]:
      (state: VuexNotificationsState): boolean => state.ui.hasNewMsg,
};

const store: VuexNotificationsStore = {
  namespaced: true,
  state: {
    ui: {
      // true if the notification window is open
      isOpen: false,
      // true if a message has arrived and the notification window is not open
      hasNewMsg: false,
    },
    forUi: [],
    forServer: [],
    httpErrors: [],
  },
  getters,
  mutations,
};

export default store;
