import {
  BroadcastType,
  MessageLevel,
  RefreshType,
  SystemStatusType,
} from '@/services/ws_notifications/types';

export type RegistryCallback = () => Promise<any>;

export type RegistryComponent = {
  name: string;
  callback: RegistryCallback;
}

export type RegistryEntry = {
  type: RefreshType;
  name: string;
  callback: RegistryCallback;
};

export type NotificationInputDto = {
  broadcast: BroadcastType;
  level?: MessageLevel;
  dataRefreshType?: RefreshType;
  systemStatusType?: SystemStatusType;
  text?: string;
}
