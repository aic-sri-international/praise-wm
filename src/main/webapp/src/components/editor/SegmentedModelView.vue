<template>
  <div class="top-level-container">
    <div class="left-column" id="segModelEditorViewLeftCol">
      <div>
        <b-form-textarea class="mb-2"
                         id="segmentedModelDescription" max-rows="20"
                         placeholder="Enter a description for the model"
                         rows="1"
                         size="sm"
                         v-model="segmentedModel.description"
                         wrap="off">
        </b-form-textarea>
        <b-popover :show="showHelp" target="segmentedModelDescription" triggers="">
          <span class="help">Describes the model.</span>
        </b-popover>
      </div>
      <div class="segmentedEditor" id="segmentedEditor">
        <div class="dcl-editor">
          <editor :editTextWatch="dclEditTextWatch" :value="segmentedModel.declarations" id="dclEditor"
                  ref="dcl_editor_ref" type="hogm">
          </editor>
        </div>
        <b-popover :show="showHelp" target="dclEditor" triggers="">
          <span class="help">Global model declarations section.</span>
        </b-popover>
        <segmented-model-editor :rules="segmentedModel.rules" id="segmented-model-editor"
                                ref="seg_model_editor_ref">
        </segmented-model-editor>
        <b-popover :show="showHelp" target="segmentedEditor" triggers="">
          <span class="help">
            Right-click within a rule section to display a context menu. The context menu
            allows you to toggle the display of metadata for the rule, insert a new rule,
            or delete the rule.
          </span>
        </b-popover>
      </div>
      <div class="modelControlsContainer">
        <div class="modelControlsPanel">
          <div style="display: flex; flex-direction: row">
            <b-input-group class="ml-1" prepend="Model" size="sm">
              <b-form-select
                  :options="modelOptions"
                  @input="modelSelectionChanged"
                  v-model="modelOptionSelected">
              </b-form-select>
            </b-input-group>
            <span class="ml-1"></span>
            <action-button
                @clicked="()=>$refs.input_ref.click()"
                title="Open Model"
                type="open">
            </action-button>
            <action-button
                @clicked="download"
                title="Download Model"
                type="download">
            </action-button>
            <action-button
                @clicked="loadModelsFromServer"
                title="Reload models from server"
                type="sync">
            </action-button>
          </div>
          <div style="display: flex; flex-direction: row">
            <editable-datalist
                :options="queryOptions"
                class="ml-1"
                label="Query"
                placeholder="Please enter a query ..."
                ref="queryOption_ref">
            </editable-datalist>
            <span class="ml-1"></span>
            <action-button
                @clicked="runQuery"
                title="Run the query"
                type="play">
            </action-button>
            <action-button
                @clicked="()=> queryResults = []"
                title="Remove query results"
                type="broom">
            </action-button>
          </div>
          <div class="query-solver-options">
            <span class="query-solver-options-text">Initial samples</span>
            <numeric-input
                :min="1"
                :max="100000000"
                :step="(curNum, isIncrement) => isIncrement ? curNum * 2 : curNum / 2"
                :value="numberOfInitialSamples"
                @blur="data => numberOfInitialSamples = data">
            </numeric-input>
            <span class="query-solver-option-divider"></span>
            <span class="query-solver-options-text">Discrete values</span>
            <numeric-input
              :min="2"
              :max="1000"
              :value="numberOfDiscreteValues"
              @blur="data => numberOfDiscreteValues = data">
            </numeric-input>
          </div>
        </div>
        <query-results :results="queryResults"
                       :selectedIx="selectedQueryResult"
                       @selectionChanged="(ix)=> { selectedQueryResult = ix; totalMainQueryCounter += 1;}"
                       class="query-results mt-2 mb-2"
                       v-if="showQueryResults && queryResults.length">
        </query-results>
      </div>
      <spinner :show="runningQueries > 0"
               hoverText="Click spinner to interrupt queries."
               @click="interruptQueries()"></spinner>
    </div>
    <div class="right-column" id="segModelEditorViewRightCol">
      <!--@TODO update when the HOGM solver can return map related query results-->
      <!-- We use the :key="totalMainQueryCounter" to assure that the complete state of
           the component is reset between each initial query -->
      <query-map-result :graph-query-result="graphQueryResult"
                        :key="totalMainQueryCounter"
                        @querySent="runningQueries += 1"
                        @queryReturned="runningQueries -= 1"
                        v-if="displayMap"></query-map-result>
      <query-chart-result :graph-query-result="graphQueryResult"
                          :key="totalMainQueryCounter"
                          @querySent="runningQueries += 1"
                          @queryReturned="runningQueries -= 1"
                          v-else></query-chart-result>
      <!-- @TODO replacement for the following is TBD soon, leave it commented out for now -->
      <!--<explanations :explanation-tree="explanationTree" id="explanations"></explanations>-->
    </div>
    <input-text-file @change="inputFileChanged" accept=".json" ref="input_ref"></input-text-file>
  </div>
</template>

<script>
  // @flow
  import Split from 'split.js';
  import cloneDeep from 'lodash/cloneDeep';
  import uniqBy from 'lodash/uniqBy';
  import identity from 'lodash/identity';
  import NumericInput from '@/components/NumericInput';
  import ActionButton from '@/components/ActionButton';
  import EditableDatalist from '@/components/EditableDatalist';
  import InputTextFile from '@/components/InputTextFile';
  import Spinner from '@/components/Spinner';
  import type { FileInfo } from '@/utils';
  import { HELP_VXC as HELP } from '@/store';
  import { mapGetters } from 'vuex';
  import Editor from './Editor';
  import Explanations from './explanations/Explanations';
  import SegmentedModelEditor from './SegmentedModelEditor';
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
    queryResults: [],
  };

  export default {
    name: 'SegmentedModelView',
    components: {
      Editor,
      ActionButton,
      InputTextFile,
      SegmentedModelEditor,
      QueryResults,
      QueryChartResult,
      MapImage,
      Explanations,
      EditableDatalist,
      QueryMapResult,
      NumericInput,
      Spinner,
    },
    data() {
      return {
        segmentedModels: [],
        modelOptionSelected: 0,
        modelOptions: [],
        segmentedModel: emptySegmentedModel,
        queryOptions: [],
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
          name: this.modelOptions[this.modelOptionSelected].text.trim(),
          description: this.segmentedModel.description.trim(),
          declarations,
          rules,
          queries: [...this.queryOptions],
        };
      },
      minimizeModel(model: SegmentedModelDto) : SegmentedModelDto {
        const coalesceRules = (rules: ModelRuleDto[]) : string =>
          rules.reduce((accum: string, modeRuleDto: ModelRuleDto) => {
          // eslint-disable-next-line no-param-reassign
            accum += ` ${modeRuleDto.rule}`;
            return accum;
          }, '');

        const coalesceRule: ModelRuleDto = {
          rule: coalesceRules(model.rules),
        };

        //  Strip metadata and coalesce rules
        const slimModel: SegmentedModelDto = {
          name: model.name,
          description: model.description,
          declarations: model.declarations,
          rules: [coalesceRule],
          queries: model.queries,
        };

        return slimModel;
      },
      async runQuery() {
        const selectedQuery = this.$refs.queryOption_ref.getCurrentOption();
        if (!selectedQuery) {
          return;
        }
        this.$refs.queryOption_ref.showList(false);
        this.$refs.queryOption_ref.addCurrentEntry();
        const sm: SegmentedModelDto = await this.getUpdatedSegmentedModel();
        const model: string = `${sm.declarations || ''}\n${sm.rules.map(mr => mr.rule).join('\n')}\n`;

        const query: ModelQueryDto = {
          model,
          query: selectedQuery,
          numberOfInitialSamples: this.numberOfInitialSamples,
          numberOfDiscreteValues: this.numberOfDiscreteValues,
        };

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
      async download() {
        const sm: SegmentedModelDto = await this.getUpdatedSegmentedModel();
        if (this.queryOptions) {
          sm.queries = this.$refs.queryOption_ref.getAllOptions();
        }
        this.$$.downloadFile(sm, `${sm.name}.json`);
      },
      modelSelectionChanged(modelIx: number) {
        const smd: SegmentedModelDto = this.segmentedModels[modelIx];
        this.segmentedModel = cloneDeep(smd);
        this.dclEditTextWatch = !this.dclEditTextWatch;
        this.queryOptions = uniqBy(smd.queries, identity);
        this.queryResults = [];
      },
      setupModels(models: SegmentedModelDto[]) {
        this.segmentedModels = models;
        const modelOptions = [];
        for (let i = 0; i < models.length; i += 1) {
          modelOptions.push({ value: i, text: models[i].name });
        }
        this.modelOptions = modelOptions;
        this.modelOptionSelected = 0;
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
        this.modelSelectionChanged(this.modelOptionSelected);
      },
      async loadModelsFromServer() {
        const models: SegmentedModelDto[] = await this.loadModels();
        if (models.length) {
          this.setupModels(models);
          this.modelSelectionChanged(this.modelOptionSelected);
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
      this.splitter$ = Split(['#segModelEditorViewLeftCol', '#segModelEditorViewRightCol'], {
        sizes: [65, 35],
        onDragEnd() {
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

  .segmentedEditor {
    flex: 1 1 auto;
    position: relative;
    overflow-y: auto;
  }

  .modelControlsContainer {
    border: thin double lightgrey;
    padding: 10px;
  }

  .modelControlsPanel {
    padding: 10px;
    flex: 0 0 auto;
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

  .help {
    background-color: lightyellow;
    border-bottom-color: green;
  }

  .query-solver-options {
    display: flex;
    flex-direction: row;
    margin-top: 0.5rem;
    margin-left: 0.25rem;
  }
  .query-solver-options-text {
    margin-top: 0.1rem;
    font-size: 0.95rem;
    margin-right: 0.5rem;
  }
  .query-solver-option-divider {
    margin-right: 0.75rem;
  }
</style>