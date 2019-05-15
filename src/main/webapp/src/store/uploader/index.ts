import mutations from './mutations';
import actions from './actions';
import UP from './constants';
import {
  UploadEntry,
  VuexUploaderState,
} from './types';
import { Module } from 'vuex';
import { RootState } from '@/store/types';

const getters = {
  [UP.GET.QUEUE]: (state: VuexUploaderState): UploadEntry[] => state.queue,
};


const state: VuexUploaderState = {
  queue: [],
};

export const module: Module<VuexUploaderState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
  actions,
};

export default module;
