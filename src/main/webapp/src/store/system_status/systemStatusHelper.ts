// This helper module is for use by modules that do not otherwise access Vue
import { MessageLevel } from '@/services/ws_notifications/types';
import { SYSTEM_STATUS_MODULE_NAME } from './constants';
import { getStore } from '@/store/store';

export const setSystemStatusDatabase = (payload: MessageLevel)
  : void => getStore().commit(`${SYSTEM_STATUS_MODULE_NAME}/setSystemStatusDatabase`, payload);
