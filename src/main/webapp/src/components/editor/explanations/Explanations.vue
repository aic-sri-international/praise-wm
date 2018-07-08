<template>
  <div>
      <div class="title">
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
</style>