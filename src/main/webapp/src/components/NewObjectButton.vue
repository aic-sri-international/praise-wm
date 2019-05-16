<template>
  <b-btn
    size="sm"
    variant="outline-secondary"
    :title="title"
    @click.stop="$emit('clicked')"
  >
    New
    <i
      class="fas fa-plus-square"
      style="color: green"
    />
  </b-btn>
</template>

<script lang="ts">
  import {
    Vue, Component, Prop,
  } from 'vue-property-decorator';

  @Component
  export default class NewObjectButton extends Vue {
    @Prop(String) readonly title?: string;
  }
</script>
