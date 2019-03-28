// @flow

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

export type ModelQueryDto = { model: string } & ModelQueryOptions;

export type FormattedPageModelDto = {
  name: string,
  text: string,
};
