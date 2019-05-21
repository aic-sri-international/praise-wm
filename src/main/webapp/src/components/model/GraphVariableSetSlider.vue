<template>
  <div>
    <vue-slider
      ref="vueSliderRef"
      v-model="slider.value"
      v-bind="slider"
      @change="(v)=>sendChangeEvent(v)"
    />
    <div
      v-if="bottomText"
      :style="bottomTextStyle"
    >
      {{ bottomText }}
    </div>
  </div>
</template>

<script lang="ts">
  // The sole reason for the following eslint-disable comment is to suppress lint errors
  // for the 'vue-slider-component/typings/typings' import and, if the comment is placed
  // as a 'disable-next-line' comment it gets removed when running IntelliJ's code reformatting.
  /* eslint-disable import/no-unresolved,import/extensions */
  import {
    Component, Prop, Vue, Watch,
  } from 'vue-property-decorator';
  import VueSlider from 'vue-slider-component';
  import {
    Direction,
    MarksFunction,
    MarksProp,
    Position,
    Styles,
    TooltipFormatter,
    TooltipFormatterFunc,
    TooltipProp,
    Value,
  } from 'vue-slider-component/typings/typings';
  import 'vue-slider-component/theme/default.css';
  import { getSliderRangeLabel } from './util';
  import { GraphVariableRangeDto, GraphVariableSet } from '@/store/model/types';
  import { GraphVariableSetSliderInterface } from '@/components/model/types';

  const defaultColor = '#3498db';

  type SliderOptions = {
    value: Value | Value[];
    direction: Direction;
    width: number | string | null; // default = 'auto'
    height: number | string | null;
    dotSize: [number, number] | number;
    min: number;
    max: number;
    interval: number;
    disabled: boolean;
    data: Value[] | null;
    lazy: boolean;
    tooltip: TooltipProp;
    tooltipPlacement: Position | Position[] | null;
    tooltipFormatter: TooltipFormatter | TooltipFormatter[] | null;
    enableCross: boolean;
    marks: MarksProp | null;
    hideLabel: boolean;
    railStyle: Styles | null,
    processStyle: Styles | null;
    labelStyle: Styles | null;
    labelActiveStyle: Styles;
  }

  @Component({
    components: {
      VueSlider,
    },
  })
  export default class GraphVariableSetSlider extends Vue
  implements GraphVariableSetSliderInterface {
    @Prop({
      type: String,
    }) readonly direction?: Direction;

    @Prop({
      type: Object,
      required: true,
    }) readonly graphVariableSet!: GraphVariableSet;

    @Prop({
      type: Boolean,
      default: false,
    }) readonly allowUpperRangeValueToChange!: boolean;

    @Prop({
      type: Boolean,
      default: false,
    }) readonly isDisabled!: boolean;

    $refs!: {
      vueSliderRef: VueSlider;
    };

    bottomText = '';

    bottomTextStyle = '';

    slider: SliderOptions = {
      value: '',
      direction: this.direction || 'ltr',
      width: null, // default = auto : field is numeric
      height: null,
      dotSize: 14, // default = 14
      min: 0,
      max: 0,
      interval: 1,
      disabled: this.isDisabled,
      data: null,
      lazy: true,
      tooltip: 'always',
      tooltipPlacement: null,
      tooltipFormatter: null,
      enableCross: false,
      marks: true,
      hideLabel: true,
      processStyle: null,
      railStyle: null,
      labelStyle: null,
      labelActiveStyle: {
        color: defaultColor,
      },
    };

    @Watch('isDisabled')
    onIsDisabled(newValue: boolean) {
      this.slider.disabled = newValue;
    }

    @Watch('graphVariableSet')
    onGraphVariableSet() {
      this.setOptions();
    }

    created() {
      this.setOptions();
    }

    getIndex(): number | number[] {
      return this.$refs.vueSliderRef.getIndex();
    }

    setIndex(index: number | number[]) {
      this.$refs.vueSliderRef.setIndex(index);
    }

    sendChangeEvent(value: any) {
      let newValue: any = value;

      // If it's a range, but we are only displaying a single dot, the value is always a
      // single value instead of an array, but always send back an array to simplify logic
      // for the caller.
      const params: GraphVariableSet = this.graphVariableSet;
      if (params.range && !this.allowUpperRangeValueToChange) {
        newValue = [this.slider.value, this.slider.max];
      }

      this.$emit('sliderChanged', {
        value: newValue,
        index: this.getIndex(),
      });
    }

    setBottomTextStyle(mt: number, mb: number) {
      this.bottomTextStyle = `margin-top: ${mt}px; margin-bottom: ${mb}px`;
    }

    setOptions() {
      if (this.slider.direction === 'ltr') {
        this.slider.height = 6;
        this.slider.width = null; // 'auto'
        this.slider.tooltipPlacement = [
          'top',
          'top',
        ];
      } else {
        this.slider.height = 300;
        this.slider.width = 6;
        this.slider.tooltipPlacement = 'top';
      }
      const params: GraphVariableSet = this.graphVariableSet;
      this.bottomText = params.name;

      const marksFunction: MarksFunction = (value: Value) => ({
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
        this.slider.labelStyle = {
          color: defaultColor,
        };
        this.slider.tooltipFormatter = null;
      } else {
        // eslint-disable-next-line prefer-destructuring
        const range: GraphVariableRangeDto | undefined = params.range;
        if (range) {
          this.setBottomTextStyle(-44, 60);
          const formatterFunc = (text: string | undefined): TooltipFormatterFunc => (
            num: Value,
          ) => getSliderRangeLabel(num, text);

          this.slider.data = null;
          this.slider.value = this.allowUpperRangeValueToChange ? [range.first, range.last]
            : range.first;
          this.slider.min = range.first;
          this.slider.max = range.last;
          this.slider.interval = range.step;
          this.slider.hideLabel = true;
          this.slider.marks = true;
          this.slider.labelStyle = null;
          this.slider.tooltipFormatter = formatterFunc(range.unitSymbol);
        }
      }
    }
  }
</script>

<style scoped>
</style>
