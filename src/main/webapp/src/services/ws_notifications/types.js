// @flow

const broadcastTypes = {
  INCLUSIVE: 'INCLUSIVE',
  EXCLUSIVE: 'EXCLUSIVE',
};

// eslint-disable-next-line no-undef
type BroadcastType = $Keys<typeof broadcastTypes>;

const messageLevels = {
  INFO: 'INFO',
  WARN: 'WARN',
  ERROR: 'ERROR',
};

// eslint-disable-next-line no-undef
type MessageLevel = $Keys<typeof messageLevels>;

const eventTypes = {
  SESSION_TIMEOUT_EVENT: 'SessionTimeoutEvent',
  SYSTEM_SHUTDOWN_EVENT: 'SystemShutdownEvent',
  DATA_REFRESH_EVENT: 'DataRefreshEvent',
  SYSTEM_STATUS_EVENT: 'SystemStatusEvent',
  NOTIFICATION_TEXTMESSAGE: 'NotificationTextMessage',
  PING_EVENT: 'PingEvent',
};

// eslint-disable-next-line no-undef
type EventType = $Values<typeof eventTypes>;

type NotificationEvent = {
  time: string,
  sessionId: string,
  eventType: EventType,
  level?: MessageLevel,
  broadcast?: BroadcastType,
};

type SystemShutdownEvent = NotificationEvent;
type PingEvent = NotificationEvent;

type NotificationTextMessage = NotificationEvent & {
  text?: string,
}

type SessionTimeoutEvent = NotificationTextMessage;

const refreshTypes = {
  USER: 'USER',
};

// eslint-disable-next-line no-undef
type RefreshType = $Keys<typeof refreshTypes>;

type DataRefreshEvent = NotificationTextMessage &
    {
      refreshType: RefreshType,
    };

const systemStatusTypes = {
  DATABASE: 'DATABASE',
};

// eslint-disable-next-line no-undef
type SystemStatusType = $Keys<typeof systemStatusTypes>;

type SystemStatusEvent = NotificationTextMessage &
    {
      systemStatuses: {
        [systemStatusType: SystemStatusType]: MessageLevel,
      },
    };

export {
  eventTypes,
  broadcastTypes,
  messageLevels,
  refreshTypes,
  systemStatusTypes,
};

export type {
  NotificationEvent,
  EventType,
  MessageLevel,
  BroadcastType,

  SystemShutdownEvent,
  PingEvent,

  NotificationTextMessage,
  SessionTimeoutEvent,

  DataRefreshEvent,
  RefreshType,

  SystemStatusEvent,
  SystemStatusType,
};
