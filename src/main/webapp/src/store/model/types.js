// @flow

import type {
  SegmentedModelDto,
  QueryResultWrapper,
} from '@/components/model/types';

export const editorTransitions = {
  NONE: 'NONE',
  LOAD: 'LOAD', // The editor manager should load the editor from the store
  STORE: 'STORE', // The editor manager save the editor text into the store
};

// eslint-disable-next-line no-undef
type EditorTransition = $Values<typeof editorTransitions>;

type VuexModelState = {
  editorTransition: EditorTransition,
  curQuery: string,
  curModelName: string,
  modelDtos: { [string]: SegmentedModelDto },
  queryResults: QueryResultWrapper[],
  queryResultsIx: number,
  queryStartTime: number,
  numberOfInitialSamples: number,
  numberOfDiscreteValues: number,
};

type VuexModelStore = {
  namespaced: boolean,
  state: VuexModelState,
  getters: Object,
  mutations: Object,
};

export type {
  VuexModelState,
  VuexModelStore,
  EditorTransition,
};

