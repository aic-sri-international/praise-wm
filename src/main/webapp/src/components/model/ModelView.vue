<template>
  <div class="top-level-container">
    <div class="left-column" id="modelEditorViewLeftColId">
      <model-editor-view class="model-editor-view"></model-editor-view>
      <div class="modelControlsContainer">
        <model-controls-panel
            ref="controlsPanel_ref"
        >
        </model-controls-panel>
        <div id="queryResultsId">
          <query-results :results="queryResults"
                         :selectedIx="selectedQueryResult"
                         @selectionChanged="(ix)=> { selectedQueryResult = ix; totalMainQueryCounter += 1;}"
                         class="query-results mt-2 mb-2"
                         v-if="showQueryResults && queryResults.length">
          </query-results>
        </div>
        <b-popover :show="showHelp" target="queryResultsId" placement="right" triggers="">
          <div class="help-title">Query results messages</div>
          After running a second query you can click on a prior result message to
          see its result in the visualization panel.
        </b-popover>
      </div>
      <spinner :show="runningQueries > 0"
               @click="interruptQueries()"></spinner>
    </div>
    <div class="right-column" id="segModelEditorViewRightColId">
      <!--@TODO update when the HOGM solver can return map related query results-->
      <!-- We use the :key="totalMainQueryCounter" to assure that the complete state of
           the component is reset between each initial query -->
      <query-map-result :graph-query-result="graphQueryResult"
                        :key="totalMainQueryCounter"
                        @querySent="runningQueries += 1"
                        @queryReturned="runningQueries -= 1"
                        v-if="displayMap"></query-map-result>
      <div v-else class="query-chart">
        <query-chart-result :graph-query-result="graphQueryResult"
                            :key="totalMainQueryCounter"
                            @querySent="runningQueries += 1"
                            @queryReturned="runningQueries -= 1">
        </query-chart-result>
      </div>
      <!-- @TODO replacement for the following is TBD soon, leave it commented out for now -->
      <!--<explanations :explanation-tree="explanationTree" id="explanations"></explanations>-->
    </div>
    <b-popover :show="showHelp" target="segModelEditorViewRightColId" triggers="">
      Visualization of query results.
    </b-popover>
  </div>
</template>

<script>
  // @flow
  import Split from 'split.js';
  import cloneDeep from 'lodash/cloneDeep';
  import uniqBy from 'lodash/uniqBy';
  import identity from 'lodash/identity';
  import Spinner from '@/components/Spinner';
  import { HELP_VXC as HELP, MODEL_VXC as MODEL } from '@/store';
  import { mapGetters, mapActions } from 'vuex';
  import ModelEditorView from './editor/ModelEditorView';
  import Editor from './editor/Editor';
  import Explanations from './explanations/Explanations';
  import SegmentedModelEditor from './editor/ModelEditor';
  import ModelControlsPanel from './ModelControlsPanel';
  import QueryResults from './QueryResults';
  import QueryChartResult from './QueryChartResult';
  import QueryMapResult from './QueryMapResult';
  import MapImage from './MapImage';
  import { interruptSolver } from './dataSourceProxy';
  import { modelQueryDtoDefaults } from './types';
  import type {
    SegmentedModelDto,
    ModelRuleDto,
    GraphQueryResultDto,
  } from './types';

  const emptySegmentedModel: SegmentedModelDto = {
    name: '',
    description: '',
    declarations: '',
    rules: [{
      metadata: '',
      rule: '',
    }],
    queries: [],
  };

  export default {
    name: 'ModelView',
    components: {
      ModelEditorView,
      Editor,
      SegmentedModelEditor,
      ModelControlsPanel,
      QueryResults,
      QueryChartResult,
      MapImage,
      Explanations,
      QueryMapResult,
      Spinner,
    },
    data() {
      return {
        segmentedModels: [],
        segmentedModel: emptySegmentedModel,
        modelNames: [],
        queries: [],
        queryResults: [],
        totalMainQueryCounter: 0,
        showQueryResults: true,
        dclEditTextWatch: false,
        selectedQueryResult: -1,
        runningQueries: 0,
        numberOfInitialSamples: modelQueryDtoDefaults.numberOfInitialSamples,
        numberOfDiscreteValues: modelQueryDtoDefaults.numberOfDiscreteValues,
      };
    },
    computed: {
      displayMap() {
        const gqr : GraphQueryResultDto = this.graphQueryResult;
        return (gqr && gqr.mapRegionNameToValue) || !(gqr && gqr.imageData);
      },
      explanationTree() {
        if (this.selectedQueryResult > -1 && this.queryResults.length > 0) {
          return this.queryResults[this.selectedQueryResult].explanationTree;
        }
        return null;
      },
      graphQueryResult() : ?GraphQueryResultDto {
        if (this.selectedQueryResult > -1 && this.queryResults.length > 0) {
          return this.queryResults[this.selectedQueryResult].graphQueryResultDto;
        }
        return null;
      },
      ...mapGetters(HELP.MODULE, [
        HELP.GET.SHOW_HELP,
      ]),
    },
    methods: {
      ...mapActions(MODEL.MODULE, [
        MODEL.ACTION.INITIALIZE,
      ]),
      async getUpdatedSegmentedModel() {
        const declarations = this.$refs.dcl_editor_ref.getValue().trim();
        const rules: ModelRuleDto[] = await this.$refs.seg_model_editor_ref.getModelRules();
        return {
          name: this.$refs.controlsPanel_ref.getSelectedModelName(),
          description: this.segmentedModel.description.trim(),
          declarations,
          rules,
          queries: [...this.$refs.controlsPanel_ref.getSelectedModelQueries()],
        };
      },
      minimizeModel(model: SegmentedModelDto) : SegmentedModelDto {
        const coalesceRules = (rules: ModelRuleDto[]) : string =>
          rules.reduce((accum: string, modeRuleDto: ModelRuleDto) => {
            const lf = accum ? '\n\n' : '';
            // eslint-disable-next-line no-param-reassign
            accum += `${lf}${modeRuleDto.rule}`;
            return accum;
          }, '');

        const rulesString: string = coalesceRules(model.rules);
        let declarations: string = model.declarations || '';
        if (declarations && rulesString) {
          declarations += `\n\n${rulesString}`;
        } else if (rulesString) {
          declarations = rulesString;
        }

        //  Strip metadata and coalesce declarations and rules
        const slimModel: SegmentedModelDto = {
          name: model.name,
          description: model.description,
          declarations,
          rules: [],
          queries: model.queries,
        };

        return slimModel;
      },
      // async runQuery(queryOptions: ModelQueryOptions) {
      //   const sm: SegmentedModelDto = await this.getUpdatedSegmentedModel();
      //   const model:
      //   string = `${sm.declarations || ''}\n${sm.rules.map(mr => mr.rule).join('\n')}\n`;
      //
      //   const query: ModelQueryDto = { model, ...queryOptions };
      //
      //   try {
      //     this.runningQueries = this.runningQueries + 1;
      //     const result: ExpressionResultDto = await solve(query);
      //     this.totalMainQueryCounter += 1;
      //     this.queryResults = [result].concat(this.queryResults);
      //     this.showQueryResults = true;
      //     this.selectedQueryResult = 0;
      //
      //     if (!this.queryResults.length) {
      //       this.selectedQueryResult = -1;
      //       this.showQueryResults = false;
      //     }
      //   } catch (err) {
      //     // errors already logged/displayed
      //   } finally {
      //     this.runningQueries = this.runningQueries > 0 ? this.runningQueries - 1 : 0;
      //   }
      // },
      modelSelectionChanged(modelIx: number) {
        const smd: SegmentedModelDto = this.segmentedModels[modelIx];
        this.segmentedModel = cloneDeep(smd);
        this.dclEditTextWatch = !this.dclEditTextWatch;
        this.queries = uniqBy(smd.queries, identity);
        this.queryResults = [];
      },
      interruptQueries() {
        interruptSolver();
      },
    },
    async created() {
      this.initialize();
    },
    mounted() {
      const sendResizeEvent = () => {
        // The Map component needs to get a window's resize event so that it can recompute
        // map coordinates and UI display.
        window.dispatchEvent(new Event('resize'));
      };
      if (this.splitter$ !== undefined) {
        this.splitter$.destroy();
      }
      this.splitter$ = Split(['#modelEditorViewLeftColId', '#segModelEditorViewRightColId'], {
        sizes: [60, 40],
        onDrag() {
          sendResizeEvent();
        },
      });
      sendResizeEvent();
    },
    beforeDestroy() {
      this.splitter$.destroy();
    },
  };
</script>

<style>
  .gutter {
    background-color: #eee;

    background-repeat: no-repeat;
    background-position: 50%;
  }

  .gutter.gutter-vertical {
    background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAFAQMAAABo7865AAAABlBMVEVHcEzMzMzyAv2sAAAAAXRSTlMAQObYZgAAABBJREFUeF5jOAMEEAIEEFwAn3kMwcB6I2AAAAAASUVORK5CYII=')
  }

  .gutter.gutter-horizontal {
    background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAeCAYAAADkftS9AAAAIklEQVQoU2M4c+bMfxAGAgYYmwGrIIiDjrELjpo5aiZeMwF+yNnOs5KSvgAAAABJRU5ErkJggg==')
  }
</style>
<style scoped>
  .top-level-container {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: row;
  }

  .left-column {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .right-column {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .query-chart {
    display: flex;
    justify-content: center;
  }

  .model-editor-view {
    flex: 1 1 auto;
    flex-direction: column;
    position: relative;
    overflow-y: auto;
  }

  .modelControlsContainer {
    display: flex;
    flex-direction: column;
    border: thin double lightgrey;
    padding: 10px;
  }

  .query-results {
    border: thin double lightgrey;
    padding: 4px;
    max-height: 140px;
    overflow: auto
  }
</style>
