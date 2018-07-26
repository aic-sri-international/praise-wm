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

export type ExpressionResultDto = {
  query: string,
  queryDuration: number,
  completionDate: string,
  answers: string[],
  explanationTree: ExplanationTree,
}
