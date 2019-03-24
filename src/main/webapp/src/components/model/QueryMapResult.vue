<template>
  <!--
  @TODO change below to  @closeMap="showMap = false" when we are ready to display explanations component
  -->
  <div>
    <transition name="fade" v-on:after-leave="showIcon = true">
      <div v-show="showMap" class="top-level-container">
        <ol-map
            ref="map_ref"
            :heightOffset="mapHeightOffset "
          @closeMap="showMap = true"
          :mapRegionNameToValue="mapRegionNameToValue">
        </ol-map>
        <query-graph-controls v-if="curResult" ref="queryGraphControls_ref"></query-graph-controls>
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
  import { mapGetters } from 'vuex';
  import { MODEL_VXC as MODEL } from '@/store';
  import OlMap from '@/components/openlayers/OlMap';
  import QueryGraphControls from './QueryGraphControls';
  import type { ExpressionResultDto } from './types';

  const pageBannerHeight = 74;

  export default {
    name: 'QueryMapResult',
    components: {
      OlMap,
      QueryGraphControls,
    },
    data() {
      return {
        mapRegionNameToValue: null,
        showMap: false,
        showIcon: false,
        mapHeightOffset: pageBannerHeight,
      };
    },
    methods: {
      initialize() {
        if (!this[MODEL.GET.CUR_RESULT]) {
          this.mapRegionNameToValue = null;
          return;
        }
        const expressionResultDto: ExpressionResultDto = this[MODEL.GET.CUR_RESULT];
        const { graphQueryResultDto } = expressionResultDto;
        if (graphQueryResultDto) {
          if (graphQueryResultDto.mapRegionNameToValue) {
            this.mapRegionNameToValue = graphQueryResultDto.mapRegionNameToValue;
          }
        } else {
          this.mapRegionNameToValue = null;
        }
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
          // Set the mapHeightOffset to the height of the queryGraphControls
          const qgc = this.$refs.queryGraphControls_ref.$el;
          this.mapHeightOffset = qgc.clientHeight + pageBannerHeight;
          this.$nextTick(() => {
            this.$refs.map_ref.updateMapSize();
          });
        });
      },
    },
    computed: {
      ...mapGetters(MODEL.MODULE, [
        MODEL.GET.CUR_RESULT,
      ]),
    },
    watch: {
      [MODEL.GET.CUR_RESULT]() {
        this.initialize();
        this.updateMapHeightOffset(true);
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
      this.initialize();
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
