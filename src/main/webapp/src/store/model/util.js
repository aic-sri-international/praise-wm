/* eslint-disable no-console */
// @flow

import type { ModelRuleDto, SegmentedModelDto } from './types';

export const EMPTY_MODEL_NAME = 'EMPTY';

export const emptyModelDto = () : SegmentedModelDto =>
  ({
    name: EMPTY_MODEL_NAME,
    description: '',
    declarations: '',
    rules: [{
      metadata: '',
      rule: '',
    }],
    queries: [],
  });

export const extractModelText = (model: SegmentedModelDto) : string =>
  `${model.declarations || ''}\n${model.rules.map(mr => mr.rule).join('\n')}\n`;

export const minimizeModel = (model: SegmentedModelDto) : SegmentedModelDto => {
  const coalesceRules = (rules: ModelRuleDto[]) : string =>
    rules.reduce((accum: string, modeRuleDto: ModelRuleDto) => {
      const lf = accum ? '\n\n' : '';
      // eslint-disable-next-line no-param-reassign
      accum += `${lf}${modeRuleDto.rule}`;
      return accum;
    }, '');

  const rulesString: string = coalesceRules(model.rules);
  let declarations: string = model.declarations || '';
  if (declarations && rulesString) {
    declarations += `\n\n${rulesString}`;
  } else if (rulesString) {
    declarations = rulesString;
  }

  //  Stripped metadata and coalesced declarations and rules
  return {
    name: model.name,
    description: model.description || '',
    declarations,
    rules: [],
    queries: model.queries,
  };
};

export const validateAndCleanModel = (model: SegmentedModelDto) : ?SegmentedModelDto => {
  if (!model.name) {
    console.error('model does not contain a name');
    return null;
  }

  if (model.rules && !Array.isArray(model.rules)) {
    console.error('model contains a rules field that is not an array');
    return null;
  }

  if (model.queries && !Array.isArray(model.queries)) {
    console.error('model contains a queries field that is not an array');
    return null;
  }

  return {
    name: model.name,
    description: model.description || '',
    declarations: model.declarations || '',
    rules: model.rules || [],
    queries: model.queries || [],
  };
};
