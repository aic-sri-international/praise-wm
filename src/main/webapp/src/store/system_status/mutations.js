// @flow
/* eslint-disable no-param-reassign */
import type { SystemStatusVuexPayload } from '@/services/ws_notifications/types';
import SS from './constants';
import type { VuexSystemStatusState } from './types';


export default {
  [SS.SET.DATABASE](state: VuexSystemStatusState, payload: SystemStatusVuexPayload) {
    state.database = payload.level;
  },
  [SS.SET.ALL_STATUSES_UNKNOWN](state: VuexSystemStatusState) {
    state.database = null;
  },
  [SS.SET.UI_IS_OPEN](state: VuexSystemStatusState, isOpen: boolean) {
    state.ui.isOpen = isOpen;
  },
};
