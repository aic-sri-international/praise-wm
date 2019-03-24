<template>
  <div class="top-level-container">
    <div class="left-column" id="modelEditorViewLeftColId">
      <model-editor-view class="model-editor-view"></model-editor-view>
      <div class="modelControlsContainer">
        <model-controls-panel></model-controls-panel>
        <query-results class="query-results mt-2 mb-2" v-if="queryResultsIx !== -1"></query-results>
      </div>
      <spinner :show="isQueryActive" @click="interruptQueries()"></spinner>
    </div>
    <div class="right-column" id="segModelEditorViewRightColId">
      <!--@TODO update when the HOGM solver can return map related query results-->
     <query-chart-result class="query-chart" v-if="displayChart"></query-chart-result>
     <query-map-result v-if="!displayChart"></query-map-result>

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
  import Spinner from '@/components/Spinner';
  import { HELP_VXC as HELP, MODEL_VXC as MODEL } from '@/store';
  import { mapState, mapGetters, mapActions } from 'vuex';
  import ModelEditorView from './editor/ModelEditorView';
  import Editor from './editor/Editor';
  import Explanations from './explanations/Explanations';
  import SegmentedModelEditor from './editor/ModelEditor';
  import ModelControlsPanel from './ModelControlsPanel';
  import QueryResults from './QueryResults';
  import QueryChartResult from './QueryChartResult';
  import QueryMapResult from './QueryMapResult';
  import MapImage from './MapImage';
  import { interruptSolver } from './dataSourceProxy';

  export default {
    name: 'ModelView',
    components: {
      ModelEditorView,
      Editor,
      SegmentedModelEditor,
      ModelControlsPanel,
      QueryResults,
      QueryChartResult,
      MapImage,
      Explanations,
      QueryMapResult,
      Spinner,
    },
    computed: {
      ...mapState(MODEL.MODULE, [
        'queryResultsIx',
      ]),
      ...mapGetters(HELP.MODULE, [
        HELP.GET.SHOW_HELP,
      ]),
      ...mapGetters(MODEL.MODULE, [
        MODEL.GET.IS_QUERY_ACTIVE,
        MODEL.GET.DISPLAY_CHART,
      ]),
    },
    methods: {
      ...mapActions(MODEL.MODULE, [
        MODEL.ACTION.INITIALIZE,
      ]),
      interruptQueries() {
        interruptSolver();
      },
    },
    async created() {
      this[MODEL.ACTION.INITIALIZE]();
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
      this.splitter$ = Split(['#modelEditorViewLeftColId', '#segModelEditorViewRightColId'], {
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
