<template>
  <div class="top-level-container">
    <div class="left-column" id="segModelEditorViewLeftColId">
      <div>
        <b-form-textarea class="mb-2"
                         id="segmentedModelDescriptionId" max-rows="20"
                         placeholder="Enter a description for the model"
                         rows="1"
                         size="sm"
                         v-model="segmentedModel.description"
                         wrap="off">
        </b-form-textarea>
        <b-popover :show="showHelp" target="segmentedModelDescriptionId" placement="right" triggers="">
          Describes the model
        </b-popover>
      </div>
      <div class="segmentedEditor" id="segmentedEditorId">
        <div class="dcl-editor" id="dclEditorId">
          <editor :editTextWatch="dclEditTextWatch" :value="segmentedModel.declarations"
                  ref="dcl_editor_ref" type="hogm">
          </editor>
        </div>
        <b-popover :show="showHelp" target="dclEditorId" triggers="">
          <div class="help-title">Global model declarations section</div>
          You can optionally include rules in this section.
        </b-popover>
        <segmented-model-editor :rules="segmentedModel.rules"
                                ref="seg_model_editor_ref">
        </segmented-model-editor>
        <b-popover :show="showHelp && segmentedModel.rules.length > 0" target="segmentedEditorId" triggers="">
          <div class="help-title">Right-click within a rule section to display a context menu</div>
          The context menu allows you to toggle the display of metadata for the rule, insert a new rule,
          or delete the rule.
        </b-popover>
      </div>
      <div class="modelControlsContainer">
        <segmented-model-view-controls-panel
            ref="controlsPanel_ref"
            :modelNames="modelNames"
            :inputQueries="queries"
            @runQuery="runQuery"
            @inputFileChanged="inputFileChanged"
            @modelSelectionChanged="modelSelectionChanged"
            @saveCurrentModelToDisk="saveCurrentModelToDisk"
            @loadModelsFromServer="loadModelsFromServer"
            @clearQueryResults="()=> queryResults = []"
        >
        </segmented-model-view-controls-panel>
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
  import type { FileInfo } from '@/utils';
  import { HELP_VXC as HELP } from '@/store';
  import { mapGetters } from 'vuex';
  import Editor from './editor/Editor';
  import Explanations from './explanations/Explanations';
  import SegmentedModelEditor from './editor/SegmentedModelEditor';
  import SegmentedModelViewControlsPanel from './SegmentedModelViewControlsPanel';
  import QueryResults from './QueryResults';
  import QueryChartResult from './QueryChartResult';
  import QueryMapResult from './QueryMapResult';
  import MapImage from './MapImage';
  import { fetchSegmentedModels, solve, interruptSolver } from './dataSourceProxy';
  import { modelQueryDtoDefaults } from './types';
  import type {
    SegmentedModelDto,
    ModelRuleDto,
    ModelQueryDto,
    ModelQueryOptions,
    ExpressionResultDto,
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
    name: 'SegmentedModelView',
    components: {
      Editor,
      SegmentedModelEditor,
      SegmentedModelViewControlsPanel,
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
      async runQuery(queryOptions: ModelQueryOptions) {
        const sm: SegmentedModelDto = await this.getUpdatedSegmentedModel();
        const model: string = `${sm.declarations || ''}\n${sm.rules.map(mr => mr.rule).join('\n')}\n`;

        const query: ModelQueryDto = { model, ...queryOptions };

        try {
          this.runningQueries = this.runningQueries + 1;
          const result: ExpressionResultDto = await solve(query);
          this.totalMainQueryCounter += 1;
          this.queryResults = [result].concat(this.queryResults);
          this.showQueryResults = true;
          this.selectedQueryResult = 0;

          if (!this.queryResults.length) {
            this.selectedQueryResult = -1;
            this.showQueryResults = false;
          }
        } catch (err) {
          // errors already logged/displayed
        } finally {
          this.runningQueries = this.runningQueries > 0 ? this.runningQueries - 1 : 0;
        }
      },
      async saveCurrentModelToDisk() {
        const sm: SegmentedModelDto = await this.getUpdatedSegmentedModel();
        this.$$.downloadFile(sm, `${sm.name}.json`);
      },
      modelSelectionChanged(modelIx: number) {
        const smd: SegmentedModelDto = this.segmentedModels[modelIx];
        this.segmentedModel = cloneDeep(smd);
        this.dclEditTextWatch = !this.dclEditTextWatch;
        this.queries = uniqBy(smd.queries, identity);
        this.queryResults = [];
      },
      setupModels(models: SegmentedModelDto[]) {
        this.segmentedModels = models;
        this.modelNames = models.map(e => e.name);
      },
      async loadModels(): Promise<SegmentedModelDto[]> {
        try {
          return await fetchSegmentedModels();
        } catch (err) {
          // errors already logged/displayed
          return [];
        }
      },
      async inputFileChanged(filesInfo: FileInfo[]) {
        if (filesInfo.length === 0) {
          return;
        }
        const { text } = filesInfo[0];
        const model: SegmentedModelDto = this.minimizeModel(JSON.parse(text));
        this.setupModels([model]);
      },
      async loadModelsFromServer() {
        const models: SegmentedModelDto[] = await this.loadModels();
        if (models.length) {
          this.setupModels(models);
        }
      },
      interruptQueries() {
        interruptSolver();
      },
    },
    async created() {
      this.loadModelsFromServer();
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
      this.splitter$ = Split(['#segModelEditorViewLeftColId', '#segModelEditorViewRightColId'], {
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

  .segmentedEditor {
    flex: 1 1 auto;
    position: relative;
    overflow-y: auto;
  }

  .modelControlsContainer {
    border: thin double lightgrey;
    padding: 10px;
  }

  .dcl-editor {
    border: thin double lightgrey;
    padding: 10px;
  }

  .query-results {
    border: thin double lightgrey;
    padding: 4px;
    max-height: 140px;
    overflow: auto
  }

  .help-title {
    font-weight: 500;
    border-bottom: 1px solid lightgrey;
  }
</style>
