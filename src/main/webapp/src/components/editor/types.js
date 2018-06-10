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

export type ModelRule = {
  metadata: string,
  rule: string,
};

export type ModelRuleWrapper = {
  modelRule: ModelRule,
  toggleMetadata: boolean,
  emitData: boolean,
};

export type SegmentedModel = {
  name: string,
  description: string,
  declarations: string,
  rules: ModelRule[],
  queries: string[],
}
