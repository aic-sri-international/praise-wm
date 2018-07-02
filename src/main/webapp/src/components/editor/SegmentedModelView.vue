<template>
  <div class="top-level-container">
    <div class="topPanel">
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
              type="eye-slash"
              title="Show query results"
              :disabled="!queryResults.length"
              @clicked="() => showQueryResults = true">
          </action-button>
        </span>
        <span v-show="showQueryResults && queryResults.length">
          <action-button
              type="eye"
              title="Hide query results"
              @clicked="() => showQueryResults = false">
          </action-button>
        </span>
      </b-button-toolbar>
      <query-editor v-if="showQueryEditor"
                    :queries="currentQueries"
                    @queriesChanged="(qs) => {updateQueryOptions(qs); showQueryEditor = false}">
      </query-editor>
      <query-results v-if="showQueryResults && queryResults.length"
                     class="mt-2 mb-2" :results="queryResults"></query-results>
      <div>
        <b-form-textarea id="segmentedModelDescription"
                         class="mt-2 mb-2" v-model="segmentedModel.description"
                         placeholder="Enter a description for the model"
                         max-rows="3"
                         wrap="off"
                         :no-resize="true">
        </b-form-textarea>
        <b-popover target="segmentedModelDescription"
                   triggers=""
                   :show.sync="displayHelp">
          This field describes the model.
        </b-popover>
      </div>
    </div>

    <div class="segmentedEditor">
      <div class="dcl-border">
        <editor id="dclEditor" ref="dcl_editor_ref"
                :editTextWatch="dclEditTextWatch"
                type="hogm" :value="segmentedModel.declarations">
        </editor>
      </div>
      <b-popover target="dclEditor"
                 triggers=""
                 :show.sync="displayHelp">
        Global model declarations section.
      </b-popover>
      <segmented-model-editor ref="seg_model_editor_ref"
                              :displayHelp="displayHelp"
                              :rules="segmentedModel.rules">
      </segmented-model-editor>
      <input-text-file ref="input_ref" @change="inputFileChanged" accept=".json"></input-text-file>
    </div>
  </div>
</template>

<script>
  // @flow
  import cloneDeep from 'lodash/cloneDeep';
  import ActionButton from '@/components/ActionButton';
  import InputTextFile from '@/components/InputTextFile';
  import type { FileInfo } from '@/utils';
  import Editor from './Editor';
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
      };
    },
    computed: {
      currentQueries() {
        return this.queryOptions.map(e => e.text);
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
        const sm : SegmentedModelDto = await this.getUpdatedSegmentedModel();
        const model: string = `${sm.declarations || ''}\n${sm.rules.map(mr => mr.rule).join('\n')}\n`;

        const query: ModelQueryDto = {
          model,
          query: this.queryOptions[this.queryOptionSelected].text,
        };

        try {
          const result = await solve(query);
          this.queryResults = [result].concat(this.queryResults);
          if (this.queryResults.length) {
            this.showQueryResults = true;
          }
        } catch (err) {
          // errors already logged/displayed
        }
      },
      async download() {
        const sm : SegmentedModelDto = await this.getUpdatedSegmentedModel();
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
    display: flex;
    flex-direction: column;
  }

  .topPanel {
    flex: 0 0 auto;
  }

  .segmentedEditor {
    flex: 1 1 auto;
    position: relative;
    overflow-y: auto;
  }

  .dcl-border {
    border: thin double lightgrey;
    padding: 10px;
  }

</style>