// @flow
import type {
  BroadcastType,
  MessageLevel,
  RefreshType,
  SystemStatusType,
} from '@/services/ws_notifications/types';

type RegistryCallback = () => Promise<any>;

type RegistryComponent = {
  name: string,
  callback: RegistryCallback,
}

type RegistryEntry = {
  type: RefreshType,
  name: string,
  callback: RegistryCallback
};

type NotificationInputDto = {
  broadcast: BroadcastType,
  level?: MessageLevel,
  dataRefreshType?: RefreshType,
  systemStatusType?: SystemStatusType,
  text?: string,
}

export type {
  RegistryCallback,
  RegistryEntry,
  RegistryComponent,
  NotificationInputDto,
};
