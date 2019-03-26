<template>
  <div>
    <vue-slider @change="(v)=>sendChangeEvent(v)"
                ref="vueSlider_ref"
                v-bind="slider"
                v-model="slider.value"
                >
    </vue-slider>
    <div v-if="bottomText" :style="bottomTextStyle">{{bottomText}}</div>
  </div>
</template>

<script>
  // @flow
  import vueSlider from 'vue-slider-component';
  // eslint-disable-next-line flowtype-errors/show-errors
  import 'vue-slider-component/theme/default.css';

  import { getRangeLabel } from './types';
  import type { GraphVariableSet, GraphVariableRangeDto } from './types';

  const defaultColor = '#3498db';

  export default {
    name: 'GraphVariableSetSlider',
    components: {
      vueSlider,
    },
    props: {
      direction: {
        type: String,
      },
      graphVariableSet: {
        type: Object,
      },
      allowUpperRangeValueToChange: {
        type: Boolean,
      },
      isDisabled: {
        type: Boolean,
        default: false,
      },
    },
    data() {
      return {
        bottomText: '',
        bottomTextStyle: '',
        slider: {
          value: null,
          width: null, // default = auto : field is numeric
          height: null,
          dotSize: 14, // default = 14
          min: 0,
          max: 0,
          interval: 1,
          disabled: this.isDisabled,
          lazy: true,
          tooltip: 'always',
          enableCross: false,
          direction: this.direction,
          tooltipFormatter: null,
          marks: true,
          hideLabel: true,
          data: null,
          tooltipDir: null,
          processStyle: null,
          bgStyle: null,
          labelStyle: null,
          labelActiveStyle: {
            color: defaultColor,
          },
        },
      };
    },
    methods: {
      getIndex() : number | number[] {
        return this.$refs.vueSlider_ref.getIndex();
      },
      setIndex(index: number | number[]) {
        return this.$refs.vueSlider_ref.setIndex(index);
      },
      sendChangeEvent(value: any) {
        let newValue: any = value;

        // If it's a range, but we are only displaying a single dot, the value is always a
        // single value instead of an array, but always send back an array to simplify logic
        // for the caller.
        const params: GraphVariableSet = this.graphVariableSet;
        if (params.range && !this.allowUpperRangeValueToChange) {
          newValue = [this.slider.value, this.slider.max];
        }

        this.$emit('sliderChanged', { value: newValue, index: this.getIndex() });
      },
      setBottomTextStyle(mt: number, mb: number) {
        this.bottomTextStyle = `margin-top: ${mt}px; margin-bottom: ${mb}px`;
      },
      setOptions() {
        if (this.direction === 'ltr') {
          this.slider.height = 6;
          this.slider.width = null; // 'auto'
          this.slider.tooltipDir = [
            'top',
            'top',
          ];
        } else {
          this.slider.height = 300;
          this.slider.width = 6;
          this.slider.tooltipDir = 'top';
        }
        const params: GraphVariableSet = this.graphVariableSet;
        this.bottomText = params.name;

        const marksFunction = (value: string) => ({
          label: value,
          style: {
            backgroundColor: defaultColor,
            height: '10px',
            width: '10px',
            display: 'block',
            transform: 'translate(-2px, -2px)',
          },
        });

        if (params.enums) {
          this.slider.data = [...params.enums];
          this.setBottomTextStyle(-40, 80);
          // eslint-disable-next-line prefer-destructuring
          this.slider.value = this.slider.data[0];
          this.slider.min = 0;
          this.slider.max = this.slider.data.length;
          this.slider.interval = 1;
          this.slider.hideLabel = false;
          this.slider.marks = marksFunction;
          this.slider.railStyle = {
            backgroundColor: defaultColor,
          };
          this.slider.bgStyle = {
            backgroundColor: defaultColor,
          };
          this.slider.labelStyle = {
            color: defaultColor,
          };
          this.slider.tooltipFormatter = null;
        } else {
          // eslint-disable-next-line prefer-destructuring
          const range: ?GraphVariableRangeDto = params.range;
          if (range) {
            this.setBottomTextStyle(-44, 60);
            const formatterFunc = (text?: string) => (num: number) => getRangeLabel(num, text);
            this.slider.data = null;
            this.slider.value
                = this.allowUpperRangeValueToChange ? [range.first, range.last] : range.first;
            this.slider.min = range.first;
            this.slider.max = range.last;
            this.slider.interval = range.step;
            this.slider.hideLabel = true;
            this.slider.marks = true;
            this.slider.bgStyle = null;
            this.slider.labelStyle = null;
            this.slider.tooltipFormatter = formatterFunc(range.unitSymbol);
          }
        }
      },
    },
    watch: {
      isDisabled(newValue: boolean) {
        this.slider.disabled = newValue;
      },
      graphVariableSet() {
        this.setOptions();
      },
    },
    created() {
      this.setOptions();
    },
  };
</script>

<style scoped>
</style>
