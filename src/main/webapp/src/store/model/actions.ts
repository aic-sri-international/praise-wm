/* eslint-disable no-console */

import Vue from 'vue';
import cloneDeep from 'lodash/cloneDeep';
import { oneLine } from 'common-tags';
import { FileInfo, downloadFile, getDate } from '@/utils';
import {
  fetchSegmentedModels,
  solve,
  fetchGraph,
  interruptSolver,
} from './dataSourceProxy';

import {
  SegmentedModelDto,
  ModelQueryDto,
  ExpressionResultDto,
  QueryResultWrapper,
  GraphRequestDto,
  GraphRequestResultDto,
  GraphQueryResultDto,
  EditorTransition,
  VuexModelState,
  RunQueryFunctionPayload,
} from './types';
import { MODEL_MODULE_NAME } from './constants';
import { validateAndCleanModel, extractModelText, minimizeModel } from './util';
import { ActionTree, Commit } from 'vuex';
import { RootState } from '@/store/types';
import { abortWatcher } from '@/store/abortWatcher';

const queryAbortWatcher = (): { unwatch: Function, signal: AbortSignal } => abortWatcher(
  MODEL_MODULE_NAME,
  'abortQueryFlag',
  'setAbortQuery',
  'Query',
  interruptSolver,
);


const loadModels = async (): Promise<SegmentedModelDto[]> => {
  try {
    return await fetchSegmentedModels();
  } catch (err) {
  // errors already logged/displayed
    return [];
  }
};

const wait = (ms:number) => new Promise(resolve => setTimeout(resolve, ms));

const waitForTransitionToComplete = async (state: VuexModelState) : Promise<any> => {
  const max = 20;
  const ms = 5;
  let count = 0;

  // This should usually be sufficient
  await Vue.nextTick();

  while (count <= max && state.editorTransition !== EditorTransition.NONE) {
    // eslint-disable-next-line no-await-in-loop
    await wait(ms);
    count += 1;
  }

  if (count > max) {
    console.error(oneLine`Editor has not transitioned to 
                            ${EditorTransition.NONE} after ${max * ms} ms`);
  }

  return Promise.resolve();
};

const updateCurModelFromEditor = async (state: VuexModelState, commit: Commit, getters: any):
    Promise<SegmentedModelDto> => {
  if (!state.curModelName) {
    throw Error('curModelName not set');
  }
  const curModel : SegmentedModelDto = getters.curModelDto;
  if (!curModel) {
    throw Error(`model not found for ${state.curModelName}`);
  }
  commit('setEditorTransition', EditorTransition.STORE);

  await waitForTransitionToComplete(state);

  return getters.curModelDto;
};

const setCurrentModel = async (commit: Commit, model: SegmentedModelDto) : Promise<any> => {
  commit('setModelDto', model);
  // Make sure the watcher for modelNames completes before the watcher for curModelName is called.
  await Vue.nextTick();
  commit('setCurModelName', model.name);

  const curQuery = (model.queries && model.queries.length) ? model.queries[0] : '';
  commit('setCurQuery', curQuery);

  commit('clearQueryResults');
  commit('setEditorTransition', EditorTransition.LOAD);
};

const actions: ActionTree<VuexModelState, RootState> = {
  async initialize({ commit }) : Promise<any> {
    let models: SegmentedModelDto[] = await loadModels();
    if (models.length === 0) {
      // Probably encountered an error that has been logged, so just return
      return;
    }

    models = models.reduce((accum: SegmentedModelDto[], cur: SegmentedModelDto) => {
      const model: SegmentedModelDto | null = validateAndCleanModel(cur);
      if (model) {
        accum.push(model);
      }
      return accum;
    }, []);
    if (models.length === 0) {
      console.error('no valid models loaded');
      return;
    }

    commit('setModelDtos', models);

    const curModel : SegmentedModelDto = models[0];

    await setCurrentModel(commit, curModel);
  },
  async runQuery({ state, commit, getters }) : Promise<any> {
    const curModel : SegmentedModelDto = await updateCurModelFromEditor(state, commit, getters);

    const query: ModelQueryDto = {
      model: extractModelText(curModel),
      query: state.curQuery,
      numberOfInitialSamples: state.numberOfInitialSamples,
      numberOfDiscreteValues: state.numberOfDiscreteValues,
    };

    commit('setQueryActive', true);

    let result: ExpressionResultDto | undefined;
    const { unwatch, signal } = queryAbortWatcher();
    try {
      result = await solve(query, { signal });
    } catch (err) {
      // errors already logged/displayed
    } finally {
      unwatch();
      commit('setQueryActive', false);
    }

    if (!result) {
      return;
    }

    const queryResultWrapper: QueryResultWrapper = {
      isFunctionQuery: false,
      expressionResult: result,
    };
    commit('setQueryResult', queryResultWrapper);
  },
  async runQueryFunction({ state, commit }, payload: RunQueryFunctionPayload) : Promise<any> {
    commit('setQueryActive', true);

    const lastQueryResult: QueryResultWrapper = cloneDeep(state.queryResults[0]);
    lastQueryResult.isFunctionQuery = true;
    let completionTimeInMillis = 0;

    let result: GraphRequestResultDto | undefined;
    const { unwatch, signal } = queryAbortWatcher();
    try {
      const now = Date.now();
      result = await fetchGraph(payload.request, { signal });
      completionTimeInMillis = Date.now() - now;
    } catch (err) {
      // errors already logged/displayed
    } finally {
      unwatch();
      commit('setQueryActive', false);
    }

    if (!result) {
      return;
    }

    const { expressionResult } = lastQueryResult;
    const xmVariables = [payload.request.xmVariable]; // in case there was an x-axis swap

    if (!expressionResult.graphQueryResultDto) {
      expressionResult.graphQueryResultDto = {
        xmVariables,
        graphVariableSets: [],
      };
    } else {
      expressionResult.graphQueryResultDto = {
        ...expressionResult.graphQueryResultDto,
        xmVariables,
      };
    }

    // Explicit assignment to set target to undefined if not contained in the result
    expressionResult.graphQueryResultDto.imageData = result.imageData;
    expressionResult.graphQueryResultDto.mapRegionNameToValue = result.mapRegionNameToValue;

    expressionResult.completionDate = getDate().toUTCString();
    expressionResult.queryDuration = completionTimeInMillis;
    expressionResult.answers = ['Function Completed'];

    lastQueryResult.expressionResult = expressionResult;
    lastQueryResult.queryGraphControlsCurValues = payload.curControlValues;
    commit('setQueryResult', lastQueryResult);
  },
  async xAxisSwap({ state, dispatch }, xAxisVariableName: string) : Promise<any> {
    // Get the variables from the original query
    const queryResultWrapper: QueryResultWrapper | undefined = state.queryResults.find(
        (qw: QueryResultWrapper) => !qw.isFunctionQuery,
    );
    if (!queryResultWrapper) {
      // Should never happen
      console.error('xAxisSwap query entry not found');
      return;
    }

    const origQueryResult:
      GraphQueryResultDto | undefined = queryResultWrapper.expressionResult.graphQueryResultDto;
    if (!origQueryResult) {
      // Should never happen
      console.error('xAxisSwap graphQueryResultDto entry not found');
      return;
    }

    const request: GraphRequestDto = {
      xmVariable: xAxisVariableName,
      graphVariableSets: cloneDeep(origQueryResult.graphVariableSets),
    };

    const payload: RunQueryFunctionPayload = {
      request,
    };
    await dispatch('runQueryFunction', payload);
  },
  async saveCurrentModelToDisk({ state, commit, getters }) : Promise<any> {
    const curModel : SegmentedModelDto = await updateCurModelFromEditor(state, commit, getters);

    downloadFile(curModel, `${curModel.name}.json`);
  },
  async changeCurrentModel({ state, commit }, modelName: string) : Promise<any> {
    const model : SegmentedModelDto = state.modelDtos[modelName];
    if (model) {
      await setCurrentModel(commit, model);
    } else {
      console.warn(`current model cannot be set because it does not exist: ${modelName}`);
    }
  },
  async loadModelsFromDisk({ state, commit }, payload: FileInfo[]) : Promise<any> {
    const models: SegmentedModelDto[] = payload.reduce((accum: SegmentedModelDto[], fileInfo) => {
          const model: SegmentedModelDto | null = validateAndCleanModel(JSON.parse(fileInfo.text));
          if (model) {
            accum.push(minimizeModel(model));
          }
          return accum;
        }, []);

    if (models.length) {
      commit('setModelDtos', Object.values(state.modelDtos).concat(models));
      await setCurrentModel(commit, models[0]);
      await Vue.nextTick();
      // We need to reset the cur model name since the UI model selection
      // component would have triggered a change after getting a new
      // list of model names.
      commit('setCurModelName', models[0].name);
    }
  },
};

export default actions;
