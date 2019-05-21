<template>
  <div>
    <div class="modelControlsPanel">
      <div style="display: flex; flex-direction: row">
        <b-input-group
          class="ml-1"
          prepend="Model"
          size="sm"
        >
          <b-form-select
            id="modelSelectionId"
            v-model="modelOptionSelected"
            :disabled="isQueryActive"
            :options="modelOptions"
            @input="(modelIx)=>modelSelectionChanged(modelIx)"
          />
        </b-input-group>
        <span class="ml-1" />
        <action-button
          v-tippy
          :disabled="isQueryActive"
          title="Open and read model from disk"
          type="open"
          @clicked="$refs.inputRef.click()"
        />
        <action-button
          v-tippy
          title="Download current model to disk"
          type="download"
          @clicked="saveCurrentModelToDisk"
        />
        <action-button
          v-tippy
          :disabled="isQueryActive"
          title="Reload models from server"
          type="sync"
          @clicked="initialize"
        />
      </div>
      <div style="display: flex; flex-direction: row">
        <editable-datalist
          id="modelQueryId"
          ref="queryOptionRef"
          :disabled="isQueryActive"
          :options="curQueries"
          class="ml-1"
          label="Query"
          placeholder="Please enter a query ..."
          @currentEntryChanged="currentQueryChanged"
          @optionsChanged="queryListChanged"
        />
        <span class="ml-1" />
        <action-button
          v-tippy
          :disabled="isQueryActive"
          title="Run the query"
          type="play"
          @clicked="submitQuery"
        />
        <action-button
          v-tippy
          :disabled="isQueryActive"
          title="Remove query results"
          type="broom"
          @clicked="clearQueryResults"
        />
      </div>
      <div class="query-solver-options">
        <div class="query-solver-component mr-4">
          <div class="query-solver-options-text">
            Initial samples
          </div>
          <numeric-input
            :disabled="isQueryActive"
            :max="100000000"
            :min="1"
            :step="(curNum, isIncrement) => isIncrement ? curNum * 2 : curNum / 2"
            :value="numberOfInitialSamples"
            @blur="data => setNumberOfInitialSamples(data)"
          />
        </div>
        <div class="query-solver-component">
          <span class="query-solver-options-text">Discrete values</span>
          <numeric-input
            :disabled="isQueryActive"
            :max="1000"
            :min="2"
            :value="numberOfDiscreteValues"
            @blur="data => setNumberOfDiscreteValues(data)"
          />
        </div>
        <div
          id="querySolverOptionsId"
          :class="{ querySolverOptionsHelpOffset: showHelp }"
          class="query-solver-component"
          style="width: 1px"
        />
      </div>
    </div>
    <b-popover
      :offset="-240"
      :show="showHelp"
      placement="topleft"
      target="modelSelectionId"
      triggers=""
    >
      Select the model to be used for the query
    </b-popover>
    <b-popover
      :show="showHelp"
      placement="topright"
      target="modelQueryId"
      triggers=""
    >
      <div class="help-title">
        The query to be used for the model
      </div>
      <div>You can enter a new query or select a prior query from the dropdown list.</div>
      <div>Right-click in the query field to select an action from the popup menu.</div>
    </b-popover>
    <b-popover
      :show="showHelp"
      placement="right"
      target="querySolverOptionsId"
      triggers=""
    >
      Query solver tuning parameters
    </b-popover>
    <input-text-file
      ref="inputRef"
      :multiple="true"
      accept=".json"
      @change="loadModelsFromDisk"
    />
  </div>
</template>

<script lang="ts">
  import { Component, Vue, Watch } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import NumericInput from '@/components/NumericInput.vue';
  import ActionButton from '@/components/ActionButton.vue';
  import EditableDatalist from '@/components/EditableDatalist.vue';
  import InputTextFile from '@/components/InputTextFile.vue';
  import { SegmentedModelDto, VuexModelState } from '@/store/model/types';
  import { FileInfo } from '@/utils';
  import { EditableDatalistInterface } from '@/components/EditableDatalistInterface.types';
  import { HELP_MODULE_NAME } from '@/store/help/constants';
  import { MODEL_MODULE_NAME } from '@/store/model/constants';
  import { VuexHelpState } from '@/store/help/types';

  type ModelOption = {
    text: string,
    value: number,
  }

  const helpModule = namespace(HELP_MODULE_NAME);
  const modelModule = namespace(MODEL_MODULE_NAME);

  @Component({
    components: {
      ActionButton,
      InputTextFile,
      EditableDatalist,
      NumericInput,
    },
  })
  export default class ModelControlsPanel extends Vue {
    modelOptions: ModelOption[] = [];

    modelOptionSelected = 0;

    $refs!: {
      queryOptionRef: EditableDatalistInterface,
    };

    @helpModule.State showHelp!: VuexHelpState['showHelp'];

    @modelModule.State curModelName!: VuexModelState['curModelName'];

    @modelModule.State numberOfInitialSamples!: VuexModelState['numberOfInitialSamples'];

    @modelModule.State numberOfDiscreteValues!: VuexModelState['numberOfDiscreteValues'];

    @modelModule.Getter isQueryActive!: boolean;

    @modelModule.Getter modelNames!: string[];

    @modelModule.Getter curModelDto!: SegmentedModelDto;

    @modelModule.Getter curQueries!: string[];

    @modelModule.Action initialize!: () => Promise<any>;

    @modelModule.Action loadModelsFromDisk!: (payload: FileInfo[]) => Promise<any>;

    @modelModule.Action saveCurrentModelToDisk!: () => Promise<any>;

    @modelModule.Action changeCurrentModel!: (modelName: string) => Promise<any>;

    @modelModule.Action runQuery!: () => Promise<any>;

    @modelModule.Mutation setCurQuery!: (query: string) => void;

    @modelModule.Mutation setCurModelQueries!: (queries: string[]) => void;

    @modelModule.Mutation setNumberOfInitialSamples!: (initialSamples: number) => void;

    @modelModule.Mutation setNumberOfDiscreteValues!: (discreteValues: number) => void;

    @modelModule.Mutation clearQueryResults!: () => void;


    @Watch('curModelName')
    onCurModelName(curName: string) {
      const ix = this.modelOptions.findIndex(option => option.text === curName);
      if (ix !== -1) {
        this.modelOptionSelected = ix;
      }
    }

    @Watch('modelNames')
    onModelNames(modelNames: string[]) {
      this.initModelNames(modelNames);
    }

    initModelNames(names: string[]) {
      const options = [];
      for (let i = 0; i < names.length; i += 1) {
        options.push({
          value: i,
          text: names[i],
        });
      }
      this.modelOptions = options;
      this.modelOptionSelected = 0;
    }

    queryListChanged(curQueries: string[]) {
      this.setCurModelQueries(curQueries);
    }

    currentQueryChanged(curQuery: string) {
      this.setCurQuery(curQuery);
    }

    async modelSelectionChanged(modelIx: number) {
      const modelOption = this.modelOptions[modelIx];
      const modelName: string = modelOption.text;
      await this.changeCurrentModel(modelName);
    }

    async submitQuery() {
      const query = this.$refs.queryOptionRef.getCurrentOption();
      if (!query) {
        return;
      }
      this.$refs.queryOptionRef.showList(false);
      this.$refs.queryOptionRef.addCurrentEntry();
      await this.runQuery();
    }
  }
</script>

<style lang="scss" scoped>
  .modelControlsPanel {
    padding: 10px;
    flex: 0 0 auto;
  }

  .query-solver-options {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    margin-top: 0.5rem;
    margin-left: 0.25rem;
  }

  .querySolverOptionsHelpOffset {
    margin-left: 1.5rem !important;
  }

  .query-solver-component {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap
  }

  .query-solver-options-text {
    margin-top: 0.1rem;
    font-size: 0.95rem;
    margin-right: 0.5rem;
    white-space: nowrap;
  }
</style>
