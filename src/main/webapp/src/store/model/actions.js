/* eslint-disable no-console */
// @flow
import type {
  SegmentedModelDto,
  ModelRuleDto,
  ModelQueryDto,
  ExpressionResultDto,
  EditorReferences,
} from '@/components/model/types';
import type { FileInfo } from '@/utils';
import {
  fetchSegmentedModels,
  solve,
} from '@/components/model/dataSourceProxy';
import { downloadFile } from '@/utils';
import MODEL from './constants';

const loadModels = async (): Promise<SegmentedModelDto[]> => {
  try {
    return await fetchSegmentedModels();
  } catch (err) {
  // errors already logged/displayed
    return [];
  }
};

const updateCurModelFromEditor = async (state, commit, getters, refs: EditorReferences):
    Promise<SegmentedModelDto> => {
  if (!state.curModelName) {
    throw Error('curModelName not set');
  }
  const curModel : SegmentedModelDto = getters[MODEL.GET.CUR_MODEL_DTO];
  if (!curModel) {
    throw Error(`model not found for ${state.curModelName}`);
  }
  const declarations = refs.dclEditor.getValue().trim();
  const rules: ModelRuleDto[] = await refs.segmentedModelEditor.getModelRules();

  curModel.declarations = declarations;
  curModel.rules = rules;
  commit(MODEL.SET.CUR_MODEL_DTO, curModel);
  return curModel;
};

const setCurrentModel = (commit, model: SegmentedModelDto) => {
  commit(MODEL.SET.CUR_MODEL_DTO, model);
  commit(MODEL.SET.CUR_MODEL_NAME, model.name);

  const curQuery =
      (model.queries && model.queries.length) ? model.queries[0] : '';
  commit(MODEL.SET.CUR_QUERY, curQuery);

  commit(MODEL.SET.CLEAR_QUERY_RESULT);
  commit(MODEL.SET.QUERY_RESULT_IX, -1);
};

const extractModelText = (model: SegmentedModelDto) : string =>
  `${model.declarations || ''}\n${model.rules.map(mr => mr.rule).join('\n')}\n`;

const minimizeModel = (model: SegmentedModelDto) : SegmentedModelDto => {
  const coalesceRules = (rules: ModelRuleDto[]) : string =>
    rules.reduce((accum: string, modeRuleDto: ModelRuleDto) => {
      const lf = accum ? '\n\n' : '';
      // eslint-disable-next-line no-param-reassign
      accum += `${lf}${modeRuleDto.rule}`;
      return accum;
    }, '');

  const rulesString: string = coalesceRules(model.rules);
  let declarations: string = model.declarations || '';
  if (declarations && rulesString) {
    declarations += `\n\n${rulesString}`;
  } else if (rulesString) {
    declarations = rulesString;
  }

  //  Strip metadata and coalesce declarations and rules
  return {
    name: model.name,
    description: model.description || '',
    declarations,
    rules: [],
    queries: model.queries,
  };
};

const validateAndCleanModel = (model: SegmentedModelDto) : ?SegmentedModelDto => {
  if (!model.name) {
    console.error('model does not contain a name');
    return null;
  }

  if (model.rules && !Array.isArray(model.rules)) {
    console.error('model contains a rules field that is not an array');
    return null;
  }

  if (model.queries && !Array.isArray(model.queries)) {
    console.error('model contains a queries field that is not an array');
    return null;
  }

  return {
    name: model.name,
    description: model.description || '',
    declarations: model.declarations || '',
    rules: model.rules || [],
    queries: model.queries || [],
  };
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
  async [MODEL.ACTION.RUN_QUERY]({ state, commit, getters }, payload: EditorReferences) {
    const curModel : SegmentedModelDto =
        await updateCurModelFromEditor(state, commit, getters, payload);

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
    commit(MODEL.SET.QUERY_RESULT_IX, 0);
  },
  async [MODEL.ACTION.SAVE_CURRENT_MODEL_TO_DISK](
    { state, commit, getters },
    payload: EditorReferences,
  ) {
    const curModel : SegmentedModelDto =
        await updateCurModelFromEditor(state, commit, getters, payload);

    downloadFile(curModel, `${curModel.name}.json`);
  },
  async [MODEL.ACTION.LOAD_MODEL_FROM_DISK]({ commit }, payload: FileInfo[]) {
    if (payload.length === 0) {
      return;
    }
    const { text } = payload[0];
    let model: ?SegmentedModelDto = validateAndCleanModel(JSON.parse(text));
    if (!model) {
      return;
    }

    model = minimizeModel(model);

    setCurrentModel(commit, model);
  },
};
