import { MessageLevel } from '@/services/ws_notifications/types';

export type VuexSystemStatusState = {
  ui: {
    isOpen: boolean;
  };
  database: MessageLevel | null;
};

export type SystemStatusIconInfo = {
  iconName: string;
  classes: string;
};
