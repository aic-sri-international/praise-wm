<template>
  <div class="explanations" :style="titleStyle">
      <div class="title" @click.stop="wrap = !wrap">
        Explanation
      </div>
    <ul>
      <item
          class="item"
          :model="explanationTree">
      </item>
    </ul>
  </div>
</template>

<script>
  import Item from './Item';

  export default {
    name: 'Explanations',
    components: {
      Item,
    },
    props: {
      explanationTree: {
        type: Object,
      },
    },
    data() {
      return {
        wrap: false,
      };
    },
    computed: {
      titleStyle() {
        return { whiteSpace: this.wrap ? 'normal' : 'nowrap' };
      },
    },
  };
</script>

<style scoped>
  .title {
    padding-bottom: 4px;
    font-weight: bold;
  }
  .item {
    cursor: pointer;
  }
  ul {
    padding: 0;
    margin: 0;
    line-height: 1.5em;
    list-style-type: none;
  }

  .explanations {
    border: thin double lightgrey;
    min-height: 20px;
    min-width: 20px;
    text-align: left;
    padding: 10px;
    position: relative;
    overflow: auto;
    cursor: pointer;
  }

</style>