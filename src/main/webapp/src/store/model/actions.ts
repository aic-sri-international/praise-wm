/* eslint-disable no-console */

import Vue from 'vue';
import cloneDeep from 'lodash/cloneDeep';
import { oneLine } from 'common-tags';
// eslint-disable-next-line import/no-cycle
import { abortWatcher } from '@/store/store';
// eslint-disable-next-line import/no-cycle
import { FileInfo, downloadFile, getDate } from '@/utils';
// eslint-disable-next-line import/no-cycle
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
  QueryGraphControlsCurValues,
  GraphRequestDto,
  GraphRequestResultDto,
  GraphQueryResultDto,
  EditorTransition,
  VuexModelState,
  RunQueryFunctionPayload,
} from './types';
import MODEL from './constants';
import { validateAndCleanModel, extractModelText, minimizeModel } from './util';
import { ActionTree, Commit } from 'vuex';
import { RootState } from '@/store/types';

const queryAbortWatcher = (): { unwatch: Function, signal: AbortSignal } => abortWatcher(
  MODEL.MODULE,
  'abortQueryFlag',
  MODEL.SET.ABORT_QUERY,
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

const waitForTransitionToComplete = async (state: VuexModelState) => {
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
  const curModel : SegmentedModelDto = getters[MODEL.GET.CUR_MODEL_DTO];
  if (!curModel) {
    throw Error(`model not found for ${state.curModelName}`);
  }
  commit(MODEL.SET.EDITOR_TRANSITION, EditorTransition.STORE);

  await waitForTransitionToComplete(state);

  return getters[MODEL.GET.CUR_MODEL_DTO];
};

const setCurrentModel = async (commit: Commit, model: SegmentedModelDto) => {
  commit(MODEL.SET.MODEL_DTO, model);
  // Make sure the watcher for MODEL.GET.MODEL_NAMES completes before
  // the watcher for MODEL.GET.CUR_MODEL_NAME is called.
  await Vue.nextTick();
  commit(MODEL.SET.CUR_MODEL_NAME, model.name);

  const curQuery = (model.queries && model.queries.length) ? model.queries[0] : '';
  commit(MODEL.SET.CUR_QUERY, curQuery);

  commit(MODEL.SET.CLEAR_QUERY_RESULTS);
  commit(MODEL.SET.EDITOR_TRANSITION, EditorTransition.LOAD);
};

const actions: ActionTree<VuexModelState, RootState> = {
  async [MODEL.ACTION.INITIALIZE]({ commit }) {
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

    commit(MODEL.SET.MODEL_DTOS, models);

    const curModel : SegmentedModelDto = models[0];

    setCurrentModel(commit, curModel);
  },
  async [MODEL.ACTION.SAVE_CURRENT_MODEL_TO_DISK]({ state, commit, getters }) {
    const curModel : SegmentedModelDto = await updateCurModelFromEditor(state, commit, getters);

    downloadFile(curModel, `${curModel.name}.json`);
  },
  async [MODEL.ACTION.RUN_QUERY]({ state, commit, getters }) {
    const curModel : SegmentedModelDto = await updateCurModelFromEditor(state, commit, getters);

    const query: ModelQueryDto = {
      model: extractModelText(curModel),
      query: state.curQuery,
      numberOfInitialSamples: state.numberOfInitialSamples,
      numberOfDiscreteValues: state.numberOfDiscreteValues,
    };

    commit(MODEL.SET.IS_QUERY_ACTIVE, true);

    let result: ExpressionResultDto | undefined;
    const { unwatch, signal } = queryAbortWatcher();
    try {
      result = await solve(query, { signal });
    } catch (err) {
      // errors already logged/displayed
    } finally {
      unwatch();
      commit(MODEL.SET.IS_QUERY_ACTIVE, false);
    }

    if (!result) {
      return;
    }

    const queryResultWrapper: QueryResultWrapper = {
      isFunctionQuery: false,
      expressionResult: result,
    };
    commit(MODEL.SET.QUERY_RESULT, queryResultWrapper);
  },
  async [MODEL.ACTION.RUN_QUERY_FUNCTION]({ state, commit }, payload: RunQueryFunctionPayload) {
    commit(MODEL.SET.IS_QUERY_ACTIVE, true);

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
      commit(MODEL.SET.IS_QUERY_ACTIVE, false);
    }

    if (!result) {
      return;
    }

    // eslint-disable-next-line prefer-destructuring
    const expressionResult: ExpressionResultDto = lastQueryResult.expressionResult;
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

    // Explict assignment to set target to undefined if not contained in the result
    expressionResult.graphQueryResultDto.imageData = result.imageData;
    expressionResult.graphQueryResultDto.mapRegionNameToValue = result.mapRegionNameToValue;

    expressionResult.completionDate = getDate().toUTCString();
    expressionResult.queryDuration = completionTimeInMillis;
    expressionResult.answers = ['Function Completed'];

    lastQueryResult.expressionResult = expressionResult;
    lastQueryResult.queryGraphControlsCurValues = payload.curControlValues;
    commit(MODEL.SET.QUERY_RESULT, lastQueryResult);
  },
  async [MODEL.ACTION.X_AXIS_SWAP]({ state, dispatch }, xAxisVariableName: string) {
    // Get the variables from the original query
    const queryResultWrapper: QueryResultWrapper | undefined = state.queryResults.find(
        (qw: QueryResultWrapper) => !qw.isFunctionQuery,
    );
    if (!queryResultWrapper) {
      // Should never happen
      console.error('MODEL.ACTION.X_AXIS_SWAP query entry not found');
      return;
    }

    const origQueryResult:
      GraphQueryResultDto | undefined = queryResultWrapper.expressionResult.graphQueryResultDto;
    if (!origQueryResult) {
      // Should never happen
      console.error('MODEL.ACTION.X_AXIS_SWAP graphQueryResultDto entry not found');
      return;
    }

    const request: GraphRequestDto = {
      xmVariable: xAxisVariableName,
      graphVariableSets: cloneDeep(origQueryResult.graphVariableSets),
    };

    const payload: RunQueryFunctionPayload = {
      request,
    };
    await dispatch(MODEL.ACTION.RUN_QUERY_FUNCTION, payload);
  },
  async [MODEL.ACTION.SAVE_CURRENT_MODEL_TO_DISK]({ state, commit, getters }) {
    const curModel : SegmentedModelDto = await updateCurModelFromEditor(state, commit, getters);

    downloadFile(curModel, `${curModel.name}.json`);
  },
  async [MODEL.ACTION.CHANGE_CURRENT_MODEL]({ state, commit }, modelName: string) {
    const model : SegmentedModelDto = state.modelDtos[modelName];
    if (model) {
      await setCurrentModel(commit, model);
    } else {
      console.warn(`current model cannot be set because it does not exist: ${modelName}`);
    }
  },
  async [MODEL.ACTION.LOAD_MODELS_FROM_DISK]({ state, commit }, payload: FileInfo[]) {
    const models: SegmentedModelDto[] = payload.reduce((accum: SegmentedModelDto[], fileInfo) => {
          const model: SegmentedModelDto | null = validateAndCleanModel(JSON.parse(fileInfo.text));
          if (model) {
            accum.push(minimizeModel(model));
          }
          return accum;
        }, []);

    if (models.length) {
      commit(
        MODEL.SET.MODEL_DTOS,
        Object.values(state.modelDtos).concat(models),
      );
      await setCurrentModel(commit, models[0]);
      await Vue.nextTick();
      // We need to reset the cur model name since the UI model selection
      // component would have triggered a change after getting a new
      // list of model names.
      commit(MODEL.SET.CUR_MODEL_NAME, models[0].name);
    }
  },
};

export default actions;
