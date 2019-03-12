// @flow
import cloneDeep from 'lodash/cloneDeep';
import { oneLine } from 'common-tags';
import type {
  ExpressionResultDto,
  SegmentedModelDto,
} from '@/components/model/types';
import mutations from './mutations';
import actions from './actions';
import MODEL from './constants';
import type {
  VuexModelStore,
  VuexModelState,
} from './types';


const getters = {
  [MODEL.GET.CUR_MODEL_NAME]: (state: VuexModelState): string => state.curModelName,
  [MODEL.GET.CUR_QUERY]: (state: VuexModelState): string => state.curQuery,
  [MODEL.GET.CUR_MODEL_DTO]: (state: VuexModelState): SegmentedModelDto => {
    if (!state.curModelName) {
      throw Error('state.curModelName is not set');
    }
    return cloneDeep(state.modelDtos[state.curModelName]);
  },
  [MODEL.GET.QUERY_RESULTS]: (state: VuexModelState):
      ExpressionResultDto[] => cloneDeep(state.queryResults),
  [MODEL.GET.CUR_QUERY_RESULT]: (state: VuexModelState):
      ?ExpressionResultDto => {
    if (state.queryResults.length) {
      if (state.queryResultsIx < 0
          || state.queryResultsIx >= state.queryResults.length) {
        throw Error(oneLine`curQueryResult called: 
        queryResults.length=${state.queryResults.length},
        curQueryResultsIx=${state.queryResultsIx}`);
      }
      return cloneDeep(state.queryResults[state.queryResultsIx]);
    }
    return null;
  },
  [MODEL.GET.IS_QUERY_ACTIVE]: (state: VuexModelState):
      boolean => state.queryStartTime !== 0,
  [MODEL.GET.NUMBER_OF_INITIAL_SAMPLES]: (state: VuexModelState):
      number => state.numberOfInitialSamples,
  [MODEL.GET.NUMBER_OF_DISCRETE_VALUES]: (state: VuexModelState):
      number => state.numberOfDiscreteValues,
};

const store: VuexModelStore = {
  namespaced: true,
  state: {
    curModelName: '',
    curQuery: '',
    modelDtos: { },
    queryResults: [],
    queryResultsIx: -1,
    queryStartTime: 0,
    numberOfInitialSamples: 1000,
    numberOfDiscreteValues: 25,
  },
  getters,
  mutations,
  actions,
};

export default store;
