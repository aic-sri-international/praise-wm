// @flow
/* eslint-disable no-param-reassign,no-console */
import Vue from 'vue';
import { oneLine } from 'common-tags';

import type {
  SegmentedModelDto,
  ExpressionResultDto,
} from '@/components/model/types';
import MODEL from './constants';
import type { VuexModelState } from './types';

export default {
  [MODEL.SET.MODEL_DTOS](state: VuexModelState, modelDtos: SegmentedModelDto[]) {
    state.modelDtos
        = modelDtos.reduce((accum: { [string] : SegmentedModelDto }, cur: SegmentedModelDto) => {
        accum[cur.name] = cur;
        return accum;
      }, {});
  },
  [MODEL.SET.CUR_MODEL_DTO](state: VuexModelState, modelDto: SegmentedModelDto) {
    Vue.set(state.modelDtos, modelDto.name, modelDto);
  },
  [MODEL.SET.CUR_MODEL_NAME](state: VuexModelState, name: string) {
    state.curModelName = name;
  },
  [MODEL.SET.CUR_QUERY](state: VuexModelState, query: string) {
    state.curQuery = query;
  },
  [MODEL.SET.QUERY_RESULT](state: VuexModelState, queryResult: ExpressionResultDto) {
    state.queryResults = [queryResult].concat(state.queryResults);
  },
  [MODEL.SET.QUERY_RESULT_IX](state: VuexModelState, queryResultsIx: number) {
    const maxIx = state.queryResults.length - 1;
    if (queryResultsIx < -1 || queryResultsIx > maxIx) {
      throw Error(oneLine`MODEL.SET.QUERY_RESULT_IX: 
            queryResults.length=${state.queryResults.length},
            queryResultIx=${queryResultsIx}`);
    }
    state.queryResultsIx = queryResultsIx;
  },
  [MODEL.SET.CLEAR_QUERY_RESULT](state: VuexModelState) {
    state.queryResults = [];
  },
  [MODEL.SET.IS_QUERY_ACTIVE](state: VuexModelState, isActive: boolean) {
    state.queryStartTime = isActive ? Date.now() : 0;
  },
  [MODEL.SET.NUMBER_OF_INITIAL_SAMPLES](state: VuexModelState, initialSamples: number) {
    state.numberOfInitialSamples = initialSamples;
  },
  [MODEL.SET.NUMBER_OF_DISCRETE_VALUES](state: VuexModelState, discreteValues: number) {
    state.numberOfDiscreteValues = discreteValues;
  },
};
