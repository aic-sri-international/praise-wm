import {
  VuexHelpState,
} from './types';
import HELP from './constants';
import { Module } from 'vuex';
import { RootState } from '@/store/types';


/* eslint-disable no-param-reassign */
const mutations = {
  [HELP.SET.SHOW_HELP](state: VuexHelpState, showHelp: boolean) {
    state.showHelp = showHelp;
  },
};

const getters = {
  [HELP.SET.SHOW_HELP]: (state: VuexHelpState): boolean => state.showHelp,
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
