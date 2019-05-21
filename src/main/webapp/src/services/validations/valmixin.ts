import Vue from 'vue';
import Component from 'vue-class-component';
import { oneLine } from 'common-tags';
import isPlainObject from 'lodash/isPlainObject';
import isFunction from 'lodash/isFunction';
import isBoolean from 'lodash/isBoolean';
import isNull from 'lodash/isNull';
import isNil from 'lodash/isNil';
import isString from 'lodash/isString';
import get from 'lodash/get';
import { Validations, ValParams } from './types';

type ValEntry = ValParams & {
  path: string[];
}

const verifyValEntry = (e: ValEntry) => {
  if (!isNil(e.required) && !isBoolean(e.required) && !isFunction(e.required)) {
    throw Error(`Value for path is neither a boolean nor a function: ${e.path.join('.')}`);
  }

  if (!isNil(e.validator) && !isFunction(e.validator)) {
    throw Error(`Value for path is not a function: ${e.path.join('.')}`);
  }


  if (typeof e.ref !== 'object') {
    throw Error(`Value for "ref" is not an object: ${e.path.join('.')}`);
  }

  if (!isFunction(e.ref.focus)) {
    throw Error(oneLine`Referenced "ref" object does not have a focus() method:
                          ${[...e.path, 'ref'].join('.')}`);
  }

  if (isNil(e.required) && isNil(e.validator)) {
    throw Error(oneLine`Entry contains neither a 'required' nor a 'validator' field: 
                          ${e.path.slice(1, e.path.length).join('.')}`);
  }
};

function buildEntries(validations: Validations): ValEntry[] {
  const toValEntry = (path: string[], v: ValParams): ValEntry => {
    const valEntry: ValEntry = {
      path: [...path],
      required: v.required,
      validator: v.validator,
      ref: v.ref,
    };

    verifyValEntry(valEntry);

    return valEntry;
  };

  const entries: ValEntry[] = [];

  const traverse = (vals: Validations, path: string[]) => {
    Object.entries(vals).forEach((keyValArr) => {
      const [key, val] = keyValArr;

      if (isNil(val)) {
        throw Error('validations object passed to validateForm method is not valid. '
          + '"ref" field value is probably missing');
      }
      if (isPlainObject(val) && (val as ValParams).ref) {
        entries.push(toValEntry([...path, key], val as ValParams));
      } else {
        path.push(key);
        traverse(val as Validations, path);
      }
    });
  };

  traverse(validations, []);

  return entries;
}

@Component
export default class ValMixin extends Vue {
  validateForm(validations: Validations): boolean {
    const entries: ValEntry[] = buildEntries(validations);

    const errIndex = entries.findIndex((valEntry) => {
      if ((isBoolean(valEntry.required) && valEntry.required)
        || (isFunction(valEntry.required) && valEntry.required())) {
        const fieldValue: any = get(this, valEntry.path);
        if (isNull(fieldValue) || (isString(fieldValue) && fieldValue.trim() === '')) {
          this.$$.showToastError('Required Field');
          valEntry.ref.focus();
          return true;
        }
      }

      if (valEntry.validator) {
        const errMsg = valEntry.validator();
        if (errMsg) {
          this.$$.showToastError(errMsg);
          valEntry.ref.focus();
          return true;
        }
      }

      return false;
    });

    return errIndex === -1;
  }
}
