// eslint-disable-next-line import/extensions,import/no-unresolved
import { FeatureCollection } from 'geojson';

export interface VuexFeatureCollectionState {
  featureCollectionNames: Array<String>;
  currentFeatureCollectionName: string;
  currentFeatureCollection: FeatureCollection | null;
}
