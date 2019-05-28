import { MessageLevel } from '@/services/ws_notifications/types';

export type VuexSystemStatusState = {
  // true if the system status window is open
  showSystemStatus: boolean;
  database: MessageLevel | null;
};

export type SystemStatusIconInfo = {
  iconName: string;
  classes: string;
};
