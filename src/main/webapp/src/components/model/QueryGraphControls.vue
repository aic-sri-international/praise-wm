<template>
  <!-- eslint-disable vue/require-v-for-key -->
  <div>
    <div v-for="(control, index) in controls">
      <div @contextmenu.prevent="onRightMouseClick($event, index)">
        <div v-if="control.isSlider">
          <graph-variable-set-slider
            v-if="!control.isXmVariable"
            :key="controlsCreationToggle"
            :ref="getSliderRefName(index)"
            :allow-upper-range-value-to-change="control.isXmVariable"
            :graph-variable-set="control.gvs"
            :is-disabled="isQueryActive"
            :style="control.style"
            @sliderChanged="(v) => onSliderChanged(index, v)"
          />
        </div>
        <div
          v-else
          :style="control.style"
        >
          <b-input-group
            v-if="!control.isXmVariable"
            :prepend="control.gvs.name"
            class="ml-1"
            size="sm"
          >
            <b-form-select
              v-model="control.ddSelection"
              :disabled="isQueryActive"
              :options="control.gvs.enums"
              @input="()=>onDropdownSelectionChanged()"
            />
          </b-input-group>
        </div>
      </div>
    </div>
    <canvas
      v-show="false"
      ref="canvasRef"
    />
    <context-menu
      id="context-menu"
      ref="ctxmenuRef"
      @ctx-open="setCurrentRightClickData"
    >
      <div>
        <li
          class="ctx-item"
          @click="switchXm"
        >
          Swap {{ menu.curXVarName }} with {{ menu.curVarName }} as X axis
        </li>
      </div>
    </context-menu>
  </div>
</template>

<script lang="ts">
  import { Component, Vue, Watch } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';

  import debounce from 'lodash/debounce';
  import cloneDeep from 'lodash/cloneDeep';
  import ContextMenu from 'vue-context-menu';
  import {
    GraphQueryVariableResults,
    GraphRequestDto,
    GraphVariableSet,
    QueryGraphControlsCurValues,
    QueryResultWrapper,
    RunQueryFunctionPayload,
  } from '@/store/model/types';

  import GraphVariableSetSlider from './GraphVariableSetSlider.vue';

  import { getSliderRangeLabel } from './util';
  import { GraphVariableSetSliderInterface } from '@/components/model/types';
  import { MODEL_MODULE_NAME } from '@/store/model/constants';


  type Control = {
    gvs: GraphVariableSet,
    sliderChanged: any,
    sliderCurIndexes?: number | number[],
    isSlider: boolean,
    isXmVariable: boolean,
    ddSelection: string | null,
    style: string,
  };

  const maxEnumsPerSlider = 4;

  const modelModule = namespace(MODEL_MODULE_NAME);

  type ContextMenuType = Vue & {
    open: (event: MouseEvent, data: { index: number }) => any,
  }

  @Component({
    components: {
      GraphVariableSetSlider,
      ContextMenu,
    },
  })
  export default class QueryGraphControls extends Vue {
    $refs!: {
      ctxmenuRef: ContextMenuType,
      canvasRef: HTMLCanvasElement,
    };

    suppressControlChangeNotification = false;

    controlsCreationToggle = false;

    controls: Control[] = [];

    lastRightClickData = null;

    menu = {
      curXVarName: '',
      curVarName: '',
    };

    debounced$: any = undefined;

    @modelModule.Action runQueryFunction!: (payload: RunQueryFunctionPayload) => void;

    @modelModule.Action xAxisSwap!: (xAxisVariableName: string) => void;

    @modelModule.Getter isQueryActive!: boolean;

    @modelModule.Getter curResultWrapper?: QueryResultWrapper;

    get graphQueryVariableResults(): GraphQueryVariableResults | undefined {
      const resultsWrapper: QueryResultWrapper | undefined = this.curResultWrapper;
      return resultsWrapper ? resultsWrapper.expressionResult.graphQueryResultDto : undefined;
    }

    get queryGraphControlsCurValues(): QueryGraphControlsCurValues | undefined {
      const resultsWrapper: QueryResultWrapper | undefined = this.curResultWrapper;
      return resultsWrapper ? resultsWrapper.queryGraphControlsCurValues : undefined;
    }

    get currentXm() {
      // The first entry in the xm array is the initial xm for the graph
      // We currently do not support changing it
      const gqvr: GraphQueryVariableResults | undefined = this.graphQueryVariableResults;
      return gqvr ? gqvr.xmVariables[0] : '';
    }

    @Watch('curResultWrapper')
    async onCurResultWrapper() {
      if (this.debounced$) {
        this.debounced$.cancel();
      }
      await this.initialize();
    }

    async mounted() {
      await this.initialize();
    }

    onRightMouseClick(event: MouseEvent, index: number) {
      if (!this.controls[index].isXmVariable) {
        this.menu.curXVarName = this.currentXm;
        this.menu.curVarName = this.controls[index].gvs.name;
        this.$refs.ctxmenuRef.open(event, { index });
      }
    }

    setCurrentRightClickData(data: any) {
      this.lastRightClickData = data;
    }

    getSliderRefName(ix: number) {
      return `slider_${ix}_ref`;
    }

    async switchXm() {
      try {
        await this.xAxisSwap(this.menu.curVarName);
      } catch (err) {
        // errors already logged/displayed
      }
    }

    buildQueryGraphControlsCurValues(): QueryGraphControlsCurValues | undefined {
      if (!this.controls.length) {
        return undefined;
      }
      const values: QueryGraphControlsCurValues = {};
      const sliderMap: { [key: string]: number | number[] } = {};
      const inputMap: { [key: number]: string } = {};

      let ix = 0;
      this.controls.forEach((c: Control) => {
        // Only include the current values if there have been any changes
        if (c.isSlider) {
          if (c.sliderCurIndexes !== undefined) {
            const refName = this.getSliderRefName(ix);
            sliderMap[refName] = c.sliderCurIndexes;
          }
        } else if (c.ddSelection) {
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
    }

    isFixedSetOfEnums(control: Control) {
      return control.isXmVariable && control.gvs.enums;
    }

    getTextWidth(text: string) {
      const canvas = this.$refs.canvasRef;
      const canvasContext = canvas.getContext('2d');
      if (canvasContext) {
        canvasContext.font = '14px sans-serif';
        const metrics = canvasContext.measureText(text);
        return metrics.width;
      }
      throw Error('canvas.getContext("2d") returned null');
    }

    completeControlsInit() {
      let maxTextWidthFirst = 0;
      let maxTextWidthLast = 0;

      this.controls.filter((ctrl: Control) => ctrl.isSlider)
      .forEach((ctrl: Control) => {
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
        ctrl.style += `margin-left: ${ml}px; margin-right: ${mr}px;`;
      });
    }

    getVariableSets(isXm: boolean, xmName: string): GraphVariableSet[] {
      const gqvr: GraphQueryVariableResults | undefined = this.graphQueryVariableResults;
      return !gqvr ? [] : gqvr.graphVariableSets
      .filter(s => (isXm ? s.name === xmName : s.name !== xmName));
    }

    async initialize() {
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

      const toControl = (gvs: GraphVariableSet, index: number): Control => {
        const currentIsSlider: boolean = isSlider(gvs);
        const currentHasTextOnTop = isTextOnTop(gvs);
        const currentHasTextOnBottom = isTextOnBottom(gvs);
        const getFirstEnum = () => (gvs.enums !== undefined ? gvs.enums[0] : '');

        const getStyle = (): string => {
          const topTextHeight: number = 34;
          const bottomTextHeight: number = 16;
          const controlDividerHeight: number = 10;

          let mt: number = 0;
          let mb: number = 0;
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
      const lastControlValues:
        QueryGraphControlsCurValues | undefined = this.queryGraphControlsCurValues;
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
          Object.entries(lastControlValues.sliderRefNameToIndex)
          .forEach((keyValue) => {
            const refName: string = keyValue[0];
            const index = keyValue[1];
            const gvsRefs = (that.$refs as any)[refName];
            if (gvsRefs && gvsRefs[0]) {
              const slider = (gvsRefs[0] as GraphVariableSetSliderInterface);
              slider.setIndex(index);
            }
          });
        } finally {
          this.suppressControlChangeNotification = false;
        }
      }
    }

    onSliderChanged(controlIx: number, { value, index }: { value: any, index: number | number[] }) {
      const control: Control = this.controls[controlIx];
      control.sliderChanged = value;
      control.sliderCurIndexes = index;
      this.onControlChanged();
    }

    onDropdownSelectionChanged() {
      this.onControlChanged();
    }

    onControlChanged() {
      if (!this.suppressControlChangeNotification) {
        if (!this.debounced$) {
          this.debounced$ = debounce(this.queryForNewGraph, 250, { trailing: true });
        }
        this.debounced$();
      }
    }

    async queryForNewGraph() {
      const request: GraphRequestDto = this.buildGraphRequest();
      try {
        const curControlValues = this.buildQueryGraphControlsCurValues();

        await this.runQueryFunction({
          request,
          curControlValues,
        });
      } catch (err) {
        // errors already logged/displayed
      }
    }

    buildGraphRequest(): GraphRequestDto {
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
    }
  }
</script>
