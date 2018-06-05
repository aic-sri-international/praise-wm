// @flow
import type { MessageLevel } from '@/services/ws_notifications/types';

type NotificationMessage = {
  id: number,
  date: Date,
  level: MessageLevel,
  text: string,
}

type HttpError = {
  id: number,
  error: string,
};

type VuexNotificationsState = {
  ui: {
    isOpen: boolean,
    hasNewMsg: boolean,
  },
  forUi: Array<NotificationMessage>,
  forServer: Array<NotificationMessage>,
  httpErrors: Array<HttpError>,
};

type VuexNotificationsStore = {
  namespaced: boolean,
  state: VuexNotificationsState,
  getters: Object,
  mutations: Object,
};

export type {
  HttpError,
  VuexNotificationsState,
  VuexNotificationsStore,
  NotificationMessage,
};
