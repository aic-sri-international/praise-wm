<template>
  <!--
@TODO change below to @click.stop="showChart = !showChart" when we are ready to display explanations component
-->
  <div>
    <transition name="fade" v-on:after-leave="showIcon = true">
      <div v-show="showChart" class="top-level-container">
        <div>
          <img @click.stop="showChart = true" :src="imageData">
          <query-graph-controls
              ref="queryGraphControls_ref"
              @controlChanged="onControlChanged"
              :graph-query-variable-results="graphQueryVariableResults">
          </query-graph-controls>
        </div>
      </div>
    </transition>
    <div @click.stop="showIcon = !showIcon">
      <transition name="fade" v-on:after-leave="showChart = true">
        <span v-show="showIcon">
          <i class="fas fa-chart-line chartIcon"></i>
        </span>
      </transition>
    </div>
  </div>
</template>

<script>
  // @flow
  import debounce from 'lodash/debounce';
  import { mapState, mapMutations } from 'vuex';
  import { MODEL_VXC as MODEL } from '@/store';
  import QueryGraphControls from './QueryGraphControls';
  import type {
    GraphRequestDto,
    GraphRequestResultDto,
    GraphQueryVariableResults,
    ExpressionResultDto,
  } from './types';
  import { fetchGraph } from './dataSourceProxy';

  export default {
    name: 'QueryChartResult',
    components: {
      QueryGraphControls,
    },
    data() {
      return {
        imageData: null,
        graphQueryVariableResults: null,
        showChart: false,
        showIcon: false,
      };
    },
    methods: {
      ...mapMutations(MODEL.MODULE, [
        MODEL.SET.IS_QUERY_ACTIVE,
      ]),
      initialize() {
        if (this.queryResultsIx < 0) {
          return;
        }
        const expressionResultDto: ExpressionResultDto = this.queryResults[this.queryResultsIx];
        const { graphQueryResultDto } = expressionResultDto;
        if (graphQueryResultDto) {
          const gqvr: GraphQueryVariableResults = {
            xmVariables: [...graphQueryResultDto.xmVariables],
            graphVariableSets: [...graphQueryResultDto.graphVariableSets],
          };

          this.imageData = graphQueryResultDto.imageData;
          this.graphQueryVariableResults = gqvr;
        } else {
          this.imageData = null;
          this.graphQueryVariableResults = null;
        }
      },
      async queryForNewGraph() {
        const request: GraphRequestDto = this.$refs.queryGraphControls_ref.buildGraphRequest();
        try {
          this[MODEL.SET.IS_QUERY_ACTIVE](true);
          const graph: GraphRequestResultDto = await fetchGraph(request);
          this.imageData = graph.imageData;
        } catch (err) {
        // errors already logged/displayed
        } finally {
          this[MODEL.SET.IS_QUERY_ACTIVE](false);
        }
      },
      onControlChanged() {
        if (!this.debounced$) {
          this.debounced$ = debounce(this.queryForNewGraph, 250, { trailing: true });
        }
        this.debounced$();
      },
    },
    computed: {
      ...mapState(MODEL.MODULE, [
        'queryResults',
        'queryResultsIx',
      ]),
    },
    watch: {
      queryResultsIx() {
        if (this.debounced$) {
          this.debounced$.cancel();
        }
        this.initialize();
      },
      showChart(newValue: boolean) {
        if (newValue) {
          this.$nextTick(() => {
            // This is needed to have any query control sliders correctly position
            // themselves if a query result is returned while the controls are not visible
            window.dispatchEvent(new Event('resize'));
          });
        }
      },
    },
    mounted() {
      this.showChart = true;
    },
    created() {
      this.initialize();
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

  img {
    max-width: 400px;
    cursor: pointer;
  }

  .chartIcon {
    color: darkorange;
    cursor: pointer;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
