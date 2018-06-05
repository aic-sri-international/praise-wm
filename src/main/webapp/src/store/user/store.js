// @flow

import { store as rootStore, vxcFp } from '@/store';
import type {
  VuexUserState,
  VuexUserStore,
} from './types';

import mutations from './mutations';
import UC from './constants';

const getters = {
  [UC.GET.USER]: (state: VuexUserState): VuexUserState => ({ ...state }),
};

const store: VuexUserStore = {
  namespaced: true,
  state: {
    isLoggedIn: false,
    userId: -1,
    name: '',
    isAdminRole: false,
  },
  getters,
  mutations,
};

// eslint-disable-next-line no-unused-vars
export function newWatchLogin(callback: (x:boolean)=>void) {
  return rootStore.watch(
    () => rootStore.getters[vxcFp(UC, UC.GET.USER)].isLoggedIn,
    callback,
  );
}

export default store;
