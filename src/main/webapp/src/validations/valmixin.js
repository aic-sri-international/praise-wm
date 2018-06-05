// @flow
import { validationMixin } from 'vuelidate';
import { SnotifyPosition } from 'vue-snotify';

import getValDefMessage from './messageMapper';
import type { FieldRefMap, ValDef } from './types';

type InvalidInfo = {
  refName: string,
  fieldName: string,
  message: string,
  ref: Object,
};

const debug = false;
const VAL = 'VALIDATOR';
const VAL_IGNORE = 'VALIDATOR_IGNORED_FIELDS';
const VAL_NO_VALIDATION = 'VALIDATOR_SKIP_ALL_VALIDATION';
const FORM_LBLS = 'formLables';

class Validator {
  componentName;
  fieldRefMap: FieldRefMap = {};
  fieldRefLabelMap: { [refName: string]: string } = {};
  fieldRefInputMap: { [refName: string]: Object } = {};
  vueThis;

  constructor(vueThis) {
    this.vueThis = vueThis;
    this.componentName = vueThis.$options.name;

    if (!vueThis.$v) {
      throw new Error('valmixin must be used in a component that contains vuelidate'
          + ` validations. ${this.componentName} does not have a $v validation object.`);
    }

    this.initFieldRefMap();
    if (debug) {
      this.log('fieldRefMap:', this.fieldRefMap);
    }
    this.validateRefs();

    this.initFieldRefInputMap();

    this.initTooltips();

    this.initFieldRefLabelMap();
  }

  static modelDataPathToVueRefName(modelPath: string[]): string {
    return `${modelPath.join('_')}_ref`;
  }

  static vueRefNameToModelDataPath(refName: string): string[] {
    const path = refName.split('_');
    // remove the 'ref' text
    path.pop();
    return path;
  }

  static buildValDefEntryForError(refName: string, state: Object) {
    // Find validation error name
    const errNameArr = Object.keys(state)
      .filter(k => k.charAt(0) !== '$' && state[k] === false);
    if (!errNameArr.length) {
      return null;
    }

    // There's only one at a time in the current vuelidate version
    const valErrName: string = errNameArr[0];
    let params: Object = state.$params[valErrName];

    if (!params) {
      params = {};
    }

    return {
      path: Validator.vueRefNameToModelDataPath(refName),
      name: valErrName,
      params: { ...params },
    };
  }

  getFieldsValidationState(refName: string) {
    const [valdef] = this.fieldRefMap[refName];
    let ref = this.vueThis.$v;
    valdef.path.forEach((seg) => {
      ref = ref[seg];
    });
    return ref;
  }

  getFieldsFormsLabel(refName: string) {
    const [valdef] = this.fieldRefMap[refName];
    let entry = this.vueThis[FORM_LBLS];
    if (!entry) {
      const msg = `corresponding data.${FORM_LBLS} fields do not exist`;
      throw Error(`${this.msgPrefix()} ${msg}`);
    }
    valdef.path.forEach((seg) => {
      entry = entry[seg];
      if (entry === undefined) {
        const msg = `corresponding data.${FORM_LBLS} field does not exist for ${refName} `;
        throw Error(`${this.msgPrefix()} ${msg}`);
      }
    });
    return entry;
  }

  initFieldRefMap() {
    this.fieldRefMap = this.vueThis.$v.$flattenParams()
      .reduce((accum: FieldRefMap, value: ValDef): FieldRefMap => {
        const key = Validator.modelDataPathToVueRefName(value.path);
        if (this.vueThis[VAL_IGNORE].includes(key)) {
          return accum;
        }

        let arr: ValDef[] = accum[key];
        if (arr === undefined) {
          arr = [];
          // eslint-disable-next-line no-param-reassign
          accum[key] = arr;
        }
        arr.push(value);
        return accum;
      }, {});
  }

  initFieldRefInputMap() {
    Object.keys(this.fieldRefMap).forEach((refName) => {
      this.fieldRefInputMap[refName] = this.getInputElement(refName);
    });
  }

  initFieldRefLabelMap() {
    Object.keys(this.fieldRefMap).forEach((refName) => {
      this.fieldRefLabelMap[refName] = this.getFieldsFormsLabel(refName);
    });
  }

  validateRefs() {
    const refs = this.vueThis.$refs;
    const missing = Object.keys(this.fieldRefMap)
      .filter((e) => {
        const ref = refs[e];
        return ref === undefined;
      });

    let msg = 'The following data fields are missing a '
        + 'corresponding UI input form component ref property:';

    if (missing.length) {
      const field = e => this.fieldRefMap[e][0].path.join('.');
      missing.forEach((key) => {
        msg += `\n  ${field(key)} => ${key} `;
      });
      throw Error(`${this.msgPrefix()} ${msg}`);
    }
  }

  buildFormLabelsMap() {
    const refs = this.vueThis.$refs;
    const missing = Object.keys(this.fieldRefMap)
      .filter((e) => {
        const ref = refs[e];
        return ref === undefined;
      });

    let msg = 'The following data fields are missing a '
        + 'corresponding UI input form component ref property:';

    if (missing.length) {
      const field = e => this.fieldRefMap[e][0].path.join('.');
      missing.forEach((key) => {
        msg += `\n  ${field(key)} => ${key} `;
      });
      throw Error(`${this.msgPrefix()} ${msg}`);
    }
  }

  getInputElement(refName: string): Object {
    let inputRef;

    const findInputElement = (ref) => {
      if (!ref) {
        return;
      }
      if (ref.tagName === 'INPUT') {
        inputRef = ref;
      } else if (ref.$el) {
        findInputElement(ref.$el);
      } else if (ref.children) {
        for (let i = 0;
          inputRef === undefined && i < ref.children.length;
          i += 1) {
          findInputElement(ref.children[i]);
        }
      }
    };

    findInputElement(this.vueThis.$refs[refName]);
    if (inputRef === undefined) {
      throw Error(`${this.msgPrefix()} input element not found for ref ${refName}`);
    }
    return inputRef;
  }

  initTooltips() {
    this.vueThis.$nextTick(() => {
      Object.keys(this.fieldRefMap).forEach((refName) => {
        const el = this.getInputElement(refName);
        // If a title has already been specified in the form's template, we will
        // assume that it should remain as-is.
        if (!el.title) {
          el.title = this.getTooltipText(refName);
        }
      });
    });
  }

  validateForm() {
    const invalidFields: InvalidInfo[] = [];

    Object.keys(this.fieldRefMap).forEach((refName: string) => {
      const state = this.getFieldsValidationState(refName);
      if (state.$invalid) {
        const valdef: ?ValDef = Validator.buildValDefEntryForError(
          refName,
          state,
        );
        const message = valdef ? getValDefMessage(valdef) : 'Field error';
        invalidFields.push({
          refName,
          fieldName: this.fieldRefLabelMap[refName],
          message,
          ref: this.fieldRefInputMap[refName],
        });
        if (debug) {
          this.log('state:', state);
        }
      }
    });

    if (invalidFields.length) {
      this.vueThis.$nextTick(() => {
        this.reportInvalidFields(invalidFields, 6000);
        invalidFields[0].ref.focus();
      });
    }

    return invalidFields.length === 0;
  }

  getTooltipText(refName: string) {
    const valDefs: ValDef[] = this.fieldRefMap[refName];
    return valDefs.map(valdef => getValDefMessage(valdef)).join('\n');
  }

  reportInvalidFields(invFields: InvalidInfo[], duration: ?number) {
    let html = '<div style="margin-right: -60px"><table class="table table-sm" style="width: 100%">'
        + '<tr><th colspan="2" style="font-size: large">Validation Exceptions</th></tr>'
        + '<tr><th style="text-align: left">Field</th><th style="text-align: left">Message</th></tr>';

    invFields.forEach((e) => {
      html += `<tr><td style="text-align: left; font-size: medium">${e.fieldName}</td>
              <td style="text-align: left; font-size: medium">${e.message}</td></tr>`;
    });
    html += '</table></div>';

    this.vueThis.$snotify.html(html, {
      timeout: duration,
      showProgressBar: true,
      closeOnClick: true,
      pauseOnHover: true,
      position: SnotifyPosition.leftCenter,
    });
  }

  log(...args) {
    const text = args.reduce((msg, value) =>
      msg + JSON.stringify(value, null, 2), '');

    // eslint-disable-next-line no-console
    console.log(`${this.msgPrefix()} ${text}`);
  }

  msgPrefix() {
    return `valmixin for component '${this.componentName}':`;
  }
}

export default {
  name: 'valmixin',
  mixins: [validationMixin],
  data() {
    return {
      [VAL]: {},
      [VAL_IGNORE]: [],
      [VAL_NO_VALIDATION]: false,
    };
  },
  methods: {
    validateForm() {
      if (this[VAL_NO_VALIDATION]) {
        throw Error('validateForm called when disableValidationFramework was set');
      }
      return this[VAL].validateForm();
    },
    skipFormValidation(fields: string[]) {
      fields.forEach((fieldName) => {
        this[VAL_IGNORE].push(`${fieldName.replace(/\./g, '_')}_ref`);
      });
    },
    disableValidationFramework() {
      this[VAL_NO_VALIDATION] = true;
    },
    enableValidationFramework() {
      if (this[VAL_NO_VALIDATION]) {
        this[VAL_NO_VALIDATION] = false;
        this[VAL] = new Validator(this);
      }
    },
  },
  mounted() {
    if (!this[VAL_NO_VALIDATION]) {
      this[VAL] = new Validator(this);
    }
  },
};
