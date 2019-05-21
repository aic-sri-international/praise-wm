import { VuexHelpState } from './types';
import { Module } from 'vuex';
import { RootState } from '@/store/types';

const mutations = {
  showHelp(state: VuexHelpState, showHelp: boolean) {
    state.showHelp = showHelp;
  },
};

const state: VuexHelpState = {
  showHelp: false,
};

const module: Module<VuexHelpState, RootState> = {
  namespaced: true,
  state,
  mutations,
};

export default module;
