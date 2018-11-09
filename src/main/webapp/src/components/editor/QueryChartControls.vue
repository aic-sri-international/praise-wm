<template>
  <div>
    <div v-for="graphVariableSet in graphVariableSets" >
      <graph-variable-set-slider
          class="horizontal-slider"
        :direction="direction"
        :graphVariableSet="graphVariableSet"
      ></graph-variable-set-slider>
    </div>
  </div>
</template>

<script>
  // @flow
  import GraphVariableSetSlider from './GraphVariableSetSlider';

  export default {
    name: 'QueryChartControls',
    components: {
      GraphVariableSetSlider,
    },
    props: {
      direction: {
        type: String,
      },
      graphVariableSets: {
        type: Array,
      },
    },
  };
</script>

<style scoped>
  .horizontal-slider {
    margin-left: 44px;
    margin-top: 34px;
  }

</style>