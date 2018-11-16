<template>
  <div>
    <vue-slider @callback="(v)=>$emit('sliderChanged', v)"
                v-bind="slider"
                v-if="showSlider"
                v-model="slider.value">
    </vue-slider>
  </div>
</template>

<script>
  // @flow
  import vueSlider from 'vue-slider-component';
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
    },
    data() {
      return {
        showSlider: false,
        slider: {
          value: null,
          width: 'auto',
          height: null,
          dotSize: 14,
          min: 0,
          max: 0,
          interval: 1,
          disabled: false,
          eventType: 'auto',
          show: true,
          tooltip: 'always',
          enableCross: false,
          direction: this.direction,
          formatter: '{value}',
          piecewiseLabel: false,
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
            this.slider.data = null;
            this.slider.value = [range.first, range.last];
            this.slider.min = range.first;
            this.slider.max = range.last;
            this.slider.interval = range.step;
            this.slider.piecewiseLabel = false;
            this.slider.piecewiseStyle = null;
            this.slider.bgStyle = null;
            this.slider.labelStyle = null;
            if (range.unitSymbol) {
              this.slider.formatter = `{value}${range.unitSymbol}`;
            }
          }
        }
      },
    },
    watch: {
      graphVariableSet() {
        this.setOptions();
      },
    },
    mounted() {
      // We seem to need this to get the slider to set its bounds correctly
      // however, it can still be an issue if the entire slider is not
      // able to be visible in its contained parent.
      setTimeout(() => {
        this.$nextTick(() => {
          this.showSlider = true;
          this.$nextTick(() => window.dispatchEvent(new Event('resize')));
        }, 100);
      });
    },
    created() {
      this.setOptions();
    },
  };
</script>

<style scoped>
</style>