// @flow

type ValDef = {
  path: string[],
  name: string,
  params: Object,
}

type FieldRefMap = {
  [ref: string]: ValDef[],
};

export type {
  ValDef,
  FieldRefMap,
};

