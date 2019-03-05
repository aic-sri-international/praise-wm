<template>
  <div class="container">
    <div class="row">
        <input style="text-align: left"
                 :style="currentStyle"
                 ref="input"
                 v-tippy
                 :title="tooltip"
                 :disabled="disabled"
                 @blur="onBlur"
                 @input="updateDigitsValue($event.target.value)"/>
        <div class="column shift-left">
          <div class="row">
            <b-btn variant="outline-secondary"
                   class="button-class"
                   @blur="onBlur"
                   @click.stop="onStepClick($event, true)"
                   :disabled="upArrowIsDisabled">
              <div class="arrow-up"></div>
            </b-btn>
          </div>
          <span style="height: .5px"></span>
            <div class="row">
              <b-btn variant="outline-secondary"
                     ref="downButton_ref"
                     class="button-class"
                     @blur="onBlur"
                     @click.stop="onStepClick($event, false)"
                     :disabled="downArrowIsDisabled">
                <div class="arrow-down"></div>
              </b-btn>
            </div>
        </div>
    </div>
  </div>
</template>

<script>
  // @flow

  export default {
    name: 'NumericInput',
    props: {
      value: {
        required: true,
      },
      title: {
        type: String,
      },
      editable: {
        type: Boolean,
        default: true,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      required: {
        type: Boolean,
        default: false,
      },
      min: {
        type: Number,
        default: Number.MIN_SAFE_INTEGER,
      },
      max: {
        type: Number,
        default: Number.MAX_SAFE_INTEGER,
      },
      step: {
        validator: (value: Object) => typeof value === 'function' || typeof value === 'number',
      },
      styles: {
        type: Object,
        default() {
          return {
            margin: 0,
            paddingTop: 0,
            paddingBottom: 0,
            paddingRight: 0,
            maxWidth: '130px',
            height: '30px',
          };
        },
      },
    },
    data() {
      return {
        upArrowIsDisabled: false,
        downArrowIsDisabled: false,
      };
    },
    computed: {
      tooltip() {
        let msg = 'Accepts a number';
        if (this.min > Number.MIN_SAFE_INTEGER && this.max < Number.MAX_SAFE_INTEGER) {
          msg += ` between ${this.min} and ${this.max} inclusive`;
        } else if (this.min > Number.MIN_SAFE_INTEGER) {
          msg += ` greater than ${this.min - 1}`;
        } else if (this.max < Number.MAX_SAFE_INTEGER) {
          msg += ` less than ${this.max + 1}`;
        }

        msg += '.';

        return msg;
      },
      currentStyle() {
        return !this.disabled
          ? this.styles
          : { ...this.styles, backgroundColor: 'lightgrey' };
      },
    },
    methods: {
      ensureMax(numInput: number) : number {
        let num = numInput;

        if (num > this.max) {
          num = this.max;
        }

        this.upArrowIsDisabled = num === this.max;

        return num;
      },
      ensureMinMax(numInput: number) : number {
        let num = numInput;

        num = this.ensureMax(numInput);

        if (num === numInput) {
          if (num < this.min) {
            num = this.min;
          }
        }

        this.downArrowIsDisabled = num === this.min;

        return num;
      },
      onStepClick(event: Object, isIncrement: boolean) {
        event.target.focus();

        const curNum = Number(this.$refs.input.value);
        let result: number;

        if (typeof this.step === 'function') {
          result = this.step(curNum, isIncrement);
        } else if (isIncrement) {
          result = curNum + 1;
        } else {
          result = curNum - 1;
        }

        result = Math.ceil(result);
        result = this.ensureMinMax(result);
        this.$refs.input.value = `${result}`;
      },
      onBlur() {
        const curNum = +this.$refs.input.value;
        const result = this.ensureMinMax(curNum);
        if (curNum === result) {
          // Emit the number value through the input event
          this.$emit('blur', Number(this.$refs.input.value));
        } else {
          this.$refs.input.value = `${result}`;
          this.$refs.input.focus();
        }
      },
      // Parses the text input and reformat as needed to only allow valid input with the
      // exception of min value since the user can enter the numbera digit at a time
      // the min value comparison cannot really be enforced until the user
      // completes the entry so that's done onBlur.
      updateDigitsValue(value: string) {
        let formattedValue = value.trim();
        let isSigned = false;

        if (formattedValue.length && formattedValue.charAt(0) === '-') {
          isSigned = true;
          formattedValue = formattedValue.slice(1);
        }
        formattedValue = value.replace(/\D/g, '');

        if (!formattedValue.length) {
          formattedValue = '0';
        }

        if (isSigned) {
          formattedValue = `-${formattedValue}`;
        }

        let num = Number(formattedValue);
        const tmp = this.ensureMax(num);
        if (num === tmp) {
          if (num < Number.MIN_SAFE_INTEGER) {
            num = this.min;
          }
        }

        formattedValue = `${num}`;

        const maxLen = Math.max(`${this.min}`.length, `${this.max}`.length);
        if (formattedValue.length > maxLen) {
          formattedValue = formattedValue.slice(0, maxLen);
        }

        // If the value was not already normalized, manually override it to conform
        if (formattedValue !== value) {
          this.$refs.input.value = formattedValue;
        }
      },
    },
    watch: {
      value() {
        this.$refs.input.value = this.value;
      },
    },
    mounted() {
      this.$refs.input.value = this.value;
    },
  };
</script>

<style scoped>
  .container {
    display:flex;
  }
  .row {
    display:flex;
    flex-direction:row;
  }
  .column {
    display:flex;
    flex-direction:column;
  }
  .shift-left {
    margin-left: -20px;
  }
  .button-class {
    height:4px;
    width: 34px;
  }
  .arrow-up {
    margin-top: -3px;
    margin-left: -4px;
    width: 0;
    height: 0;
    border-style: solid;
    border-width: 0 7px 6px 7px;
    border-color: transparent transparent #0040ff transparent;
  }
  .arrow-down {
    margin-top: -3px;
    margin-left: -4px;
    width: 0;
    height: 0;
    border-style: solid;
    border-width: 6px 7px 0 7px;
    border-color: #0040ff transparent transparent transparent;
  }
</style>
