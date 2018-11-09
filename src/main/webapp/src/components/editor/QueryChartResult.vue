<template>
  <div>
    <transition name="fade" v-on:after-leave="showIcon = true">
      <div v-if="showChart" class="top-level-container">
        <div>
          <img @click.stop="showChart = !showChart" :src="graphQueryResult.imageData">
          <query-chart-controls direction="horizontal"
                                :graph-variable-sets="horizontalVariableSets">
          </query-chart-controls>
        </div>
      </div>
    </transition>
    <div @click.stop="showIcon = !showIcon">
      <transition name="fade" v-on:after-leave="showChart = true">
        <span v-if="showIcon">
          <i class="fas fa-chart-line chartIcon"></i>
        </span>
      </transition>
    </div>
  </div>
</template>

<script>
  // @flow
  import QueryChartControls from './QueryChartControls';
  import type { GraphVariableSet, GraphQueryResultDto } from './types';

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
        showChart: false,
        showIcon: false,
      };
    },
    methods: {
      getVariableSets(isXm: boolean): GraphVariableSet[] {
        // eslint-disable-next-line prefer-destructuring
        const graphQueryResult: GraphQueryResultDto = this.graphQueryResult;
        const xmVariable: string = graphQueryResult.xmVariables[0];
        return graphQueryResult.graphVariableSets
          .filter(s => (isXm ? s.name === xmVariable : s.name !== xmVariable));
      },
    },
    computed: {
      horizontalVariableSets(): GraphVariableSet[] {
        return [...this.getVariableSets(true), ...this.getVariableSets(false)];
      },
    },
    mounted() {
      this.$nextTick(() => { this.showChart = true; });
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