import { Module } from 'vuex';

import {
  VuexUserState,
} from './types';

import mutations from './mutations';
import { RootState } from '@/store/types';

const getters = {
  user: (state: VuexUserState): VuexUserState => ({ ...state }),
};

const state: VuexUserState = {
  isLoggedIn: false,
  userId: -1,
  name: '',
  isAdminRole: false,
};

export const module: Module<VuexUserState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
};


export default module;
