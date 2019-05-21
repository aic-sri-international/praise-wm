<template>
  <div class="top-level-container">
    <div
      id="modelEditorViewLeftColId"
      class="left-column"
    >
      <model-editor-view class="model-editor-view" />
      <div class="modelControlsContainer">
        <model-controls-panel />
        <query-results
          v-if="queryResultsIx !== -1"
          class="query-results mt-2 mb-2"
        />
      </div>
      <spinner
        :show="isQueryActive"
        hover-text="Click here to abort the query"
        @click="setAbortQuery(true)"
      />
    </div>
    <div
      id="segModelEditorViewRightColId"
      class="right-column"
    >
      <query-chart-result
        v-if="displayChart"
        class="query-chart"
      />
      <query-map-result v-if="!displayChart" />

      <!-- @TODO placeholder for when hogm solver returns an explanation tree -->
      <!--<explanations :explanation-tree="explanationTree" id="explanations"></explanations>-->
    </div>
    <b-popover
      :show="showHelp"
      target="segModelEditorViewRightColId"
      triggers=""
    >
      Visualization of query results.
    </b-popover>
  </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import Split from 'split.js';
  import Spinner from '@/components/Spinner.vue';
  import ModelEditorView from './editor/ModelEditorView.vue';
  import ModelControlsPanel from './ModelControlsPanel.vue';
  import QueryResults from './QueryResults.vue';
  import QueryChartResult from './QueryChartResult.vue';
  import QueryMapResult from './QueryMapResult.vue';
  import { HELP_MODULE_NAME } from '@/store/help/constants';
  import { MODEL_MODULE_NAME } from '@/store/model/constants';
  import { VuexHelpState } from '@/store/help/types';
  import { VuexModelState } from '@/store/model/types';

  const helpModule = namespace(HELP_MODULE_NAME);
  const modelModule = namespace(MODEL_MODULE_NAME);

  @Component({
    components: {
      ModelEditorView,
      ModelControlsPanel,
      QueryResults,
      QueryChartResult,
      QueryMapResult,
      Spinner,
    },
  })
  export default class ModelView extends Vue {
    splitter?: Split.Instance = undefined;

    @helpModule.State showHelp!: VuexHelpState['showHelp'];

    @modelModule.State queryResultsIx!: VuexModelState['queryResultsIx'];

    @modelModule.Getter isQueryActive!: boolean;

    @modelModule.Getter displayChart!: boolean;

    @modelModule.Action initialize!: () => Promise<any>;

    @modelModule.Mutation setAbortQuery!: (abortFlag: boolean) => void;

    async created() {
      await this.initialize();
    }

    mounted() {
      const sendResizeEvent = () => {
        // The Map component needs to get a window's resize event so that it can recompute
        // map coordinates and UI display.
        window.dispatchEvent(new Event('resize'));
      };
      if (this.splitter) {
        this.splitter.destroy();
      }
      this.splitter = Split(['#modelEditorViewLeftColId', '#segModelEditorViewRightColId'], {
        sizes: [60, 40],
        onDrag() {
          sendResizeEvent();
        },
      });
      sendResizeEvent();
    }

    beforeDestroy() {
      if (this.splitter) {
        this.splitter.destroy();
      }
    }
  }
</script>

<style lang="scss" scoped>
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
