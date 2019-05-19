import { MessageLevel } from '@/services/ws_notifications/types';

import mutations from './mutations';
import {
  VuexSystemStatusState,
  SystemStatusIconInfo,
} from './types';
import { Module } from 'vuex';
import { RootState } from '@/store/types';

const rankedLevels = [
  MessageLevel.INFO,
  MessageLevel.WARN,
  MessageLevel.ERROR,
];

const iconInfoByLevel: SystemStatusIconInfo[] = [
  { iconName: 'check-circle', classes: 'systemStatusInfoColor' },
  { iconName: 'exclamation-triangle', classes: 'systemStatusWarnColor' },
  { iconName: 'times-circle', classes: 'systemStatusErrorColor' },
];

const getIconInfoForLevel = (level: MessageLevel | null) :
    SystemStatusIconInfo | null => (level ? iconInfoByLevel[rankedLevels.indexOf(level)] : null);

const getters = {
  systemStatusUiIsOpen:
      (state: VuexSystemStatusState): boolean => state.ui.isOpen,
  systemStatusDatabaseIconInfo: (state: VuexSystemStatusState):
      SystemStatusIconInfo | null => getIconInfoForLevel(state.database),
  systemStatusOverallIconInfo:
      (state: VuexSystemStatusState): SystemStatusIconInfo | null => {
        const getIndex = (level: MessageLevel | null):
          number => (level ? rankedLevels.indexOf(level) : -1);

        const max = Math.max(
        // placeholder for additional system state
          getIndex(state.database),
        );
        return max === -1 ? null : iconInfoByLevel[max];
      },
};

const state: VuexSystemStatusState = {
  ui: {
    isOpen: false,
  },
  database: null,
};

export const module: Module<VuexSystemStatusState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
};

export default module;
