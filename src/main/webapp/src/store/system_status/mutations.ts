/* eslint-disable no-param-reassign */
import { MessageLevel } from '@/services/ws_notifications/types';
import SS from './constants';
import { VuexSystemStatusState } from './types';


export default {
  setSystemStatusDatabase(state: VuexSystemStatusState, payload: MessageLevel) {
    state.database = payload;
  },
  [SS.SET.ALL_STATUSES_UNKNOWN](state: VuexSystemStatusState) {
    state.database = null;
  },
  setSystemStatusUiIsOpen(state: VuexSystemStatusState, isOpen: boolean) {
    state.ui.isOpen = isOpen;
  },
};
