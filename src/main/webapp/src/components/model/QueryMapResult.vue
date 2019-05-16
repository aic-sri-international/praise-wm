<template>
  <!--
  @TODO change below to  @closeMap="showMap = false"
  when we are ready to display explanations component
  -->
  <div>
    <transition
      name="fade"
      @after-leave="showIcon = true"
    >
      <div
        v-show="showMap"
        class="top-level-container"
      >
        <ol-map
          ref="mapRef"
          :height-offset="mapHeightOffset "
          :map-region-name-to-value="mapRegionNameToValue"
          @closeMap="showMap = true"
        />
        <query-graph-controls
          v-if="mapRegionNameToValue && curResult"
          ref="queryGraphControlsRef"
        />
      </div>
    </transition>
    <div @click.stop="showIcon = !showIcon">
      <transition
        name="fade"
        @after-leave="showMap = true"
      >
        <span v-show="showIcon">
          <i class="fas fa-map mapIcon" />
        </span>
      </transition>
    </div>
  </div>
</template>

<script lang="ts">
  import { Component, Vue, Watch } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import { MODEL_VXC } from '@/store';
  import { ExpressionResultDto } from '@/store/model/types';
  import OlMap from '@/components/openlayers/OlMap.vue';
  import QueryGraphControls from './QueryGraphControls.vue';
  import { OlMapInterface } from '@/components/openlayers/OlMap.types';

  const pageBannerHeight = 74;

  const modelModule = namespace(MODEL_VXC.MODULE);

  @Component({
    components: {
      OlMap,
      QueryGraphControls,
    },
  })
  export default class QueryMapResult extends Vue {
    @modelModule.Getter
    curResult!: ExpressionResultDto | null;

    $refs!: {
      mapRef: OlMapInterface,
      queryGraphControlsRef: Vue,
    };

    mapRegionNameToValue: { [key: string]: number } | null = null;

    showMap = false;

    showIcon = false;

    mapHeightOffset = pageBannerHeight;


    @Watch('curResult')
    onCurResult() {
      this.initialize();
      this.updateMapHeightOffset(true);
    }

    @Watch('showMap')
    onShowMap(newValue: boolean) {
      if (newValue) {
        this.$nextTick(() => {
          this.$refs.mapRef.updateMapSize();
        });
      }
    }


    mounted() {
      this.showMap = true;
      this.updateMapHeightOffset(false);
    }

    created() {
      this.initialize();
    }

    initialize() {
      if (!this.curResult) {
        this.mapRegionNameToValue = null;
        return;
      }
      const expressionResultDto: ExpressionResultDto = this.curResult;
      const { graphQueryResultDto } = expressionResultDto;
      if (graphQueryResultDto) {
        if (graphQueryResultDto.mapRegionNameToValue) {
          this.mapRegionNameToValue = graphQueryResultDto.mapRegionNameToValue;
        }
      } else {
        this.mapRegionNameToValue = null;
      }
    }

      updateMapHeightOffset(alwaysResize: boolean) {
        this.$nextTick(() => {
          if (!this.$refs.queryGraphControlsRef) {
            this.mapHeightOffset = pageBannerHeight;
            if (alwaysResize) {
              this.$nextTick(() => {
                this.$refs.mapRef.updateMapSize();
              });
            }
            return;
          }
          // Set the mapHeightOffset to the height of the queryGraphControls
          const qgc = this.$refs.queryGraphControlsRef.$el;
          this.mapHeightOffset = qgc.clientHeight + pageBannerHeight;
          this.$nextTick(() => {
            this.$refs.mapRef.updateMapSize();
          });
        });
      }
  }
</script>

<style lang="scss" scoped>
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
