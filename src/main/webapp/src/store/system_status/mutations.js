// @flow
/* eslint-disable no-param-reassign */
import type { MessageLevel } from '@/services/ws_notifications/types';
import SS from './constants';
import type { VuexSystemStatusState } from './types';


export default {
  [SS.SET.DATABASE](state: VuexSystemStatusState, status: MessageLevel) {
    state.database = status;
  },
  [SS.SET.ALL_STATUSES_UNKNOWN](state: VuexSystemStatusState) {
    state.database = null;
  },
  [SS.SET.UI_IS_OPEN](state: VuexSystemStatusState, isOpen: boolean) {
    state.ui.isOpen = isOpen;
  },
};
