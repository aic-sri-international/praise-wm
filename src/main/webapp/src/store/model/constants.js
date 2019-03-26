// @flow

const MODEL_VXC = {
  MODULE: 'model',
  GET: {
    MODEL_NAMES: 'modelNames',
    CUR_QUERIES: 'curQueries',
    CUR_MODEL_DTO: 'curModelDto',
    CUR_RESULT: 'curResult',
    CUR_RESULT_WRAPPER: 'curResultWrapper',
    IS_QUERY_ACTIVE: 'isQueryActive',
    DISPLAY_CHART: 'displayChart',
  },
  SET: {
    EDITOR_TRANSITION: 'setEditorTransition',
    MODEL_DTOS: 'ModelDtos',
    MODEL_DTO: 'setModelDto',
    UPDATE_MODEL_DTO: 'updateModelDto',
    CUR_MODEL_NAME: 'setCurModelName',
    CUR_QUERY: 'setCurQuery',
    CUR_MODEL_QUERIES: 'setCurModelQueries',
    QUERY_RESULT: 'setQueryResult',
    QUERY_RESULTS_IX: 'setQueryResultsIx',
    CLEAR_QUERY_RESULTS: 'clearQueryResults',
    IS_QUERY_ACTIVE: 'setQueryActive',
    NUMBER_OF_INITIAL_SAMPLES: 'setNumberOfInitialSamples',
    NUMBER_OF_DISCRETE_VALUES: 'setNumberOfDiscreteValues',
  },
  ACTION: {
    INITIALIZE: 'initialize',
    RUN_QUERY: 'runQuery',
    RUN_QUERY_FUNCTION: 'runQueryFunction',
    X_AXIS_SWAP: 'xAxisSwap',
    CANCEL_QUERY: 'cancelQuery',
    SAVE_CURRENT_MODEL_TO_DISK: 'saveCurrentModelToDisk',
    CHANGE_CURRENT_MODEL: 'changeCurrentModel',
    LOAD_MODELS_FROM_DISK: 'loadModelsFromDisk',
  },
};

export default MODEL_VXC;
