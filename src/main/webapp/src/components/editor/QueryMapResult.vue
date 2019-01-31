<template>
  <!--
  @TODO change below to  @closeMap="showMap = false" when we are ready to display explanations component
  -->
  <div>
    <transition name="fade" v-on:after-leave="showIcon = true">
      <div v-show="showMap" class="top-level-container">
          <ol-map
            class="ol-map"
            @closeMap="showMap = true"
            :mapRegionNameToValue="mapRegionNameToValue">
          </ol-map>
          <query-graph-controls
              v-if="graphQueryVariableResults"
              ref="queryGraphControls_ref"
              :isMapControls="true"
              @controlChanged="onControlChanged"
              :graph-query-variable-results="graphQueryVariableResults">
          </query-graph-controls>
      </div>
    </transition>
    <div @click.stop="showIcon = !showIcon">
      <transition name="fade" v-on:after-leave="showMap = true">
        <span v-show="showIcon">
          <i class="fas fa-map mapIcon"></i>
        </span>
      </transition>
    </div>
  </div>
</template>

<script>
  // @flow
  import debounce from 'lodash/debounce';
  import OlMap from '@/components/openlayers/OlMap';
  import QueryGraphControls from './QueryGraphControls';
  import type {
    GraphQueryResultDto,
    GraphRequestDto,
    GraphQueryVariableResults,
    GraphRequestResultDto,
  } from './types';
  import { fetchGraph } from './dataSourceProxy';

  export default {
    name: 'QueryMapResult',
    components: {
      OlMap,
      QueryGraphControls,
    },
    props: {
      graphQueryResult: {
        type: Object,
      },
    },
    data() {
      return {
        graphQueryVariableResults: null,
        mapRegionNameToValue: null,
        showMap: false,
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

        this.graphQueryVariableResults = gqvr;

        if (this.graphQueryResult && this.graphQueryResult.mapRegionNameToValue) {
          this.mapRegionNameToValue = this.graphQueryResult.mapRegionNameToValue;
        }
      },
      async queryForNewMapData() {
        const request: GraphRequestDto = this.$refs.queryGraphControls_ref.buildGraphRequest();
        const graph: GraphRequestResultDto = await fetchGraph(request);
        this.mapRegionNameToValue = graph.mapRegionNameToValue;
      },
      onControlChanged() {
        if (!this.debounced$) {
          this.debounced$ = debounce(this.queryForNewMapData, 250, { trailing: true });
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
      showMap(newValue: boolean) {
        if (newValue) {
          // This is needed to have any query control sliders correctly position
          // themselves if a query result is returned while the controls are not visible
          this.$nextTick(() => {
            window.dispatchEvent(new Event('resize'));
          });
        }
      },
    },
    mounted() {
      this.showMap = true;
    },
    created() {
      if (this.graphQueryResult) {
        this.initialize();
      }
    },
  };
</script>

<style scoped>
  .top-level-container {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  .ol-map {
    height: 400px;
    width: 100%;
  }

  .mapIcon {
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