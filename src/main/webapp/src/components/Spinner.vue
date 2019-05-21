<template>
  <div
    v-if="show"
    id="spinnerId"
    class="spinner"
    @click.stop="$emit('click')"
  >
    <span class="fa-layers fa-fw">
      <i
        :data-fa-transform="transform"
        :style="`color: ${color}`"
        class="fas fa-spinner fa-pulse"
      />
    </span>
    <b-popover
      v-if="hoverText"
      target="spinnerId"
      triggers="hover"
    >
      <span>{{ hoverText }}</span>
    </b-popover>
  </div>
</template>

<script lang="ts">
  import { Component, Prop, Vue } from 'vue-property-decorator';

  @Component
  export default class Spinner extends Vue {
    @Prop({ default: 'grow-80' }) readonly transform!: string;

    @Prop({ default: 'green' }) readonly color!: string;

    @Prop(String) readonly hoverText?: string;

    @Prop({ default: false }) readonly show!: boolean;
  }
</script>

<style lang="scss" scoped>
  .spinner {
    position: fixed;
    top: 50%;
    left: 50%;
    cursor: pointer;
    z-index: 1030;
  }
</style>
