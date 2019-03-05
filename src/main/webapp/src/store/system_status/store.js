// @flow
import { messageLevels } from '@/services/ws_notifications/types';

import mutations from './mutations';
import SS from './constants';
import type {
  VuexSystemStatusState,
  VuexSystemStatusStore,
  SystemStatusIconInfo,
} from './types';

const rankedLevels = [
  messageLevels.INFO,
  messageLevels.WARN,
  messageLevels.ERROR,
];

const iconInfoByLevel: SystemStatusIconInfo[] = [
  { iconName: 'check-circle', classes: 'systemStatusInfoColor' },
  { iconName: 'exclamation-triangle', classes: 'systemStatusWarnColor' },
  { iconName: 'times-circle', classes: 'systemStatusErrorColor' },
];

const getIconInfoForLevel = (level) :
    ?SystemStatusIconInfo => (level ? iconInfoByLevel[rankedLevels.indexOf(level)] : null);

const getters = {
  [SS.GET.UI_IS_OPEN]:
      (state: VuexSystemStatusState): boolean => state.ui.isOpen,
  [SS.GET.DATABASE_ICON_INFO]: (state: VuexSystemStatusState):
      ?SystemStatusIconInfo => getIconInfoForLevel(state.database),
  [SS.GET.STATUS_OVERALL_ICON_INFO]:
      (state: VuexSystemStatusState): ?SystemStatusIconInfo => {
        // eslint-disable-next-line function-paren-newline
        const max = Math.max(
        // placeholder for additional system state
          rankedLevels.indexOf(state.database));
        return max === -1 ? null : iconInfoByLevel[max];
      },
};

const store: VuexSystemStatusStore = {
  namespaced: true,
  state: {
    ui: {
      isOpen: false,
    },
    database: null,
  },
  getters,
  mutations,
};

export default store;
