<template>
  <!--
@TODO change below to @click.stop="showChart = !showChart"
when we are ready to display explanations component
-->
  <div>
    <transition
      name="fade"
      @after-leave="showIcon = true"
    >
      <div
        v-show="showChart"
        class="top-level-container"
      >
        <div>
          <img
            :src="imageData"
            @click.stop="showChart = true"
          >
          <query-graph-controls />
        </div>
      </div>
    </transition>
    <div @click.stop="showIcon = !showIcon">
      <transition
        name="fade"
        @after-leave="showChart = true"
      >
        <span v-show="showIcon">
          <i class="fas fa-chart-line chartIcon" />
        </span>
      </transition>
    </div>
  </div>
</template>

<script lang="ts">
  import { Component, Vue, Watch } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import { ExpressionResultDto } from '@/store/model/types';
  import QueryGraphControls from './QueryGraphControls.vue';
  import { MODEL_MODULE_NAME } from '@/store/model/constants';

  const modelModule = namespace(MODEL_MODULE_NAME);

  @Component({
    components: {
      QueryGraphControls,
    },
  })
  export default class QueryChartResult extends Vue {
    imageData: string | null = null;

    showChart = false;

    showIcon = false;

    @modelModule.Getter
    curResult!: ExpressionResultDto | null;

    @Watch('curResult')
    onCurResult() {
      this.initialize();
    }

    @Watch('showChart')
    onShowChart(newValue: boolean) {
      if (newValue) {
        this.$nextTick(() => {
          // This is needed to have any query control sliders correctly position
          // themselves if a query result is returned while the controls are not visible
          window.dispatchEvent(new Event('resize'));
        });
      }
    }

    mounted() {
      this.showChart = true;
    }

    created() {
      this.initialize();
    }

    initialize() {
      if (!this.curResult) {
        return;
      }
      const expressionResultDto: ExpressionResultDto = this.curResult;
      const { graphQueryResultDto } = expressionResultDto;
      if (graphQueryResultDto && graphQueryResultDto.imageData) {
        this.imageData = graphQueryResultDto.imageData;
      } else {
        this.imageData = null;
      }
    }
  }
</script>

<style lang="scss" scoped>
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
