/* eslint-disable no-param-reassign */
import { MutationTree } from 'vuex';

import { VuexUserState } from './types';
import UC from './constants';

const mutations: MutationTree<VuexUserState> = {
  [UC.SET.LOGGED_IN](state, userData: VuexUserState) {
    Object.assign(state, userData);
  },
  [UC.SET.LOGGED_OUT](state) {
    state.isLoggedIn = false;
  },
};

export default mutations;
