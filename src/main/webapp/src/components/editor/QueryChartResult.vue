<template>
  <div>
    <transition name="fade" v-on:after-leave="showIcon = true">
      <div v-show="showChart" class="top-level-container">
        <div>
          <img @click.stop="showChart = !showChart" :src="imageData">
          <query-chart-controls
              ref="queryChartControls_ref"
              @controlChanged="onControlChanged"
              :graph-query-variable-results="graphQueryVariableResults">
          </query-chart-controls>
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
  import QueryChartControls from './QueryChartControls';
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
      QueryChartControls,
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
        const gqvr: GraphQueryVariableResults = {
          xmVariables: [...graphQueryResult.xmVariables],
          graphVariableSets: [...graphQueryResult.graphVariableSets],
        };

        this.imageData = graphQueryResult.imageData;
        this.graphQueryVariableResults = gqvr;
      },
      async fetchGraph(request: GraphRequestDto): Promise<GraphRequestResultDto> {
        try {
          return await fetchGraph(request);
        } catch (err) {
          // errors already logged/displayed
          return { imageData: '' };
        }
      },
      async queryForNewGraph() {
        const request: GraphRequestDto = this.$refs.queryChartControls_ref.buildGraphRequest();
        const graph: GraphRequestResultDto = await fetchGraph(request);
        this.imageData = graph.imageData;
      },
      onControlChanged() {
        if (!this.debounced$) {
          this.debounced$ = debounce(this.queryForNewGraph, 2000, { trailing: true });
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
    },
    mounted() {
      this.$nextTick(() => { this.showChart = true; });
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