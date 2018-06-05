// @flow
import { messageLevels } from '@/services/ws_notifications/types';

import mutations from './mutations';
import SS from './constants';
import type {
  VuexSystemStatusState,
  VuexSystemStatusStore,
} from './types';

const rankedLevels = [
  messageLevels.INFO,
  messageLevels.WARN,
  messageLevels.ERROR,
];

const classesByLevel = [
  'fa fa-check-circle systemStatusInfoColor',
  'fa fa-exclamation-triangle systemStatusWarnColor',
  'fa fa-times-circle systemStatusErrorColor',
];

const getClassesForLevel = (level) : ?string =>
  (level ? classesByLevel[rankedLevels.indexOf(level)] : null);

const getters = {
  [SS.GET.UI_IS_OPEN]:
      (state: VuexSystemStatusState): boolean => state.ui.isOpen,
  [SS.GET.DATABASE_CLASS]:
      (state: VuexSystemStatusState): ?string => getClassesForLevel(state.database),
  [SS.GET.STATUS_OVERALL_CLASS]:
      (state: VuexSystemStatusState): ?string => {
        // eslint-disable-next-line function-paren-newline
        const max = Math.max(
          //  comment placeholder for additional state
          rankedLevels.indexOf(state.database));
        return max === -1 ? null : classesByLevel[max];
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
