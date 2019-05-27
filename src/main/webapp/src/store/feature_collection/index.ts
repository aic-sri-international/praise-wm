import { Module } from 'vuex';

import { VuexFeatureCollectionState } from './types';

import actions from './actions';
import mutations from './mutations';
import { RootState } from '@/store/types';


const state: VuexFeatureCollectionState = {
  featureCollectionNames: [],
  currentFeatureCollectionName: '',
  currentFeatureCollection: null,
};

export const module: Module<VuexFeatureCollectionState, RootState> = {
  namespaced: true,
  state,
  mutations,
  actions,
};

export default module;
