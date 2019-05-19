import {
  HttpError,
  NotificationMessage,
  VuexNotificationsState,
  NotificationForUiPayload,
} from './types';

let httpErrId : number = 0;
let notForUiId : number = 0;

export default {
  addHttpError(state: VuexNotificationsState, error: string) {
    httpErrId += 1;
    const httpError: HttpError = {
      id: httpErrId,
      error,
    };
    state.httpErrors.push(httpError);
  },
  removeHttpError(state: VuexNotificationsState, id: number) {
    const arr = state.httpErrors;
    const ix = arr.findIndex(e => e.id === id);
    if (ix !== -1) {
      arr.splice(ix, 1);
    }
  },
  addNotificationForUi(
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
  removeNotificationsForUi(
    state: VuexNotificationsState,
    idsToRemove: number[],
  ) {
    state.forUi = state.forUi.filter(item => !idsToRemove.includes(item.id));
  },
  removeAllNotificationsForUi(state: VuexNotificationsState) {
    state.forUi = [];
    state.ui.hasNewMsg = false;
  },
  setNotificationUiIsOpen(state: VuexNotificationsState, isOpen: boolean) {
    state.ui.isOpen = isOpen;
    if (isOpen) {
      state.ui.hasNewMsg = false;
    }
  },
};
