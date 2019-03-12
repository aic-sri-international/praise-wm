// @flow

import type {
  SegmentedModelDto,
  ExpressionResultDto,
} from '@/components/model/types';

type VuexModelState = {
  curModelName: string,
  curQuery: string,
  modelDtos: { [string]: SegmentedModelDto },
  queryResults: ExpressionResultDto[],
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
};

