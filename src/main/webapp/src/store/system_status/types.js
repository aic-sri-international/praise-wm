// @flow
import type { MessageLevel } from '@/services/ws_notifications/types';

type VuexSystemStatusState = {
  ui: {
    isOpen: boolean,
  },
  database: ?MessageLevel,
};

type VuexSystemStatusStore = {
  namespaced: boolean,
  state: VuexSystemStatusState,
  getters: Object,
  mutations: Object,
};

export type {
  VuexSystemStatusState,
  VuexSystemStatusStore,
};
