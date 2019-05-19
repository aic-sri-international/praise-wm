import { MutationTree } from 'vuex';
import { VuexUserState } from './types';

const mutations: MutationTree<VuexUserState> = {
  setLoggedIn(state, userData: VuexUserState) {
    Object.assign(state, userData);
  },
  setLoggedOut(state) {
    state.isLoggedIn = false;
  },
};

export default mutations;
