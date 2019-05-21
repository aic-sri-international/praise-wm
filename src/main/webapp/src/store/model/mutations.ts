/* eslint-disable no-console */
import Vue from 'vue';
import { oneLine } from 'common-tags';
import isEqual from 'lodash/isEqual';

import {
  EditorTransition,
  ModelEditorData,
  QueryResultWrapper,
  SegmentedModelDto,
  UpdateModelDtoPayload,
  VuexModelState,
} from './types';

export default {
  setEditorTransition(state: VuexModelState, editorTransition: EditorTransition) {
    state.editorTransition = editorTransition;
  },
  setModelDtos(state: VuexModelState, modelDtos: SegmentedModelDto[]) {
    state.modelDtos = modelDtos.reduce(
      (accum: { [key: string]: SegmentedModelDto }, cur: SegmentedModelDto) => {
        accum[cur.name] = cur;
        return accum;
      }, {},
    );
  },
  setModelDto(state: VuexModelState, modelDto: SegmentedModelDto) {
    Vue.set(state.modelDtos, modelDto.name, modelDto);
  },
  updateModelDto(state: VuexModelState, payload: UpdateModelDtoPayload) {
    const model: SegmentedModelDto = state.modelDtos[payload.modelName];
    const med: ModelEditorData = payload.modelEditorData;
    model.description = med.description;
    model.declarations = med.declarations;
    model.rules = med.rules;
  },
  setCurModelName(state: VuexModelState, name: string) {
    state.curModelName = name;
  },
  setCurQuery(state: VuexModelState, query: string) {
    state.curQuery = query;
  },
  setCurModelQueries(state: VuexModelState, queries: string[]) {
    if (state.curModelName) {
      const model: SegmentedModelDto = state.modelDtos[state.curModelName];
      if (model) {
        if (!isEqual(model.queries, queries)) {
          model.queries = [...queries];
        }
      } else {
        console.warn(`setCurModelQueries model not found for  ${state.curModelName}`);
      }
    }
  },
  setQueryResult(state: VuexModelState, queryResult: QueryResultWrapper) {
    state.queryResults = [queryResult].concat(state.queryResults);
    state.queryResultsIx = state.queryResults.length ? 0 : -1;
  },
  setQueryResultsIx(state: VuexModelState, queryResultsIx: number) {
    const maxIx = state.queryResults.length - 1;
    if (queryResultsIx < -1 || queryResultsIx > maxIx) {
      throw Error(oneLine`setQueryResultsIx: 
            queryResults.length=${state.queryResults.length},
            queryResultIx=${queryResultsIx}`);
    }
    state.queryResultsIx = queryResultsIx;
  },
  clearQueryResults(state: VuexModelState) {
    state.queryResults = [];
    state.queryResultsIx = -1;
  },
  setAbortQuery(state: VuexModelState, abort: boolean) {
    state.abortQueryFlag = abort;
  },
  setQueryActive(state: VuexModelState, isActive: boolean) {
    state.queryStartTime = isActive ? Date.now() : 0;
  },
  setNumberOfInitialSamples(state: VuexModelState, initialSamples: number) {
    state.numberOfInitialSamples = initialSamples;
  },
  setNumberOfDiscreteValues(state: VuexModelState, discreteValues: number) {
    state.numberOfDiscreteValues = discreteValues;
  },
};
