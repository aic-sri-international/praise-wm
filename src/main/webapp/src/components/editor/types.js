// @flow

export type ModelPageDto = {
  model: string,
  queries: string[],
};

export type ModelPagesDto = {
  name: string,
  pages: ModelPageDto[],
};

export type ModelQueryDto = {
  query: string,
  model: string,
};

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
  first: number,
  last: number,
  step: number,
}

// When contained within a GraphRequestDto,
// if enums is present, it must always contain a single entry in the array
export type GraphVariableSet = {
  name: string,
  enums?: string[],
  range?: GraphVariableRangeDto,
}

export type GraphRequestDto = {
  xmVariable: string,
  graphVariableSets: GraphVariableSet[],
}

export type GraphQueryResultDto = {
  // The 1st entry in the list is the xm variable name used to create the
  // imageData Graph creation is supported for others in the list
  xmVariables?: string[],
  graphVariableSets: GraphVariableSet[],
  imageData: string,
}

export type ExpressionResultDto = {
  query: string,
  queryDuration: number,
  completionDate: string,
  answers: string[],
  explanationTree: ExplanationTree,
  graphQueryResultDto: GraphQueryResultDto,
}
