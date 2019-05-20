import { MessageLevel } from '@/services/ws_notifications/types';

export type NotificationMessage = {
  id: number,
  date: Date,
  level: MessageLevel,
  text: string,
}

export type HttpError = {
  id: number,
  error: string,
}

export type VuexNotificationsState = {
  uiIsOpen: boolean,
  notificationUiHasNewMsg: boolean,
  notificationsForUi: Array<NotificationMessage>,
  httpErrors: Array<HttpError>,
}

export type NotificationForUiPayload = {
  level: MessageLevel,
  date: Date,
  text: string,
}
