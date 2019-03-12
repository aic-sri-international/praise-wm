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
  import QueryGraphControls from './QueryGraphControls';
  import type {
    GraphQueryResultDto,
    GraphRequestDto,
    GraphRequestResultDto,
    GraphQueryVariableResults,
  } from './types';
  import { fetchGraph } from './dataSourceProxy';

  export default {
    name: 'QueryChartResult',
    components: {
      QueryGraphControls,
    },
    props: {
      graphQueryResult: {
        type: Object,
      },
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
      initialize() {
        // eslint-disable-next-line prefer-destructuring
        const graphQueryResult: GraphQueryResultDto = this.graphQueryResult;
        if (graphQueryResult) {
          const gqvr: GraphQueryVariableResults = {
            xmVariables: [...graphQueryResult.xmVariables],
            graphVariableSets: [...graphQueryResult.graphVariableSets],
          };

          this.imageData = graphQueryResult.imageData;
          this.graphQueryVariableResults = gqvr;
        } else {
          this.imageData = null;
          this.graphQueryVariableResults = null;
        }
      },
      async queryForNewGraph() {
        const request: GraphRequestDto = this.$refs.queryGraphControls_ref.buildGraphRequest();
        try {
          this.$emit('querySent');
          const graph: GraphRequestResultDto = await fetchGraph(request);
          this.imageData = graph.imageData;
        } catch (err) {
        // errors already logged/displayed
        } finally {
          this.$emit('queryReturned');
        }
      },
      onControlChanged() {
        if (!this.debounced$) {
          this.debounced$ = debounce(this.queryForNewGraph, 250, { trailing: true });
        }
        this.debounced$();
      },
    },
    watch: {
      graphQueryResult() {
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