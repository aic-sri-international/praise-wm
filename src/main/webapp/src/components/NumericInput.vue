<template>
  <div class="container">
    <div class="row">
      <input
        ref="input"
        v-tippy
        :class="{ disabledBackground: disabled }"
        :disabled="disabled"
        :style="styles"
        :title="tooltip"
        style="text-align: left"
        @blur="onBlur"
        @input="updateDigitsValue($event.target.value)"
      >
      <div class="column shift-left">
        <div class="row">
          <b-btn
            :disabled="disabledUpArrow"
            class="button-class"
            variant="outline-secondary"
            @blur="onBlur"
            @click.stop="onStepClick($event, true)"
          >
            <div
              :class="{ 'arrow-up-disabled': disabledUpArrow}"
              class="arrow-up"
            />
          </b-btn>
        </div>
        <span style="height: .5px" />
        <div class="row">
          <b-btn
            ref="downButton_ref"
            :disabled="disabledDownArrow"
            class="button-class"
            variant="outline-secondary"
            @blur="onBlur"
            @click.stop="onStepClick($event, false)"
          >
            <div
              :class="{ 'arrow-down-disabled': disabledDownArrow}"
              class="arrow-down"
            />
          </b-btn>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import {
    Component, Prop, Vue, Watch,
  } from 'vue-property-decorator';
  import isFunction from 'lodash/isFunction';
  import isNumber from 'lodash/isNumber';

  export type Step = number | ((curNum: number, isIncrement: boolean) => number);

  @Component
  export default class NumericInput extends Vue {
    @Prop({
      type: Number,
      required: true,
    }) readonly value!: number;

    @Prop(String) readonly title?: string;

    @Prop({ default: true }) readonly editable!: boolean;

    @Prop({ default: false }) readonly disabled!: boolean;

    @Prop({ default: false }) readonly required!: boolean;

    @Prop({ default: Number.MIN_SAFE_INTEGER }) readonly min!: number;

    @Prop({ default: Number.MAX_SAFE_INTEGER }) readonly max!: number;

    @Prop({
      validator: (value: Function | number) => isFunction(value) || isNumber(value),
    })
    readonly step?: Step;

    @Prop({
      default: () => ({
        margin: 0,
        paddingTop: 0,
        paddingBottom: 0,
        paddingRight: 0,
        maxWidth: '130px',
        height: '30px',
        fontSize: '0.9rem',
      }),
    }) readonly styles!: Object;

    $refs!: {
      input: HTMLInputElement
    };

    upArrowIsDisabled = false;

    downArrowIsDisabled = false;

    get tooltip() {
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
    }

    get disabledUpArrow() {
      return this.disabled || this.upArrowIsDisabled;
    }

    get disabledDownArrow() {
      return this.disabled || this.downArrowIsDisabled;
    }

    @Watch('value')
    onValueChanged() {
      this.$refs.input.value = `${this.value}`;
    }

    mounted() {
      this.$refs.input.value = `${this.value}`;
    }

    ensureMax(numInput: number): number {
      let num = numInput;

      if (num > this.max) {
        num = this.max;
      }

      this.upArrowIsDisabled = num === this.max;

      return num;
    }

    ensureMinMax(numInput: number): number {
      let num = this.ensureMax(numInput);

      if (num === numInput) {
        if (num < this.min) {
          num = this.min;
        }
      }

      this.downArrowIsDisabled = num === this.min;

      return num;
    }

    onStepClick(event: Event, isIncrement: boolean) {
      if (event.target) {
        (event.target as HTMLInputElement).focus();
      }

      const curNum = Number(this.$refs.input.value);
      let result!: number;
      let bump: number | null = null;

      if (isFunction(this.step)) {
        result = this.step(curNum, isIncrement);
      } else if (isNumber(this.step)) {
        bump = this.step;
      } else {
        bump = 1;
      }

      if (bump !== null) {
        if (isIncrement) {
          result = curNum + bump;
        } else {
          result = curNum - bump;
        }
      }

      result = Math.ceil(result);
      result = this.ensureMinMax(result);
      this.$refs.input.value = `${result}`;
    }

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
    }

    // Parses the text input and reformat as needed to only allow valid input with the
    // exception of min value since the user can enter the numbera digit at a time
    // the min value comparison cannot really be enforced until the user
    // completes the entry so that's done onBlur.
    updateDigitsValue(value: string) {
      let formattedValue = value.trim();
      let isSigned = false;

      if (formattedValue.length && formattedValue.charAt(0) === '-') {
        isSigned = true;
      }

      formattedValue = value.replace(/\D/g, '');

      if (!formattedValue.length) {
        formattedValue = '0';
      } else if (isSigned) {
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
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    display: flex;
  }

  .row {
    display: flex;
    flex-direction: row;
  }

  .column {
    display: flex;
    flex-direction: column;
  }

  .shift-left {
    margin-left: -20px;
  }

  .button-class {
    height: 4px;
    width: 34px;
  }

  [class*="arrow-"] {
    margin-top: -3px;
    margin-left: -4px;
    width: 0;
    height: 0;
    border-style: solid;
  }

  .arrow-up {
    border-width: 0 7px 6px 7px;
    border-top-color: transparent;
    border-right-color: transparent;
    border-bottom-color: #0040ff;
    border-left-color: transparent;
  }

  .arrow-down {
    border-width: 6px 7px 0 7px;
    border-top-color: #0040ff;
    border-right-color: transparent;
    border-bottom-color: transparent;
    border-left-color: transparent;
  }

  .arrow-up-disabled {
    border-bottom-color: grey;
  }

  .arrow-down-disabled {
    border-top-color: grey;
  }
</style>
