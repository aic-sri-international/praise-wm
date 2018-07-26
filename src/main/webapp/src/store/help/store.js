// @flow
import type {
  VuexHelpState,
  VuexHelpStore,
} from './types';
import HELP from './constants';


/* eslint-disable no-param-reassign */
const mutations = {
  [HELP.SET.SHOW_HELP](state: VuexHelpState, showHelp: boolean) {
    state.showHelp = showHelp;
  },
};

const getters = {
  [HELP.SET.SHOW_HELP]: (state: VuexHelpState): boolean => state.showHelp,
};

const store: VuexHelpStore = {
  namespaced: true,
  state: {
    showHelp: false,
  },
  getters,
  mutations,
};

export default store;
