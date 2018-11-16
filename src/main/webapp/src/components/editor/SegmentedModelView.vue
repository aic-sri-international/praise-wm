<template>
  <div class="top-level-container">
    <div id="segModelEditorViewLeftCol" class="left-column">
      <div>
        <b-form-textarea id="segmentedModelDescription"
                         class="mb-2" v-model="segmentedModel.description"
                         placeholder="Enter a description for the model"
                         rows="1"
                         size="sm"
                         max-rows="20"
                         wrap="off">
        </b-form-textarea>
        <b-popover target="segmentedModelDescription" triggers="" :show="showHelp">
          <span class="help">Describes the model.</span>
        </b-popover>
      </div>
      <div id="segmentedEditor" class="segmentedEditor">
        <div class="dcl-editor">
          <editor id="dclEditor" ref="dcl_editor_ref" :editTextWatch="dclEditTextWatch"
                  type="hogm" :value="segmentedModel.declarations">
          </editor>
        </div>
        <b-popover target="dclEditor" triggers="" :show="showHelp">
          <span class="help">Global model declarations section.</span>
        </b-popover>
        <segmented-model-editor id="segmented-model-editor" ref="seg_model_editor_ref"
                                :rules="segmentedModel.rules">
        </segmented-model-editor>
        <b-popover target="segmentedEditor" triggers="" :show="showHelp">
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
            <b-input-group size="sm" class="ml-1" prepend="Model">
              <b-form-select
                  @input="modelSelectionChanged"
                  v-model="modelOptionSelected"
                  :options="modelOptions">
              </b-form-select>
            </b-input-group>
            <span class="ml-1"></span>
            <action-button
                type="open"
                title="Open Model"
                @clicked="()=>$refs.input_ref.click()">
            </action-button>
            <action-button
                type="download"
                title="Download Model"
                @clicked="download">
            </action-button>
            <action-button
                type="sync"
                title="Reload models from server"
                @clicked="loadModelsFromServer">
            </action-button>
          </div>
          <div style="display: flex; flex-direction: row">
            <editable-datalist
                ref="queryOption_ref"
                class="ml-1"
                label="Query"
                placeholder="Please enter a query ..."
                :options="queryOptions">
            </editable-datalist>
            <span class="ml-1"></span>
            <action-button
                type="play"
                title="Run the query"
                @clicked="runQuery">
            </action-button>
            <action-button
                type="broom"
                title="Remove query results"
                @clicked="()=> queryResults = []">
            </action-button>
          </div>
        </div>
        <query-results v-if="showQueryResults && queryResults.length"
                       class="query-results mt-2 mb-2"
                       :selectedIx="selectedQueryResult"
                       @selectionChanged="(ix)=> selectedQueryResult = ix"
                       :results="queryResults">
        </query-results>
      </div>
      <div id="runningQueries" v-if="runningQueries" class="querySpinner"
           @click.stop="interruptQueries">
        <span class="fa-layers fa-fw">
           <i class="fas fa-spinner fa-pulse" data-fa-transform="grow-80" style="color: green"></i>
        </span>
        <b-popover target="runningQueries" triggers="hover">
          <span>Click spinner to interrupt queries.</span>
        </b-popover>
      </div>
    </div>
    <div id="segModelEditorViewRightCol" class="right-column">
      <map-image v-if="!graphQueryResult"></map-image>
      <query-chart-result v-else :graph-query-result="graphQueryResult"></query-chart-result>
      <explanations id="explanations" :explanation-tree="explanationTree"></explanations>
    </div>
    <input-text-file ref="input_ref" @change="inputFileChanged" accept=".json"></input-text-file>
  </div>
</template>

<script>
  // @flow
  import Split from 'split.js';
  import cloneDeep from 'lodash/cloneDeep';
  import uniqBy from 'lodash/uniqBy';
  import identity from 'lodash/identity';
  import ActionButton from '@/components/ActionButton';
  import EditableDatalist from '@/components/EditableDatalist';
  import InputTextFile from '@/components/InputTextFile';
  import type { FileInfo } from '@/utils';
  import { HELP_VXC as HELP } from '@/store';
  import { mapGetters } from 'vuex';
  import Editor from './Editor';
  import Explanations from './explanations/Explanations';
  import SegmentedModelEditor from './SegmentedModelEditor';
  import QueryResults from './QueryResults';
  import QueryChartResult from './QueryChartResult';
  import MapImage from './MapImage';
  import { fetchSegmentedModels, solve, interruptSolver } from './dataSourceProxy';
  import type {
    SegmentedModelDto,
    ModelRuleDto,
    ModelQueryDto,
    ExpressionResultDto,
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
    },
    data() {
      return {
        segmentedModels: [],
        modelOptionSelected: 0,
        modelOptions: [],
        segmentedModel: emptySegmentedModel,

        queryOptions: [],
        queryResults: [],
        showQueryResults: true,
        dclEditTextWatch: false,
        selectedQueryResult: -1,
        runningQueries: 0,
      };
    },
    computed: {
      explanationTree() {
        if (this.selectedQueryResult > -1 && this.queryResults.length > 0) {
          return this.queryResults[this.selectedQueryResult].explanationTree;
        }
        return null;
      },
      graphQueryResult() {
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
        };

        try {
          this.runningQueries = this.runningQueries + 1;
          const arrayOfResults: ExpressionResultDto[] = await solve(query);
          if (arrayOfResults.length) {
            // We should not normally get multiple results for a query.
            // If we do, only use the first one.
            const result: ExpressionResultDto = arrayOfResults[0];
            this.queryResults = [result].concat(this.queryResults);
            this.showQueryResults = true;
            this.selectedQueryResult = 0;
          }

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
        const model: SegmentedModelDto = JSON.parse(text);

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
      if (this.splitter$ !== undefined) {
        this.splitter$.destroy();
      }
      this.splitter$ = Split(['#segModelEditorViewLeftCol', '#segModelEditorViewRightCol'], {
        sizes: [65, 35],
      });
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

  .querySpinner {
    position: fixed;
    top: 50%;
    left: 50%;
    cursor: pointer;
  }

</style>