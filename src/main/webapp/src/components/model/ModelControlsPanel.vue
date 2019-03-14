<template>
  <div>
    <div class="modelControlsPanel">
      <div style="display: flex; flex-direction: row">
        <b-input-group class="ml-1" prepend="Model" size="sm">
          <b-form-select
              :options="modelOptions"
              @input="(modelIx)=>modelSelectionChanged(modelIx)"
              id="modelSelectionId"
              v-model="modelOptionSelected">
          </b-form-select>
        </b-input-group>
        <span class="ml-1"></span>
        <action-button
            @clicked="$refs.input_ref.click()"
            title="Open and read model from disk"
            type="open"
            v-tippy>
        </action-button>
        <action-button
            @clicked="saveCurrentModelToDisk"
            title="Download current model to disk"
            type="download"
            v-tippy>
        </action-button>
        <action-button
            @clicked="initialize"
            title="Reload models from server"
            type="sync"
            v-tippy>
        </action-button>
      </div>
      <div style="display: flex; flex-direction: row">
        <editable-datalist
            :options="curQueries"
            @currentEntryChanged="currentQueryChanged"
            @optionsChanged="queryListChanged"
            class="ml-1"
            id="modelQueryId"
            label="Query"
            placeholder="Please enter a query ..."
            ref="queryOption_ref">
        </editable-datalist>
        <span class="ml-1"></span>
        <action-button
            @clicked="runQuery"
            title="Run the query"
            type="play"
            v-tippy>
        </action-button>
        <action-button
            @clicked="()=>$emit('clearQueryResults')"
            title="Remove query results"
            type="broom"
            v-tippy>
        </action-button>
      </div>
      <div class="query-solver-options">
        <div class="query-solver-component mr-4">
          <div class="query-solver-options-text">Initial samples</div>
          <numeric-input
              :max="100000000"
              :min="1"
              :step="(curNum, isIncrement) => isIncrement ? curNum * 2 : curNum / 2"
              :value="numberOfInitialSamples"
              @blur="data => numberOfInitialSamples = data">
          </numeric-input>
        </div>
        <div class="query-solver-component">
          <span class="query-solver-options-text">Discrete values</span>
          <numeric-input
              :max="1000"
              :min="2"
              :value="numberOfDiscreteValues"
              @blur="data => numberOfDiscreteValues = data">
          </numeric-input>
        </div>
        <div :class="{ querySolverOptionsHelpOffset: showHelp }" class="query-solver-component" id="querySolverOptionsId"
             style="width: 1px">
        </div>
      </div>
    </div>
    <b-popover :offset="-240" :show="showHelp" placement="topleft" target="modelSelectionId"
               triggers="">
      Select the model to be used for the query
    </b-popover>
    <b-popover :show="showHelp" placement="topright" target="modelQueryId" triggers="">
      <div class="help-title">The query to be used for the model</div>
      <div>You can enter a new query or select a prior query from the dropdown list.</div>
      <div>Right-click in the query field to select an action from the popup menu.</div>
    </b-popover>
    <b-popover :show="showHelp" placement="right" target="querySolverOptionsId" triggers="">
      Query solver tuning parameters
    </b-popover>
    <input-text-file @change="loadModelFromDisk" accept=".json" ref="input_ref"></input-text-file>
  </div>
</template>

<script>
  // @flow
  import isEqual from 'lodash/isEqual';
  import NumericInput from '@/components/NumericInput';
  import ActionButton from '@/components/ActionButton';
  import EditableDatalist from '@/components/EditableDatalist';
  import InputTextFile from '@/components/InputTextFile';
  import { HELP_VXC as HELP, MODEL_VXC as MODEL } from '@/store';
  // import { editorTransitions } from '@/store/model/types';
  // import type { EditorTransition } from '@/store/model/types';
  import { mapGetters, mapMutations, mapActions } from 'vuex';
  import { modelQueryDtoDefaults } from './types';
  import type {
    ModelQueryOptions,
    // SegmentedModelDto,
    // ModelRuleDto,
    // GraphQueryResultDto,
  } from './types';

  export default {
    name: 'ModelControlsPanel',
    components: {
      ActionButton,
      InputTextFile,
      EditableDatalist,
      NumericInput,
    },
    data() {
      return {
        modelOptions: [],
        modelOptionSelected: 0,
        numberOfInitialSamples: modelQueryDtoDefaults.numberOfInitialSamples,
        numberOfDiscreteValues: modelQueryDtoDefaults.numberOfDiscreteValues,
      };
    },
    computed: {
      ...mapGetters(HELP.MODULE, [
        HELP.GET.SHOW_HELP,
      ]),
      ...mapGetters(MODEL.MODULE, [
        MODEL.GET.MODEL_NAMES,
        MODEL.GET.CUR_MODEL_DTO,
        MODEL.GET.CUR_MODEL_NAME,
        MODEL.GET.CUR_QUERIES,
      ]),
    },
    methods: {
      ...mapActions(MODEL.MODULE, [
        MODEL.ACTION.INITIALIZE,
        MODEL.ACTION.LOAD_MODEL_FROM_DISK,
        MODEL.ACTION.SAVE_CURRENT_MODEL_TO_DISK,
        MODEL.ACTION.CHANGE_CURRENT_MODEL,
      ]),
      ...mapMutations(MODEL.MODULE, [
        MODEL.SET.CUR_QUERY,
        MODEL.SET.CUR_MODEL_QUERIES,
      ]),
      initModelNames(names: string[]) {
        const options = [];
        for (let i = 0; i < names.length; i += 1) {
          options.push({ value: i, text: names[i] });
        }
        this.modelOptions = options;
        this.modelOptionSelected = 0;
      },
      queryListChanged(curQueries: string[]) {
        this.setCurModelQueries(curQueries);
      },
      currentQueryChanged(curQuery: string) {
        this.setCurQuery(curQuery);
      },
      modelSelectionChanged(modelIx: number) {
        const modelOption = this.modelOptions[modelIx];
        const modelName: string = modelOption.text;
        this.changeCurrentModel(modelName);
      },
      runQuery() {
        const query = this.$refs.queryOption_ref.getCurrentOption();
        if (!query) {
          return;
        }
        this.$refs.queryOption_ref.showList(false);
        this.$refs.queryOption_ref.addCurrentEntry();
        const options: ModelQueryOptions = {
          query,
          numberOfInitialSamples: this.numberOfInitialSamples,
          numberOfDiscreteValues: this.numberOfDiscreteValues,
        };
        this.$emit('runQuery', options);
      },
    },
    watch: {
      curModelName(curModelName: string) {
        const ix = this.modelOptions.findIndex(option => option.text === curModelName);
        if (ix !== -1) {
          this.modelOptionSelected = ix;
        }
      },
      modelNames(modelNames: string[]) {
        this.initModelNames(modelNames);
      },
      inputQueries(newValue: string[]) {
        if (!isEqual(newValue, this.queries)) {
          this.queries = [...newValue];
        }
      },
    },
  };
</script>

<style scoped>
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
