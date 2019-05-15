export enum BroadcastType {
  INCLUSIVE = 'INCLUSIVE',
  EXCLUSIVE = 'EXCLUSIVE',
}

export enum MessageLevel {
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
}

export enum EventType {
  SESSION_TIMEOUT_EVENT = 'SessionTimeoutEvent',
  SYSTEM_SHUTDOWN_EVENT = 'SystemShutdownEvent',
  DATA_REFRESH_EVENT = 'DataRefreshEvent',
  SYSTEM_STATUS_EVENT = 'SystemStatusEvent',
  NOTIFICATION_TEXT_MESSAGE = 'NotificationTextMessage',
  PING_EVENT = 'PingEvent',
}

export enum RefreshType { USER = 'USER' }

export enum SystemStatusType { DATABASE = 'DATABASE' }

export interface NotificationEvent {
  time: string;
  sessionId: string;
  eventType: EventType;
  level?: MessageLevel;
  broadcast?: BroadcastType;
}

export interface SystemShutdownEvent extends NotificationEvent {}

export interface PingEvent extends NotificationEvent {}

export interface NotificationTextMessage extends NotificationEvent {
  text?: string;
}

export interface SessionTimeoutEvent extends NotificationEvent {}

export interface DataRefreshEvent extends NotificationTextMessage {
  refreshType: RefreshType;
}

export interface SystemStatusEvent extends NotificationTextMessage {
  systemStatuses: {
    [K in SystemStatusType]: MessageLevel;
  };
}
