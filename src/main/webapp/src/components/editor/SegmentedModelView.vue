<template>
  <div class="top-level-container">
    <div class="left-column">
      <div>
        <b-form-textarea id="segmentedModelDescription"
                         class="mb-2" v-model="segmentedModel.description"
                         placeholder="Enter a description for the model"
                         rows="1"
                         size="sm"
                         max-rows="20"
                         wrap="off">
        </b-form-textarea>
        <b-popover target="segmentedModelDescription"
                   triggers=""
                   :show.sync="displayHelp">
          <span class="help">
          Describes the model.
          </span>
        </b-popover>
      </div>
      <div id="segmentedEditor" class="segmentedEditor">
        <div class="dcl-editor">
          <editor id="dclEditor" ref="dcl_editor_ref"
                  :editTextWatch="dclEditTextWatch"
                  type="hogm" :value="segmentedModel.declarations">
          </editor>
        </div>
        <b-popover target="dclEditor"
                   triggers=""
                   :show.sync="displayHelp">
          <span class="help">Global model declarations section.</span>
        </b-popover>
        <segmented-model-editor id="segmented-model-editor" ref="seg_model_editor_ref"
                                :rules="segmentedModel.rules">
        </segmented-model-editor>
        <b-popover target="segmentedEditor"
                   triggers=""
                   :show.sync="displayHelp">
          <span class="help">
            Right-click within a rule section to display a context menu. The context menu
            allows you to toggle the display of metadata for the rule, insert a new rule,
            or delete the rule.
          </span>
        </b-popover>
      </div>
      <div class="modelControlsContainer">
        <div class="modelControlsPanel">
          <b-button-toolbar>
            <b-button-group size="sm">
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
            </b-button-group>
            <b-input-group size="sm" class="w-50 mx-1" prepend="Model">
              <b-form-select
                  @input="modelSelectionChanged"
                  v-model="modelOptionSelected"
                  :options="modelOptions">
              </b-form-select>
            </b-input-group>
            <span class="ml-1">
          <action-button
              type="sync"
              title="Reload models from server"
              @clicked="loadModelsFromServer">
          </action-button>
        </span>
            <action-button
                type="help"
                title="Toggle field level help"
                @clicked="displayHelp = !displayHelp">
            </action-button>
          </b-button-toolbar>
          <b-button-toolbar>
            <b-button-group size="sm">
              <action-button
                  type="play"
                  title="Run the query"
                  @clicked="runQuery">
              </action-button>
              <action-button
                  type="broom"
                  title="Sweep away query results"
                  @clicked="()=> queryResults = []">
              </action-button>
            </b-button-group>
            <b-input-group size="sm" class="w-50 mx-2" prepend="Query">
              <b-form-select
                  v-model="queryOptionSelected"
                  :options="queryOptions">
              </b-form-select>
            </b-input-group>
            <action-button
                type="edit"
                title="Toggle query editor"
                @clicked="() => showQueryEditor = !showQueryEditor">
            </action-button>
            <span v-show="!showQueryResults || !queryResults.length">
              <action-button
                  type="eye"
                  title="Show query results"
                  :disabled="!queryResults.length"
                  @clicked="() => showQueryResults = true">
              </action-button>
            </span>
            <span v-show="showQueryResults && queryResults.length">
              <action-button
                  type="eye-slash"
                  title="Hide query results"
                  @clicked="() => showQueryResults = false">
              </action-button>
            </span>
          </b-button-toolbar>
        </div>
        <query-editor v-if="showQueryEditor"
                      class="query-editor"
                      :queries="currentQueries"
                      @queriesChanged="(qs) => {updateQueryOptions(qs); showQueryEditor = false}">
        </query-editor>
        <query-results v-if="showQueryResults && queryResults.length"
                       class="query-results mt-2 mb-2"
                       :selectedIx="selectedQueryResult"
                       @selectionChanged="(ix)=> selectedQueryResult = ix"
                       :results="queryResults">
        </query-results>
      </div>
      <div class="left-column-bottom"></div>
    </div>
    <div class="right-column">
      <img src="../../../static/images/South_Sudan_food_security.jpg">
      <div class="explanations mt-2">
        <explanations :explanation-tree="explanationTree"></explanations>
      </div>
    </div>
    <input-text-file ref="input_ref" @change="inputFileChanged"
                     accept=".json"></input-text-file>
  </div>
</template>

<script>
  // @flow
  import cloneDeep from 'lodash/cloneDeep';
  import ActionButton from '@/components/ActionButton';
  import InputTextFile from '@/components/InputTextFile';
  import type { FileInfo } from '@/utils';
  import Editor from './Editor';
  import Explanations from './explanations/Explanations';
  import SegmentedModelEditor from './SegmentedModelEditor';
  import QueryEditor from './QueryEditor';
  import QueryResults from './QueryResults';
  import { fetchSegmentedModels, solve } from './dataSourceProxy';
  import type { SegmentedModelDto, ModelRuleDto, ModelQueryDto } from './types';

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
      QueryEditor,
      QueryResults,
      Explanations,
    },
    data() {
      return {
        segmentedModels: [],
        modelOptionSelected: 0,
        modelOptions: [],
        segmentedModel: emptySegmentedModel,

        queryOptions: [],
        queryOptionSelected: -1,
        queryResults: [],
        showQueryResults: false,
        showQueryEditor: false,
        displayHelp: false,
        dclEditTextWatch: false,
        selectedQueryResult: -1,
      };
    },
    computed: {
      currentQueries() {
        return this.queryOptions.map(e => e.text);
      },
      explanationTree() {
        if (this.selectedQueryResult > -1 && this.queryResults.length > 0) {
          return this.queryResults[this.selectedQueryResult].explanationTree;
        }
        return null;
      },
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
          queries: this.currentQueries,
        };
      },
      async runQuery() {
        if (this.queryOptionSelected === -1) {
          this.showQueryEditor = true;
          return;
        }
        const sm: SegmentedModelDto = await this.getUpdatedSegmentedModel();
        const model: string = `${sm.declarations || ''}\n${sm.rules.map(mr => mr.rule).join('\n')}\n`;

        const query: ModelQueryDto = {
          model,
          query: this.queryOptions[this.queryOptionSelected].text,
        };

        try {
          const arrayOfResults = await solve(query);
          if (arrayOfResults.length) {
            // We should not normally get multiple results for a query.
            // If we do, only use the first one.
            const result = arrayOfResults[0];
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
        }
      },
      async download() {
        const sm: SegmentedModelDto = await this.getUpdatedSegmentedModel();
        this.$$.downloadFile(sm, `${sm.name}.json`);
      },
      updateQueryOptions(queries: string[]) {
        const queryOptions = [];
        if (queries) {
          for (let i = 0; i < queries.length; i += 1) {
            queryOptions.push({ value: i, text: queries[i] });
          }
        }
        this.queryOptions = queryOptions;
        this.queryOptionSelected = queryOptions.length ? 0 : -1;
      },
      modelSelectionChanged(modelIx: number) {
        const smd: SegmentedModelDto = this.segmentedModels[modelIx];
        this.segmentedModel = cloneDeep(smd);
        this.dclEditTextWatch = !this.dclEditTextWatch;
        this.updateQueryOptions(smd.queries);
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
    },
    async created() {
      this.loadModelsFromServer();
    },
  };
</script>

<style scoped>
  .top-level-container {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: row;
  }

  .left-column {
    height: 100%;
    width: 70%;
    display: flex;
    flex-direction: column;
  }

  .right-column {
    height: 100%;
    width: 30%;
    padding-left: 10px;
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

  .left-column-bottom {
    flex: 2 2 auto;
  }

  .dcl-editor {
    border: thin double lightgrey;
    padding: 10px;
  }

  .query-editor {
    border: thin double lightgrey;
    padding: 4px;
  }

  .query-results {
    border: thin double lightgrey;
    padding: 4px;
    max-height: 140px;
    overflow: auto
  }

  .explanations {
    border: thin double lightgrey;
    min-height: 20px;
    min-width: 20px;
    text-align: left;
    padding: 10px;
    position: relative;
    white-space: nowrap;
    overflow: auto;
  }

  img {
    max-width: 100%;
    height: auto;
  }

  .help {
    background-color: lightyellow;
    border-bottom-color: green;
  }

</style>