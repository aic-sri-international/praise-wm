<template>
  <div>
    <div v-for="(control, index) in controls" >
      <div v-if="control.isSlider">
        <graph-variable-set-slider
            :style="control.style"
            @sliderChanged="(v) => onSliderChanged(index, v)"
            class="horizontal-slider"
            direction="horizontal"
            :graphVariableSet="control.gvs"
        ></graph-variable-set-slider>
      </div>
      <div v-else :style="control.style" >
        <b-input-group size="sm" class="ml-1" :prepend="control.gvs.name">
          <b-form-select
              @input="()=>onDropdownSelectionChanged(index)"
              v-model="control.ddSelection"
              :options="control.gvs.enums">
          </b-form-select>
        </b-input-group>
      </div>
    </div>
    </div>
</template>

<script>
  // @flow
  import GraphVariableSetSlider from './GraphVariableSetSlider';
  import type { GraphVariableSet, GraphQueryVariableResults } from './types';

  type Control = {
    gvs: GraphVariableSet,
    sliderChanged: any,
    isSlider: boolean,
    ddSelection: string,
    style: string,
  };

  const maxEnumsPerSlider = 4;

  export default {
    name: 'QueryChartControls',
    components: {
      GraphVariableSetSlider,
    },
    props: {
      graphQueryVariableResults: {
        type: Object,
      },
    },
    data() {
      return {
        controls: [],
      };
    },
    methods: {
      buildGraphRequest() {
        // Build the query using the variables in the same order as initial query
        // const sets: GraphVariableSet[] = this.graphVariableSets;
      },
      getVariableSets(isXm: boolean, xmName: string): GraphVariableSet[] {
        const gqvr : GraphQueryVariableResults = this.graphQueryVariableResults;
        return gqvr.graphVariableSets
          .filter(s => (isXm ? s.name === xmName : s.name !== xmName));
      },
      initialize() {
        const gqvr : GraphQueryVariableResults = this.graphQueryVariableResults;
        // The first entry in the xm array is the initial xm for the graph
        const xmVariable: string = gqvr.xmVariables[0];
        // Show the current xm at the top
        const sets: GraphVariableSet[] = [
          ...this.getVariableSets(true, xmVariable),
          ...this.getVariableSets(false, xmVariable),
        ];

        const isSlider = (gvs: GraphVariableSet) => {
          if (gvs.range) {
            return true;
          }
          return gvs.enums !== undefined && gvs.enums.length <= maxEnumsPerSlider;
        };

        const isTextOnTop = (gvs: GraphVariableSet) => isSlider(gvs);
        const isTextOnBottom = (gvs: GraphVariableSet) => isSlider(gvs) && !gvs.range;

        const toControl = (gvs: GraphVariableSet, index: number) : Control => {
          const currentIsSlider : boolean = isSlider(gvs);
          const currentHasTextOnTop = isTextOnTop(gvs);
          const currentHasTextOnBottom = isTextOnBottom(gvs);
          const getFirstEnum = () => (gvs.enums !== undefined ? gvs.enums[0] : '');

          const getStyle = () : string => {
            const topTextHeight : number = 34;
            const bottomTextHeight : number = 16;
            const controlDividerHeight : number = 10;

            let mt : number = 0;
            let mb : number = 0;
            const ml = currentIsSlider ? '44' : '40';

            if (currentHasTextOnTop) {
              mt += topTextHeight;
            }
            if (index > 0 && isTextOnBottom(sets[index - 1])) {
              mt += bottomTextHeight;
            }
            mt += controlDividerHeight;

            // Last control
            if (index === sets.length - 1) {
              if (currentHasTextOnBottom) {
                mb += bottomTextHeight;
              }
            }
            mb += controlDividerHeight;

            return `margin-top: ${mt}px; margin-bottom: ${mb}px; margin-left: ${ml}px`;
          };
          return {
            gvs,
            isSlider: currentIsSlider,
            sliderChanged: null,
            ddSelection: getFirstEnum(),
            style: getStyle(),
          };
        };

        this.controls = sets.map(toControl);
      },
      onSliderChanged(controlIx: number, value: any) {
        const control : Control = this.controls[controlIx];
        control.sliderChanged = value;
        console.info(`onSliderChanged: ${JSON.stringify(control, null, 4)}`);
        this.$emit('controlChanged');
      },
      onDropdownSelectionChanged(controlIx: number) {
        const control : Control = this.controls[controlIx];
        console.log(`onDropdownSelectionChanged: ${JSON.stringify(control, null, 4)}`);
        this.$emit('controlChanged');
      },
    },
    watch: {
      graphQueryVariableResults() {
        this.initialize();
      },
    },
    created() {
      this.initialize();
    },
  };
</script>

<style scoped>
</style>