import { Module } from 'vuex';
import cloneDeep from 'lodash/cloneDeep';
import {
  VuexModelState,
  ExpressionResultDto,
  QueryResultWrapper,
  SegmentedModelDto,
  modelQueryDtoDefaults,
  EditorTransition,
} from './types';
import mutations from './mutations';
// eslint-disable-next-line import/no-cycle
import actions from './actions';
import MODEL from './constants';
import { emptyModelDto, EMPTY_MODEL_NAME } from './util';
import { RootState } from '@/store/types';

const getters = {
  [MODEL.GET.MODEL_NAMES]: (state: VuexModelState):
      string[] => Object.keys(state.modelDtos).sort(),
  [MODEL.GET.CUR_QUERIES]: (state: VuexModelState): string[] => {
    if (!state.curModelName) {
      return [];
    }
    const model: SegmentedModelDto | undefined = state.modelDtos[state.curModelName];
    let queries: string[] = [];
    if (model) {
      // We might be in the middle of a transition to a new set of loaded models
      // or removal of the current model
      queries = state.modelDtos[state.curModelName].queries || [];
    }
    return [...queries];
  },
  [MODEL.GET.CUR_MODEL_DTO]: (state: VuexModelState): SegmentedModelDto => {
    if (!state.curModelName) {
      throw Error('state.curModelName is not set');
    }
    return cloneDeep(state.modelDtos[state.curModelName]);
  },
  [MODEL.GET.CUR_RESULT]: (state: VuexModelState): ExpressionResultDto | null => (
    state.queryResultsIx < 0 ? null : state.queryResults[state.queryResultsIx].expressionResult
  ),
  [MODEL.GET.CUR_RESULT_WRAPPER]: (state: VuexModelState): QueryResultWrapper | null => (
    state.queryResultsIx < 0 ? null : state.queryResults[state.queryResultsIx]
  ),
  [MODEL.GET.IS_QUERY_ACTIVE]: (state: VuexModelState):
      boolean => state.queryStartTime !== 0,
  [MODEL.GET.DISPLAY_CHART]: (state: VuexModelState): boolean => {
    if (state.queryResultsIx >= 0) {
      const queryResult :
        ExpressionResultDto = state.queryResults[state.queryResultsIx].expressionResult;
      const { graphQueryResultDto } = queryResult;
      return graphQueryResultDto !== undefined
          && graphQueryResultDto.imageData !== undefined
          && graphQueryResultDto.mapRegionNameToValue === undefined;
    }
    return false;
  },
};

// The store should always have a curModelName whose SegmentedModelDto
// can be found in modelDtos.

const state: VuexModelState = {
  editorTransition: EditorTransition.NONE,
  curModelName: EMPTY_MODEL_NAME,
  curQuery: '',
  modelDtos: { [EMPTY_MODEL_NAME]: emptyModelDto() },
  queryResults: [],
  queryResultsIx: -1,
  queryStartTime: 0,
  abortQueryFlag: false,
  numberOfInitialSamples: modelQueryDtoDefaults.numberOfInitialSamples,
  numberOfDiscreteValues: modelQueryDtoDefaults.numberOfDiscreteValues,
};

export const module: Module<VuexModelState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
  actions,
};
export default module;
