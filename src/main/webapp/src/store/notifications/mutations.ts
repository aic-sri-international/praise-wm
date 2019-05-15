/* eslint-disable no-param-reassign */
import NC from './constants';
import {
  HttpError,
  NotificationMessage,
  VuexNotificationsState,
  NotificationForUiPayload,
} from './types';

let httpErrId : number = 0;
let notForUiId : number = 0;

export default {
  [NC.SET.ADD_HTTP_ERROR](state: VuexNotificationsState, error: string) {
    httpErrId += 1;
    const httpError: HttpError = {
      id: httpErrId,
      error,
    };
    state.httpErrors.push(httpError);
  },
  [NC.SET.REMOVE_HTTP_ERROR](state: VuexNotificationsState, id: number) {
    const arr = state.httpErrors;
    const ix = arr.findIndex(e => e.id === id);
    if (ix !== -1) {
      arr.splice(ix, 1);
    }
  },
  [NC.SET.ADD_NOTIFICATION_FOR_UI](
    state: VuexNotificationsState,
    payload: NotificationForUiPayload,
  ) {
    notForUiId += 1;

    const notification: NotificationMessage = {
      id: notForUiId,
      date: payload.date,
      level: payload.level,
      text: payload.text,
    };

    state.forUi.push(notification);
    if (!state.ui.isOpen) {
      state.ui.hasNewMsg = true;
    }
  },
  [NC.SET.REMOVE_NOTIFICATIONS_FOR_UI](
    state: VuexNotificationsState,
    idsToRemove: number[],
  ) {
    state.forUi = state.forUi.filter(item => !idsToRemove.includes(item.id));
  },
  [NC.SET.REMOVE_ALL_NOTIFICATIONS_FOR_UI](state: VuexNotificationsState) {
    state.forUi = [];
    state.ui.hasNewMsg = false;
  },
  [NC.SET.UI_IS_OPEN](state: VuexNotificationsState, isOpen: boolean) {
    state.ui.isOpen = isOpen;
    if (isOpen) {
      state.ui.hasNewMsg = false;
    }
  },
};
