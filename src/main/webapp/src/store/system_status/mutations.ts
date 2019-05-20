import { MessageLevel } from '@/services/ws_notifications/types';
import { VuexSystemStatusState } from './types';


export default {
  setSystemStatusDatabase(state: VuexSystemStatusState, payload: MessageLevel) {
    state.database = payload;
  },
  setAllStatusesToUnknown(state: VuexSystemStatusState) {
    state.database = null;
  },
  setSystemStatusUiIsOpen(state: VuexSystemStatusState, isOpen: boolean) {
    state.uiIsOpen = isOpen;
  },
};
