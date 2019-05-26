<template>
  <span
    v-show="showScrollUpButton"
    class="button-up-style"
  >
    <b-btn href="#router"><i class="fas fa-arrow-up fa-2x" /></b-btn>
  </span>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';

  @Component
  export default class ScrollToTopButton extends Vue {
    scrollTop = 0;

    get showScrollUpButton(): boolean {
      return this.scrollTop > 8;
    }

    onScrollEvent(event: Event) {
      if (event.target) {
        this.scrollTop = (event.target as Element).scrollTop;
      }
    }
  }
</script>

<style lang="scss" scoped>
  @import '~@/styles/_variables.scss';

  .button-up-style {
    z-index: $z-index-panels;
    position: fixed;
    bottom: .3em;
    padding-left: 2px;
    padding-right: 2px;
    right: .2em;
    opacity: .15;
  }

  .button-up-style:hover {
    opacity: .8;
  }
</style>
