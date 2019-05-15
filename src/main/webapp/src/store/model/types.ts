
export const modelQueryDtoDefaults = {
  numberOfInitialSamples: 1000,
  numberOfDiscreteValues: 25,
};

export type ModelQueryOptions = {
  query: string;
  numberOfInitialSamples: number;
  numberOfDiscreteValues: number;
};


export type ModelQueryDto = { model: string } & ModelQueryOptions;

export type ModelRuleDto = {
  metadata?: string;
  rule: string;
};

export type ModelEditorData = {
  description?: string;
  declarations?: string;
  rules: ModelRuleDto[];
}

export type SegmentedModelDto = ModelEditorData & {
  name: string;
  queries?: string[];
}

export type ExplanationTree = {
  header?: string;
  footer?: string;
  subExplanations: ExplanationTree[];
}

export type GraphVariableRangeDto = {
  unitName?: string;
  unitSymbol?: string;
  first: number;
  last: number;
  step: number;
}

// When contained within a GraphRequestDto,
// if enums is present, it will usually contain a single entry in the array
export type GraphVariableSet = {
  name: string;
  enums?: string[];
  range?: GraphVariableRangeDto;
}

export type GraphRequestDto = {
  xmVariable: string;
  graphVariableSets: GraphVariableSet[];
}

export type GraphRequestResultDto = {
  imageData?: string;
  mapRegionNameToValue?: { [key:string]: number };
}

export type GraphQueryVariableResults = {
  // The 1st entry in the list is the xm variable name used to create the
  // imageData Graph creation is supported for others in the list
  xmVariables: string[];
  graphVariableSets: GraphVariableSet[];
}

export type GraphQueryResultDto = GraphQueryVariableResults & GraphRequestResultDto;

export type ExpressionResultDto = {
  query: string;
  queryDuration: number;
  completionDate: string;
  answers: string[];
  explanationTree: ExplanationTree;
  graphQueryResultDto?: GraphQueryResultDto;
}

export type QueryGraphControlsCurValues = {
  sliderRefNameToIndex?: { [key:string]: number | number[] };
  inputFieldIndexToValue?: { [key:number]: string };
}

export type QueryResultWrapper = {
  isFunctionQuery: boolean;
  expressionResult: ExpressionResultDto;
  queryGraphControlsCurValues?: QueryGraphControlsCurValues;
}

export enum EditorTransition {
  NONE = 'NONE',
  LOAD = 'LOAD', // The editor manager should load the editor from the store
  STORE = 'STORE' // The editor manager save the editor text into the store
}

export type UpdateModelDtoPayload = {
  modelName: string;
  modelEditorData: ModelEditorData;
}

export type RunQueryFunctionPayload = {
  request: GraphRequestDto,
  curControlValues?: QueryGraphControlsCurValues,
};

export interface VuexModelState {
  editorTransition: EditorTransition;
  curQuery: string;
  curModelName: string;
  modelDtos: { [key:string]: SegmentedModelDto };
  queryResults: QueryResultWrapper[];
  queryResultsIx: number;
  queryStartTime: number;
  abortQueryFlag: boolean;
  numberOfInitialSamples: number;
  numberOfDiscreteValues: number;
}
