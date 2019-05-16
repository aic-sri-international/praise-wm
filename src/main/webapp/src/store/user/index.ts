import { Module } from 'vuex';

import {
  VuexUserState,
} from './types';

import mutations from './mutations';
import UC from './constants';
import { RootState } from '@/store/types';

const getters = {
  [UC.GET.USER]: (state: VuexUserState): VuexUserState => ({ ...state }),
  [UC.GET.IS_LOGGED_IN]: (state: VuexUserState): boolean => state.isLoggedIn,
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
