<template>
  <div>
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
        <span v-show="!showQueryResults || !queryResults.length">
          <action-button
              type="eye-slash"
              title="Show query results"
              :disabled="!queryResults.length"
              @clicked="() => showQueryResults = !showQueryResults">
          </action-button>
        </span>
        <span v-show="showQueryResults && queryResults.length">
          <action-button
              type="eye"
              title="Hide query results"
              @clicked="() => showQueryResults = !showQueryResults">
          </action-button>
        </span>
      </b-button-toolbar>
      <query-results v-if="showQueryResults && queryResults.length"
                     class="mt-2 mb-2" :results="queryResults"></query-results>
      <div>
        <b-form-textarea class="mt-2 mb-2" v-model="segmentedModel.description"
                         placeholder="Enter a description for the model"
                         max-rows="3"
                         wrap="off"
                         :no-resize="true">
        </b-form-textarea>
      </div>
    </div>

    <div class="segmentedEditor">
      <div class="dcl-border">
        <editor ref="dcl_editor_ref" type="hogm" :value="segmentedModel.declarations"></editor>
      </div>
      <segmented-model-editor ref="seg_model_editor_ref" :rules="segmentedModel.rules"></segmented-model-editor>
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
      };
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
          queries: this.queryOptions.map(e => e.text),
        };
      },
      async runQuery() {
        const sm : SegmentedModelDto = await this.getUpdatedSegmentedModel();
        const model: string = `${sm.declarations || ''}\n${sm.rules.map(mr => mr.rule).join('\n')}\n`;

        const query: ModelQueryDto = {
          model,
          query: this.queryOptions[this.queryOptionSelected].text,
        };

        try {
          const result = await solve(query);
          this.queryResults = [result].concat(this.queryResults);
        } catch (err) {
          // errors already logged/displayed
        }
      },
      async download() {
        const sm : SegmentedModelDto = await this.getUpdatedSegmentedModel();
        this.$$.downloadFile(sm, `${sm.name}.json`);
      },
      modelSelectionChanged(modelIx: number) {
        const getQueryOptions = (smd: SegmentedModelDto) => {
          const queryOptions = [];
          if (smd.queries) {
            for (let i = 0; i < smd.queries.length; i += 1) {
              queryOptions.push({ value: i, text: smd.queries[i] });
            }
          }
          return queryOptions;
        };

        const smd: SegmentedModelDto = this.segmentedModels[modelIx];
        this.segmentedModel = cloneDeep(smd);

        const qo = getQueryOptions(smd);
        this.queryOptions = qo;
        this.queryOptionSelected = qo.length ? 0 : -1;
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
    },
    async created() {
      const models: SegmentedModelDto[] = await this.loadModels();
      if (models.length) {
        this.setupModels(models);
        this.modelSelectionChanged(this.modelOptionSelected);
      }
    },
  };
</script>

<style scoped>
  .dcl-border {
    border: thin double lightgrey;
    padding: 10px;
  }

  .topPanel {
    max-height: calc(30vh);
  }

  .segmentedEditor {
    max-height: calc(70vh);
    overflow-y: auto;
  }
</style>