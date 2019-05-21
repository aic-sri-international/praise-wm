import {
  HttpError,
  NotificationForUiPayload,
  NotificationMessage,
  VuexNotificationsState,
} from './types';

let httpErrId: number = 0;
let notForUiId: number = 0;

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

    state.notificationsForUi.push(notification);
    if (!state.uiIsOpen) {
      state.notificationUiHasNewMsg = true;
    }
  },
  removeNotificationsForUi(
    state: VuexNotificationsState,
    idsToRemove: number[],
  ) {
    state.notificationsForUi = state.notificationsForUi.filter(
      item => !idsToRemove.includes(item.id),
    );
  },
  removeAllNotificationsForUi(state: VuexNotificationsState) {
    state.notificationsForUi = [];
    state.notificationUiHasNewMsg = false;
  },
  setNotificationUiIsOpen(state: VuexNotificationsState, isOpen: boolean) {
    state.uiIsOpen = isOpen;
    if (isOpen) {
      state.notificationUiHasNewMsg = false;
    }
  },
};
