<template>
  <div>
    <div class="modelControlsPanel">
      <div style="display: flex; flex-direction: row">
        <b-input-group class="ml-1" prepend="Model" size="sm">
          <b-form-select
              :disabled="isQueryActive"
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
            :disabled="isQueryActive"
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
            :disabled="isQueryActive"
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
            :disabled="isQueryActive"
            ref="queryOption_ref">
        </editable-datalist>
        <span class="ml-1"></span>
        <action-button
            @clicked="submitQuery"
            title="Run the query"
            type="play"
            :disabled="isQueryActive"
            v-tippy>
        </action-button>
        <action-button
            @clicked="clearQueryResults"
            title="Remove query results"
            type="broom"
            :disabled="isQueryActive"
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
              :disabled="isQueryActive"
              @blur="data => setNumberOfInitialSamples(data)">
          </numeric-input>
        </div>
        <div class="query-solver-component">
          <span class="query-solver-options-text">Discrete values</span>
          <numeric-input
              :max="1000"
              :min="2"
              :value="numberOfDiscreteValues"
              :disabled="isQueryActive"
              @blur="data => setNumberOfDiscreteValues(data)">
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
    <input-text-file
        @change="loadModelsFromDisk"
        accept=".json"
        :multiple="true"
        ref="input_ref">
    </input-text-file>
  </div>
</template>

<script>
  // @flow
  import NumericInput from '@/components/NumericInput';
  import ActionButton from '@/components/ActionButton';
  import EditableDatalist from '@/components/EditableDatalist';
  import InputTextFile from '@/components/InputTextFile';
  import { HELP_VXC as HELP, MODEL_VXC as MODEL } from '@/store';
  import { mapState, mapGetters, mapMutations, mapActions } from 'vuex';

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
      };
    },
    computed: {
      ...mapGetters(HELP.MODULE, [
        HELP.GET.SHOW_HELP,
      ]),
      ...mapState(MODEL.MODULE, [
        'curModelName',
        'numberOfInitialSamples',
        'numberOfDiscreteValues',
      ]),
      ...mapGetters(MODEL.MODULE, [
        MODEL.GET.MODEL_NAMES,
        MODEL.GET.CUR_MODEL_DTO,
        MODEL.GET.CUR_QUERIES,
        MODEL.GET.IS_QUERY_ACTIVE,
      ]),
    },
    methods: {
      ...mapActions(MODEL.MODULE, [
        MODEL.ACTION.INITIALIZE,
        MODEL.ACTION.LOAD_MODELS_FROM_DISK,
        MODEL.ACTION.SAVE_CURRENT_MODEL_TO_DISK,
        MODEL.ACTION.CHANGE_CURRENT_MODEL,
        MODEL.ACTION.RUN_QUERY,
      ]),
      ...mapMutations(MODEL.MODULE, [
        MODEL.SET.CUR_QUERY,
        MODEL.SET.CUR_MODEL_QUERIES,
        MODEL.SET.NUMBER_OF_INITIAL_SAMPLES,
        MODEL.SET.NUMBER_OF_DISCRETE_VALUES,
        MODEL.SET.CLEAR_QUERY_RESULTS,
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
        this[MODEL.SET.CUR_MODEL_QUERIES](curQueries);
      },
      currentQueryChanged(curQuery: string) {
        this[MODEL.SET.CUR_QUERY](curQuery);
      },
      modelSelectionChanged(modelIx: number) {
        const modelOption = this.modelOptions[modelIx];
        const modelName: string = modelOption.text;
        this[MODEL.ACTION.CHANGE_CURRENT_MODEL](modelName);
      },
      submitQuery() {
        const query = this.$refs.queryOption_ref.getCurrentOption();
        if (!query) {
          return;
        }
        this.$refs.queryOption_ref.showList(false);
        this.$refs.queryOption_ref.addCurrentEntry();
        this[MODEL.ACTION.RUN_QUERY]();
      },
    },
    watch: {
      curModelName(curName: string) {
        const ix = this.modelOptions.findIndex(option => option.text === curName);
        if (ix !== -1) {
          this.modelOptionSelected = ix;
        }
      },
      modelNames(names: string[]) {
        this.initModelNames(names);
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
