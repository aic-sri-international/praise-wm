/* eslint-disable no-console */
// @flow
import Vue from 'vue';
import { oneLine } from 'common-tags';
import type {
  SegmentedModelDto,
  ModelQueryDto,
  ExpressionResultDto,
} from '@/components/model/types';
import type { FileInfo } from '@/utils';
import {
  fetchSegmentedModels,
  solve,
} from '@/components/model/dataSourceProxy';
import { downloadFile } from '@/utils';
import { editorTransitions } from './types';
import MODEL from './constants';
import { validateAndCleanModel, extractModelText, minimizeModel } from './util';

const loadModels = async (): Promise<SegmentedModelDto[]> => {
  try {
    return await fetchSegmentedModels();
  } catch (err) {
  // errors already logged/displayed
    return [];
  }
};

const wait = ms => new Promise(resolve => setTimeout(resolve, ms));

const waitForTransitionToComplete = async (getters) => {
  const max = 20;
  const ms = 5;
  let count = 0;

  // This should usually be sufficient
  await Vue.nextTick();

  while (count <= max && getters[MODEL.GET.EDITOR_TRANSITION] !== editorTransitions.NONE) {
    // eslint-disable-next-line no-await-in-loop
    await wait(ms);
    count += 1;
  }

  if (count > max) {
    console.error(oneLine`Editor has not transitioned to 
                            ${editorTransitions.NONE} after ${max * ms} ms`);
  }

  return Promise.resolve();
};


const updateCurModelFromEditor = async (state, commit, getters):
    Promise<SegmentedModelDto> => {
  if (!state.curModelName) {
    throw Error('curModelName not set');
  }
  const curModel : SegmentedModelDto = getters[MODEL.GET.CUR_MODEL_DTO];
  if (!curModel) {
    throw Error(`model not found for ${state.curModelName}`);
  }
  commit(MODEL.SET.EDITOR_TRANSITION, editorTransitions.STORE);

  await waitForTransitionToComplete(getters);

  return getters[MODEL.GET.CUR_MODEL_DTO];
};

const setCurrentModel = async (commit, model: SegmentedModelDto) => {
  commit(MODEL.SET.MODEL_DTO, model);
  // Make sure the watcher for MODEL.GET.MODEL_NAMES completes before
  // the watcher for MODEL.GET.CUR_MODEL_NAME is called.
  await Vue.nextTick();
  commit(MODEL.SET.CUR_MODEL_NAME, model.name);

  const curQuery =
      (model.queries && model.queries.length) ? model.queries[0] : '';
  commit(MODEL.SET.CUR_QUERY, curQuery);

  commit(MODEL.SET.CLEAR_QUERY_RESULTS);
  commit(MODEL.SET.EDITOR_TRANSITION, editorTransitions.LOAD);
};

export default {
  async [MODEL.ACTION.INITIALIZE]({ commit }) {
    let models: SegmentedModelDto[] = await loadModels();
    if (models.length === 0) {
      // Probably encountered an error that has been logged, so just return
      return;
    }

    models = models.reduce((accum: SegmentedModelDto[], cur: SegmentedModelDto) => {
      const model: ?SegmentedModelDto = validateAndCleanModel(cur);
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
    const curModel : SegmentedModelDto =
        await updateCurModelFromEditor(state, commit, getters);

    downloadFile(curModel, `${curModel.name}.json`);
  },
  async [MODEL.ACTION.RUN_QUERY]({ state, commit, getters }) {
    const curModel : SegmentedModelDto =
        await updateCurModelFromEditor(state, commit, getters);

    const query: ModelQueryDto = {
      model: extractModelText(curModel),
      query: state.curQuery,
      numberOfInitialSamples: state.numberOfInitialSamples,
      numberOfDiscreteValues: state.numberOfDiscreteValues,
    };

    commit(MODEL.SET.IS_QUERY_ACTIVE, true);

    let result: ?ExpressionResultDto;
    try {
      result = await solve(query);
    } catch (err) {
      // errors already logged/displayed
    }
    commit(MODEL.SET.IS_QUERY_ACTIVE, false);

    if (!result) {
      return;
    }
    commit(MODEL.SET.QUERY_RESULT, result);
  },
  async [MODEL.ACTION.SAVE_CURRENT_MODEL_TO_DISK]({ state, commit, getters }) {
    const curModel : SegmentedModelDto =
        await updateCurModelFromEditor(state, commit, getters);

    downloadFile(curModel, `${curModel.name}.json`);
  },
  async [MODEL.ACTION.CHANGE_CURRENT_MODEL]({ state, commit }, modelName: string) {
    const model : SegmentedModelDto = state.modelDtos[modelName];
    if (model) {
      setCurrentModel(commit, model);
    } else {
      console.warn(`current model cannot be set because it does not exist: ${modelName}`);
    }
  },
  async [MODEL.ACTION.LOAD_MODELS_FROM_DISK]({ state, commit }, payload: FileInfo[]) {
    const models: SegmentedModelDto[]
        = payload.reduce((accum, fileInfo) => {
          const model: ?SegmentedModelDto
              = validateAndCleanModel(JSON.parse(fileInfo.text));
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
      setCurrentModel(commit, models[0]);
    }
  },
};
