<template>
  <b-btn
    :title="title"
    size="sm"
    variant="outline-secondary"
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
  import { Component, Prop, Vue } from 'vue-property-decorator';

  @Component
  export default class NewObjectButton extends Vue {
    @Prop(String) readonly title?: string;
  }
</script>
