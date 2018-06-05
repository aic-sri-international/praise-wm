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
