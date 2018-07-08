<template>
  <li v-if="model != null">
    <div
        @click="toggle">
      <span v-if="isFolder">[{{ open ? '-' : '+' }}]</span>
      {{ model.header }}
    </div>
    <ul v-show="open" v-if="isFolder">
      <item
          class="item"
          v-for="(model, index) in model.subExplanations"
          :key="index"
          :model="model">
      </item>
    </ul>
    {{ model.footer }}
  </li>
</template>

<script>
  export default {
    name: 'Item',
    props: {
      model: Object,
    },
    data() {
      return {
        open: false,
      };
    },
    computed: {
      isFolder() {
        return this.model.subExplanations &&
            this.model.subExplanations.length;
      },
    },
    methods: {
      toggle() {
        if (this.isFolder) {
          this.open = !this.open;
        }
      },
    },
  };
</script>

<style scoped>
  body {
    font-family: Menlo, Consolas, monospace;
    color: #444;
  }
  .item {
    cursor: pointer;
  }
  .bold {
    font-weight: bold;
  }
  ul {
    padding-left: .5em;
    line-height: 1.5em;
    list-style-type: none;
  }
  li {
    list-style: none;
    margin: 0;
    padding: 0;
  }
</style>