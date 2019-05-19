import {
  VuexHelpState,
} from './types';
import { Module } from 'vuex';
import { RootState } from '@/store/types';

const mutations = {
  showHelp(state: VuexHelpState, showHelp: boolean) {
    state.showHelp = showHelp;
  },
};

const getters = {
  showHelp: (state: VuexHelpState): boolean => state.showHelp,
};

const state: VuexHelpState = {
  showHelp: false,
};

const module: Module<VuexHelpState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
};

export default module;
