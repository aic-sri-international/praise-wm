<template>
  <div ref="temp_ref" v-observe-visibility="{
                  callback: visibilityChanged,
                }">
    <vue-slider @callback="(v)=>$emit('sliderChanged', v)"
                ref="vueSlider_ref"
                v-bind="slider"
                v-model="slider.value"
                >
    </vue-slider>
    <div v-if="bottomText" style="margin-top: -34px">{{bottomText}}</div>
    <div v-if="bottomText" style="height: 4px"></div>
  </div>
</template>

<script>
  // @flow
  import vueSlider from 'vue-slider-component';
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
    },
    data() {
      return {
        bottomText: '',
        slider: {
          value: null,
          width: 'auto',
          height: null,
          dotSize: 14,
          min: 0,
          max: 0,
          interval: 1,
          disabled: false,
          lazy: true,
          eventType: 'auto',
          show: false,
          tooltip: 'always',
          enableCross: false,
          direction: this.direction,
          formatter: null,
          piecewiseLabel: true,
          data: null,
          tooltipDir: null,
          piecewise: true,
          processStyle: null,
          piecewiseStyle: null,
          piecewiseActiveStyle: {
            backgroundColor: defaultColor,
          },
          bgStyle: null,
          labelStyle: null,
          labelActiveStyle: {
            color: defaultColor,
          },
        },
      };
    },
    methods: {
      visibilityChanged(isVisible: boolean) {
        // This is required to have the slider dots and labels display correctly on initial
        // display without adding restrictions or complexity to parent components.
        // See 'Exceptions' section of docs: https://github.com/NightCatSama/vue-slider-component
        if (isVisible && !this.slider.show) {
          this.slider.show = true;
        }
      },
      setOptions() {
        if (this.direction === 'horizontal') {
          this.slider.height = 6;
          this.slider.width = 'auto';
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
        if (params.enums) {
          this.slider.data = [...params.enums];
          // eslint-disable-next-line prefer-destructuring
          this.slider.value = this.slider.data[0];
          this.slider.min = 0;
          this.slider.max = this.slider.data.length;
          this.slider.interval = 1;
          this.slider.piecewiseLabel = true;
          this.slider.piecewiseStyle = {
            backgroundColor: defaultColor,
            visibility: 'visible',
            width: '12px',
            height: '12px',
          };
          this.slider.bgStyle = {
            backgroundColor: defaultColor,
          };
          this.slider.labelStyle = {
            color: defaultColor,
          };
          this.slider.formatter = null;
        } else {
          // eslint-disable-next-line prefer-destructuring
          const range: ?GraphVariableRangeDto = params.range;
          if (range) {
            const formatterFunc = (text?: string) => (num: number) => getRangeLabel(num, text);

            this.bottomText = params.name;
            this.slider.data = null;
            this.slider.value = [range.first, range.last];
            this.slider.disabled = [false, !this.allowUpperRangeValueToChange];
            this.slider.min = range.first;
            this.slider.max = range.last;
            this.slider.interval = range.step;
            this.slider.piecewiseLabel = false;
            this.slider.piecewiseStyle = null;
            this.slider.bgStyle = null;
            this.slider.labelStyle = null;
            this.slider.formatter = formatterFunc(range.unitSymbol);
          }
        }
      },
    },
    watch: {
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