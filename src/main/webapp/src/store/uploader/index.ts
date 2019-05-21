import mutations from './mutations';
import actions from './actions';
import { VuexUploaderState } from './types';
import { Module } from 'vuex';
import { RootState } from '@/store/types';

const state: VuexUploaderState = {
  queue: [],
};

export const module: Module<VuexUploaderState, RootState> = {
  namespaced: true,
  state,
  mutations,
  actions,
};

export default module;
