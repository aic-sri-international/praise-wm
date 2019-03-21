<template>
  <!--
  @TODO change below to  @closeMap="showMap = false" when we are ready to display explanations component
  -->
  <div>
    <transition name="fade" v-on:after-leave="showIcon = true">
      <div v-show="showMap" class="top-level-container">
        <ol-map
            ref="map_ref"
            :heightOffset="mapHeightOffset"
          @closeMap="showMap = true"
          :mapRegionNameToValue="mapRegionNameToValue">
        </ol-map>
        <query-graph-controls
            v-if="graphQueryVariableResults"
            ref="queryGraphControls_ref"
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
  import { mapState, mapMutations } from 'vuex';
  import { MODEL_VXC as MODEL } from '@/store';
  import OlMap from '@/components/openlayers/OlMap';
  import QueryGraphControls from './QueryGraphControls';
  import type {
    GraphQueryResultDto,
    GraphRequestDto,
    GraphQueryVariableResults,
    GraphRequestResultDto,
    ExpressionResultDto,
  } from './types';

  import { fetchGraph } from './dataSourceProxy';

  const pageBannerHeight = 74;

  export default {
    name: 'QueryMapResult',
    components: {
      OlMap,
      QueryGraphControls,
    },
    data() {
      return {
        graphQueryVariableResults: null,
        mapRegionNameToValue: null,
        showMap: false,
        showIcon: false,
        mapHeightOffset: pageBannerHeight,
      };
    },
    methods: {
      ...mapMutations(MODEL.MODULE, [
        MODEL.SET.IS_QUERY_ACTIVE,
      ]),
      getCurrentGraphQueryResultDto() : ?GraphQueryResultDto {
        const expressionResultDto: ExpressionResultDto = this.queryResults[this.queryResultsIx];
        return expressionResultDto.graphQueryResultDto;
      },
      initialize() {
        const graphQueryResultDto: ?GraphQueryResultDto = this.getCurrentGraphQueryResultDto();
        if (graphQueryResultDto) {
          const gqvr: GraphQueryVariableResults = {
            xmVariables: [...graphQueryResultDto.xmVariables],
            graphVariableSets: [...graphQueryResultDto.graphVariableSets],
          };
          if (graphQueryResultDto.mapRegionNameToValue) {
            this.mapRegionNameToValue = graphQueryResultDto.mapRegionNameToValue;
          }
          this.graphQueryVariableResults = gqvr;
        } else {
          this.graphQueryVariableResults = null;
          this.mapRegionNameToValue = null;
        }
      },
      async queryForNewMapData() {
        let xmVariableChanged = false;
        const expressionResultDto: ExpressionResultDto = this.queryResults[this.queryResultsIx];
        const { graphQueryResultDto } = expressionResultDto;
        if (!graphQueryResultDto) {
          throw Error('graphQueryResultDto is not set');
        }
        const request: GraphRequestDto = this.$refs.queryGraphControls_ref.buildGraphRequest();
        try {
          this[MODEL.SET.IS_QUERY_ACTIVE](true);
          const graph: GraphRequestResultDto = await fetchGraph(request);
          this.mapRegionNameToValue = graph.mapRegionNameToValue;
          if (request.xmVariable !== graphQueryResultDto.xmVariables[0]) {
            xmVariableChanged = true;
            // this.graphQueryResult.xmVariables[0] = request.xmVariable;
          }
        } catch (err) {
          // errors already logged/displayed
        } finally {
          this[MODEL.SET.IS_QUERY_ACTIVE](false);
        }
        if (xmVariableChanged) {
          this.initialize();
        }
      },
      onControlChanged() {
        if (!this.debounced$) {
          this.debounced$ = debounce(this.queryForNewMapData, 250, { trailing: true });
        }
        this.debounced$();
      },
      updateMapHeightOffset(alwaysResize: boolean) {
        this.$nextTick(() => {
          if (!this.$refs.queryGraphControls_ref) {
            this.mapHeightOffset = pageBannerHeight;
            if (alwaysResize) {
              this.$nextTick(() => {
                this.$refs.map_ref.updateMapSize();
              });
            }
            return;
          }
          // We do not yet have the complete client height for the queryGraphControls
          // component. Set the mapHeightOffset to the partial height of the queryGraphControls
          // component to trigger a recomputation.
          const qgc = this.$refs.queryGraphControls_ref.$el;
          this.mapHeightOffset = qgc.clientHeight + pageBannerHeight;
          setTimeout(() => {
            // Wait for clientHeight to be recomputed, them set the offset to what should be
            // the complete clientHeight.
            this.mapHeightOffset = qgc.clientHeight + pageBannerHeight;
            this.$nextTick(() => {
              this.$refs.map_ref.updateMapSize();
            });
          }, 68);
        });
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
        if (this.queryResultsIx > -1) {
          this.initialize();
          this.updateMapHeightOffset(true);
        }
      },
      showMap(newValue: boolean) {
        if (newValue) {
          this.$nextTick(() => {
            this.$refs.map_ref.updateMapSize();
          });
        }
      },
    },
    mounted() {
      this.showMap = true;
      this.updateMapHeightOffset(false);
    },
    created() {
      if (this.queryResultsIx > -1) {
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
