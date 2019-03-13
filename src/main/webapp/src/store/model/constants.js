// @flow

const MODEL_VXC = {
  MODULE: 'model',
  GET: {
    EDITOR_TRANSITION: 'editorTransition',
    CUR_MODEL_NAME: 'curModelName',
    CUR_QUERY: 'curQuery',
    CUR_MODEL_DTO: 'curModelDto',
    QUERY_RESULTS: 'queryResults',
    CUR_QUERY_RESULT: 'curQueryResult',
    IS_QUERY_ACTIVE: 'isQueryActive',
    NUMBER_OF_INITIAL_SAMPLES: 'numberOfInitialSamples',
    NUMBER_OF_DISCRETE_VALUES: 'numberOfDiscreteValues',
  },
  SET: {
    EDITOR_TRANSITION: 'setEditorTransition',
    MODEL_DTOS: 'setModelDtos',
    CUR_MODEL_DTO: 'setCurModelDto',
    CUR_MODEL_NAME: 'setCurModelName',
    CUR_QUERY: 'setCurQuery',
    QUERY_RESULT: 'setQueryResult',
    QUERY_RESULT_IX: 'setQueryResultIx',
    CLEAR_QUERY_RESULT: 'clearQueryResult',
    IS_QUERY_ACTIVE: 'setQueryActive',
    NUMBER_OF_INITIAL_SAMPLES: 'setNumberOfInitialSamples',
    NUMBER_OF_DISCRETE_VALUES: 'setNumberOfDiscreteValues',
  },
  ACTION: {
    INITIALIZE: 'initialize',
    RUN_QUERY: 'runQuery',
    CANCEL_QUERY: 'cancelQuery',
    SAVE_CURRENT_MODEL_TO_DISK: 'saveCurrentModelToDisk',
    LOAD_MODEL_FROM_DISK: 'loadModelFromDisk',
  },
};

export default MODEL_VXC;
