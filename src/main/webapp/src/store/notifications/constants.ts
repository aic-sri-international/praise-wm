const NOTIFICATIONS_VXC = {
  MODULE: 'notifications',
  GET: {
    HTTP_ERRORS: 'httpErrors',
    NOTIFICATIONS_FOR_UI: 'notificationsForUi',
    NOTIFICATION_FOR_SERVER: 'notificationForServer',
    UI_IS_OPEN: 'notificationUiIsOpen',
    UI_HAS_NEW_MSG: 'notificationUiHasNewMsg',
  },
  SET: {
    ADD_HTTP_ERROR: 'addHttpError',
    REMOVE_HTTP_ERROR: 'removeHttpError',
    ADD_NOTIFICATION_FOR_UI: 'addNotificationForUi',
    REMOVE_NOTIFICATIONS_FOR_UI: 'removeNotificationsForUi',
    REMOVE_ALL_NOTIFICATIONS_FOR_UI: 'removeAllNotificationsForUi',
    ADD_NOTIFICATION_FOR_SERVER: 'addNotificationForServer',
    REMOVE_NOTIFICATION_FOR_SERVER: 'removeNotificationForServer',
    UI_IS_OPEN: 'setNotificationUiIsOpen',
  },
};

export default NOTIFICATIONS_VXC;
