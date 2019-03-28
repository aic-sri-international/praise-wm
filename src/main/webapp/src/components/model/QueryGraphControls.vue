<template>
  <div>
    <div v-for="(control, index) in controls" >
      <div @contextmenu.prevent="onRightMouseClick($event, index)">
        <div v-if="control.isSlider">
          <graph-variable-set-slider
              v-if="!isFixedSetOfEnums(control)"
              :key="controlsCreationToggle"
              :ref="getSliderRefName(index)"
              :isDisabled="isQueryActive"
              :style="control.style"
              @sliderChanged="(v) => onSliderChanged(index, v)"
              direction="ltr"
              :allowUpperRangeValueToChange="control.isXmVariable"
              :graphVariableSet="control.gvs"
          ></graph-variable-set-slider>
        </div>
        <div v-else :style="control.style">
          <b-input-group v-if="!isFixedSetOfEnums(control)"
                         size="sm"
                         class="ml-1"
                         :prepend="control.gvs.name">
            <b-form-select
                @input="()=>onDropdownSelectionChanged()"
                :disabled="isQueryActive"
                v-model="control.ddSelection"
                :options="control.gvs.enums">
            </b-form-select>
          </b-input-group>
        </div>
      </div>
    </div>
    <canvas v-show="false" ref="canvas_ref"></canvas>
    <context-menu id="context-menu" ref="ctxmenu_ref" @ctx-open="setCurrentRightClickData">
      <div>
        <li @click="switchXm"
            class="ctx-item">Swap {{menu.curXVarName}} with {{menu.curVarName}} as X axis
        </li>
      </div>
    </context-menu>
  </div>
</template>

<script>
  // @flow
  import debounce from 'lodash/debounce';
  import cloneDeep from 'lodash/cloneDeep';
  import ContextMenu from 'vue-context-menu';
  import { mapGetters, mapActions } from 'vuex';
  import { MODEL_VXC as MODEL } from '@/store';
  import type {
    GraphVariableSet,
    QueryResultWrapper,
    GraphQueryVariableResults,
    GraphRequestDto,
    QueryGraphControlsCurValues,
  } from '@/store/model/types';

  import GraphVariableSetSlider from './GraphVariableSetSlider';
  import { getSliderRangeLabel } from './util';

  type Control = {
    gvs: GraphVariableSet,
    sliderChanged: any,
    sliderCurIndexes?: number | number[],
    isSlider: boolean,
    isXmVariable: boolean,
    ddSelection: ?string,
    style: string,
  };

  const maxEnumsPerSlider = 4;

  export default {
    name: 'QueryGraphControls',
    components: {
      GraphVariableSetSlider,
      ContextMenu,
    },
    data() {
      return {
        suppressControlChangeNotification: false,
        controlsCreationToggle: false,
        controls: [],
        lastRightClickData: null,
        menu: {
          curXVarName: '',
          curVarName: '',
        },
      };
    },
    methods: {
      ...mapActions(MODEL.MODULE, [
        MODEL.ACTION.RUN_QUERY_FUNCTION,
        MODEL.ACTION.X_AXIS_SWAP,
      ]),
      onRightMouseClick(event: any, index: number) {
        if (!this.controls[index].isXmVariable) {
          this.menu.curXVarName = this.currentXm;
          this.menu.curVarName = this.controls[index].gvs.name;
          this.$refs.ctxmenu_ref.open(event, { index });
        }
      },
      setCurrentRightClickData(data: any) {
        this.lastRightClickData = data;
      },
      getSliderRefName(ix) {
        return `slider_${ix}_ref`;
      },
      async switchXm() {
        try {
          await this[MODEL.ACTION.X_AXIS_SWAP]({ xm: this.menu.curVarName });
        } catch (err) {
          // errors already logged/displayed
        }
      },
      buildQueryGraphControlsCurValues() : ?QueryGraphControlsCurValues {
        if (!this.controls.length) {
          return null;
        }
        const values: QueryGraphControlsCurValues = {};
        const sliderMap = {};
        const inputMap = {};

        let ix = 0;
        this.controls.forEach((c: Control) => {
          // Only include the current values if there have been any changes
          if (c.isSlider) {
            if (c.sliderCurIndexes !== undefined) {
              const refName = this.getSliderRefName(ix);
              sliderMap[refName] = c.sliderCurIndexes;
            }
          } else if (c.ddSelection !== undefined) {
            inputMap[ix] = c.ddSelection;
          }
          ix += 1;
        });

        if (Object.keys(sliderMap).length) {
          values.sliderRefNameToIndex = sliderMap;
        }
        if (Object.keys(inputMap).length) {
          values.inputFieldIndexToValue = inputMap;
        }
        return values;
      },
      isFixedSetOfEnums(control: Control) {
        return control.isXmVariable && control.gvs.enums;
      },
      getTextWidth(text: string) {
        const canvas: any = this.$refs.canvas_ref;
        const canvasContext = canvas.getContext('2d');
        canvasContext.font = '14px sans-serif';
        const metrics = canvasContext.measureText(text);
        return metrics.width;
      },
      completeControlsInit() {
        let maxTextWidthFirst = 0;
        let maxTextWidthLast = 0;

        this.controls.filter((ctrl : Control) => ctrl.isSlider).forEach((ctrl : Control) => {
          let first = '';
          let last = '';
          if (ctrl.gvs.enums) {
            // eslint-disable-next-line
            first = ctrl.gvs.enums[0];
            last = ctrl.gvs.enums[ctrl.gvs.enums.length - 1];
          } else if (ctrl.gvs.range) {
            const { range } = ctrl.gvs;
            first = getSliderRangeLabel(range.first, range.unitSymbol);
            last = getSliderRangeLabel(range.last, range.unitSymbol);
          }

          maxTextWidthFirst = Math.max(this.getTextWidth(first), maxTextWidthFirst);
          maxTextWidthLast = Math.max(this.getTextWidth(last), maxTextWidthLast);
        });

        const tooltipBorderPad = 4;

        const ml = Math.ceil(maxTextWidthFirst / 2) + tooltipBorderPad;
        const mr = Math.ceil(maxTextWidthLast / 2) + tooltipBorderPad;

        this.controls.forEach((ctrl) => {
          // eslint-disable-next-line
          ctrl.style += `margin-left: ${ml}px; margin-right: ${mr}px;`;
        });
      },
      getVariableSets(isXm: boolean, xmName: string): GraphVariableSet[] {
        const gqvr : GraphQueryVariableResults = this.graphQueryVariableResults;
        return !gqvr ? [] : gqvr.graphVariableSets
          .filter(s => (isXm ? s.name === xmName : s.name !== xmName));
      },
      async initialize() {
        if (this.queryResultsIx < 0) {
          return;
        }
        const xmVariable: string = this.currentXm;
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
            const ml = '40';

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

            let margins = `margin-top: ${mt}px; margin-bottom: ${mb}px;`;

            if (!currentIsSlider) {
              margins += `margin-left: ${ml}px;`;
            }

            return margins;
          };
          return {
            gvs,
            isSlider: currentIsSlider,
            sliderChanged: null,
            isXmVariable: this.currentXm === gvs.name,
            ddSelection: currentIsSlider ? null : getFirstEnum(),
            style: getStyle(),
          };
        };

        this.controls = sets.map(toControl);
        this.completeControlsInit();
        const lastControlValues: ?QueryGraphControlsCurValues = this.queryGraphControlsCurValues;
        if (!lastControlValues) {
          // Force all controls to be recreated if not using prior values
          this.controlsCreationToggle = !this.controlsCreationToggle;
          return;
        }

        if (lastControlValues.sliderRefNameToIndex) {
          try {
            this.suppressControlChangeNotification = true;
            await this.$nextTick();
            const that = this;
            Object.entries(lastControlValues.sliderRefNameToIndex).forEach((keyValue) => {
              const refName: string = keyValue[0];
              const index = keyValue[1];
              const slider = that.$refs[refName][0];
              slider.setIndex(index);
            });
          } finally {
            this.suppressControlChangeNotification = false;
          }
        }
      },
      onSliderChanged(controlIx: number, { value, index }) {
        const control : Control = this.controls[controlIx];
        control.sliderChanged = value;
        control.sliderCurIndexes = index;
        this.onControlChanged();
      },
      onDropdownSelectionChanged() {
        this.onControlChanged();
      },
      onControlChanged() {
        if (!this.suppressControlChangeNotification) {
          if (!this.debounced$) {
            this.debounced$ = debounce(this.queryForNewGraph, 250, { trailing: true });
          }
          this.debounced$();
        }
      },
      async queryForNewGraph() {
        const request: GraphRequestDto = this.buildGraphRequest();
        try {
          const curControlValues: ?QueryGraphControlsCurValues
              = this.buildQueryGraphControlsCurValues();
          await this[MODEL.ACTION.RUN_QUERY_FUNCTION]({ request, curControlValues });
        } catch (err) {
          // errors already logged/displayed
        }
      },
      buildGraphRequest() : GraphRequestDto {
        const xmVarName = this.currentXm;
        const request: GraphRequestDto = {
          xmVariable: xmVarName,
          graphVariableSets: [],
        };

        this.controls.forEach((c) => {
          const gvs: GraphVariableSet = cloneDeep(c.gvs);
          if (gvs.range && c.sliderChanged) {
            // eslint-disable-next-line prefer-destructuring
            gvs.range.first = c.sliderChanged[0];
            // eslint-disable-next-line prefer-destructuring
            gvs.range.last = c.sliderChanged[1];
          } else if (this.isFixedSetOfEnums(c)) {
            // Do not change the set of enums
          } else if (gvs.enums && c.sliderChanged) {
            gvs.enums = [c.sliderChanged];
          } else if (gvs.enums && !c.isSlider && c.ddSelection) {
            gvs.enums = [c.ddSelection];
          } else if (gvs.enums) {
            gvs.enums = [gvs.enums[0]];
          }
          request.graphVariableSets.push(gvs);
        });

        return request;
      },
    },
    computed: {
      ...mapGetters(MODEL.MODULE, [
        MODEL.GET.IS_QUERY_ACTIVE,
        MODEL.GET.CUR_RESULT_WRAPPER,
      ]),
      graphQueryVariableResults() : ?GraphQueryVariableResults {
        const resultsWrapper: ?QueryResultWrapper = this[MODEL.GET.CUR_RESULT_WRAPPER];
        return resultsWrapper ? resultsWrapper.expressionResult.graphQueryResultDto : null;
      },
      queryGraphControlsCurValues() : ?QueryGraphControlsCurValues {
        const resultsWrapper: ?QueryResultWrapper = this[MODEL.GET.CUR_RESULT_WRAPPER];
        return resultsWrapper ? resultsWrapper.queryGraphControlsCurValues : null;
      },
      currentXm() {
        // The first entry in the xm array is the initial xm for the graph
        // We currently do not support changing it
        const gqvr : GraphQueryVariableResults = this.graphQueryVariableResults;
        return gqvr.xmVariables[0];
      },
    },
    watch: {
      [MODEL.GET.CUR_RESULT_WRAPPER]() {
        if (this.debounced$) {
          this.debounced$.cancel();
        }
        this.initialize();
      },
    },
    mounted() {
      this.initialize();
    },
  };
</script>

<style scoped>
</style>
