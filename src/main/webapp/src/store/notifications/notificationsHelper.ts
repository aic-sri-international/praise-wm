// This helper module is for use by modules that do not otherwise access Vue

import { getDate } from '@/utils';
import { MessageLevel } from '@/services/ws_notifications/types';
import { NOTIFICATIONS_MODULE_NAME } from '@/store/notifications/constants';
import { getStore } from '@/store/store';

export const addHttpError = (error: string)
  : void => getStore().commit(`${NOTIFICATIONS_MODULE_NAME}/addHttpError`, error);

type NotificationForUiPayload = {
  date: Date,
  level: MessageLevel,
  text: string,
}

export const addNotificationForUi = (payload: NotificationForUiPayload)
  : void => {
  getStore().commit(`${NOTIFICATIONS_MODULE_NAME}/addNotificationForUi`, payload);
};

export const addErrorNotificationForUi = (msg: string)
  : void => {
  addNotificationForUi({
    date: getDate(),
    level: MessageLevel.ERROR,
    text: msg,
  });
};
