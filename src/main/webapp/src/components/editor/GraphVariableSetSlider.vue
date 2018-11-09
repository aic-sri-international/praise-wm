<template>
  <div>
    <vue-slider ref="slider" v-bind="slider" v-model="slider.value"></vue-slider>
  </div>
</template>

<script>
  // @flow
  import vueSlider from 'vue-slider-component';
  import type { GraphVariableSet, GraphVariableRangeDto } from './types';

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
          data: null,
          tooltipDir: null,
          piecewise: true,
          processStyle: null,
          // piecewiseStyle: {
          //   backgroundColor: '#ccc',
          //   visibility: 'visible',
          //   width: '12px',
          //   height: '12px',
          // },
          piecewiseActiveStyle: {
            backgroundColor: '#3498db',
          },
          labelActiveStyle: {
            color: '#3498db',
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
          this.slider.data = null;
        }
        const params : GraphVariableSet = this.graphVariableSet;
        if (params.enums) {
          this.slider.data = [...params.enums];
          this.slider.min = 0;
          this.slider.max = this.slider.data.length;
          this.slider.interval = 1;
        } else {
          // eslint-disable-next-line prefer-destructuring
          const range : ?GraphVariableRangeDto = params.range;
          if (range) {
            this.slider.min = range.first;
            this.slider.max = range.last;
            this.slider.interval = range.step;
            this.slider.value = [range.first, range.last];
            this.slider.data = null;
          }
        }
      },
    },
    created() {
      this.setOptions();
    },
  };
</script>

<style scoped>
</style>