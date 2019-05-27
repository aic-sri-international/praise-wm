/* eslint-disable no-console */

import {
  fetchFeatureCollectionNames, fetchFeatureCollection,
} from './dataSourceProxy';

import {
  VuexFeatureCollectionState,
} from './types';

import { ActionTree } from 'vuex';
import { RootState } from '@/store/types';


const actions: ActionTree<VuexFeatureCollectionState, RootState> = {
  async initFeatureCollectionState({ commit }): Promise<any> {
    let names: string[] = [];
    try {
      names = await fetchFeatureCollectionNames();
    } catch (err) {
      // errors have already been logged
    }
    if (names.length === 0) {
      console.error('no feature collection names loaded');
      return;
    }

    const name = names[0]; // currently we only have one
    let featureCollection: Object | null = null;

    try {
      featureCollection = await fetchFeatureCollection(name);
    } catch (err) {
      return;
    }


    commit('setFeatureCollectionNames', names);
    commit('setCurrentFeatureCollection', { name, featureCollection });
  },
};

export default actions;
