// @flow

import type { ValDef } from './types';

type MsgMapFunc = (valdef: ValDef) => string;

type MsgMap = {
  [name: string]: MsgMapFunc,
};

const msgMap: MsgMap = {
  required: () => 'Required field',
  minLength: valdef => `Minimum Length=${valdef.params.min}`,
  maxLength: valdef => `Maximum Length=${valdef.params.max}`,
  requiredIf: valdef => `${valdef.name}`,
  numeric: () => 'All digits',
};

function defaultMsgMapFunc(valDef: ValDef): string {
  if (valDef.name.trim().indexOf(' ') !== -1) {
    // A custom message was used, so, return it.
    return valDef.name;
  }

  let params = '';

  if (valDef.params) {
    Object.keys(valDef.params).forEach((k) => {
      if (k !== 'type') {
        if (params) {
          params += ' ';
        }
        params += `${k}=${valDef.params[k]}`;
      }
    });
  }
  return params ? `${valDef.name}(${params})` : valDef.name;
}

function getValDefMessage(valDef: ValDef): string {
  const func: MsgMapFunc = msgMap[valDef.name];
  return func ? func(valDef) : defaultMsgMapFunc(valDef);
}

export default getValDefMessage;
