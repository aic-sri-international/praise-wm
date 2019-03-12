// @flow

// eslint-disable-next-line
export const modelQueryDtoDefaults = {
  numberOfInitialSamples: 1000,
  numberOfDiscreteValues: 25,
};

export const getRangeLabel = (num: number, textToAppend: ?string) => {
  let numOut = '';
  if (num !== undefined) {
    numOut = Number.isInteger(num) ? num : num.toFixed(2);
  }
  return textToAppend ? `${numOut}${textToAppend}` : `${numOut}`;
};

export type ModelPageDto = {
  model: string,
  queries: string[],
};

export type ModelPagesDto = {
  name: string,
  pages: ModelPageDto[],
};

export type ModelQueryOptions = {
  query: string,
  numberOfInitialSamples: number,
  numberOfDiscreteValues: number,
};

export type EditorReferences = {
  dclEditor: Object,
  segmentedModelEditor: Object,
};

export type ModelQueryDto = { model: string } & ModelQueryOptions;

export type FormattedPageModelDto = {
  name: string,
  text: string,
};

export type ModelRuleDto = {
  metadata?: string,
  rule: string,
};

export type ModelRuleWrapper = {
  modelRule: ModelRuleDto,
  toggleMetadata: boolean,
  emitData: boolean,
};

export type SegmentedModelDto = {
  name: string,
  description?: string,
  declarations?: string,
  rules: ModelRuleDto[],
  queries?: string[],
}

export type ExplanationTree = {
  header?: string,
  footer?: string,
  subExplanations: ExplanationTree[],
}

export type GraphVariableRangeDto = {
  unitName?: string,
  unitSymbol?: string,
  first: number,
  last: number,
  step: number,
}

// When contained within a GraphRequestDto,
// if enums is present, it will usually contain a single entry in the array
export type GraphVariableSet = {
  name: string,
  enums?: string[],
  range?: GraphVariableRangeDto,
}

export type GraphRequestDto = {
  xmVariable: string,
  graphVariableSets: GraphVariableSet[],
}

export type GraphRequestResultDto = {
  imageData?: string,
  mapRegionNameToValue?: { [string]: number },
}

export type GraphQueryVariableResults = {
  // The 1st entry in the list is the xm variable name used to create the
  // imageData Graph creation is supported for others in the list
  xmVariables: string[],
  graphVariableSets: GraphVariableSet[],
}

export type GraphQueryResultDto = GraphQueryVariableResults & GraphRequestResultDto;

export type ExpressionResultDto = {
  query: string,
  queryDuration: number,
  completionDate: string,
  answers: string[],
  explanationTree: ExplanationTree,
  graphQueryResultDto?: GraphQueryResultDto,
}
