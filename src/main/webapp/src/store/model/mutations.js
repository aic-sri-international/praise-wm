// @flow
/* eslint-disable no-param-reassign,no-console */
import Vue from 'vue';
import { oneLine } from 'common-tags';
import isEqual from 'lodash/isEqual';

import type {
  SegmentedModelDto,
  ExpressionResultDto,
} from '@/components/model/types';
import MODEL from './constants';
import type { EditorTransition, VuexModelState } from './types';

export default {
  [MODEL.SET.EDITOR_TRANSITION](state: VuexModelState, editorTransition: EditorTransition) {
    state.editorTransition = editorTransition;
  },
  [MODEL.SET.MODEL_DTOS](state: VuexModelState, modelDtos: SegmentedModelDto[]) {
    state.modelDtos
        = modelDtos.reduce((accum: { [string] : SegmentedModelDto }, cur: SegmentedModelDto) => {
        accum[cur.name] = cur;
        return accum;
      }, {});
  },
  [MODEL.SET.MODEL_DTO](state: VuexModelState, modelDto: SegmentedModelDto) {
    Vue.set(state.modelDtos, modelDto.name, modelDto);
  },
  [MODEL.SET.CUR_MODEL_NAME](state: VuexModelState, name: string) {
    state.curModelName = name;
  },
  [MODEL.SET.CUR_QUERY](state: VuexModelState, query: string) {
    state.curQuery = query;
  },
  [MODEL.SET.CUR_MODEL_QUERIES](state: VuexModelState, queries: string[]) {
    if (state.curModelName) {
      const model: SegmentedModelDto = state.modelDtos[state.curModelName];
      if (model) {
        if (!isEqual(model.queries, queries)) {
          model.queries = [...queries];
        }
      } else {
        console.warn(`MODEL.SET.CUR_MODEL_QUERIES model not found for  ${state.curModelName}`);
      }
    }
  },
  [MODEL.SET.QUERY_RESULT](state: VuexModelState, queryResult: ExpressionResultDto) {
    state.queryResults = [queryResult].concat(state.queryResults);
    state.queryResultsIx = state.queryResults.length ? 0 : -1;
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
  [MODEL.SET.CLEAR_QUERY_RESULTS](state: VuexModelState) {
    state.queryResults = [];
    state.queryResultsIx = -1;
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
