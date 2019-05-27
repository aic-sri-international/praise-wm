/* eslint-disable no-console */

// eslint-disable-next-line import/extensions,import/no-unresolved
import { FeatureCollection } from 'geojson';
import {
  VuexFeatureCollectionState,
} from './types';


export default {
  setFeatureCollectionNames(state: VuexFeatureCollectionState, names: string[]) {
    state.featureCollectionNames = names;
  },
  setCurrentFeatureCollection(
    state: VuexFeatureCollectionState,
    { name, featureCollection } : {name: string, featureCollection: FeatureCollection},
    ) {
    state.currentFeatureCollectionName = name;
    state.currentFeatureCollection = featureCollection;
  },
};
