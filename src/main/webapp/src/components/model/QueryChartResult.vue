<template>
  <!--
@TODO change below to @click.stop="showChart = !showChart" when we are ready to display explanations component
-->
  <div>
    <transition name="fade" v-on:after-leave="showIcon = true">
      <div v-show="showChart" class="top-level-container">
        <div>
          <img @click.stop="showChart = true" :src="imageData">
          <query-graph-controls></query-graph-controls>
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
  import { mapGetters } from 'vuex';
  import { MODEL_VXC as MODEL } from '@/store';
  import QueryGraphControls from './QueryGraphControls';
  import type { ExpressionResultDto } from './types';

  export default {
    name: 'QueryChartResult',
    components: {
      QueryGraphControls,
    },
    data() {
      return {
        imageData: null,
        showChart: false,
        showIcon: false,
      };
    },
    methods: {
      initialize() {
        if (!this[MODEL.GET.CUR_RESULT]) {
          return;
        }
        const expressionResultDto: ExpressionResultDto = this[MODEL.GET.CUR_RESULT];
        const { graphQueryResultDto } = expressionResultDto;
        if (graphQueryResultDto) {
          this.imageData = graphQueryResultDto.imageData;
        } else {
          this.imageData = null;
        }
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
