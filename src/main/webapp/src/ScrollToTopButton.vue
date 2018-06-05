<template>
    <span class="button-up-style" v-show="showScrollUpButton">
      <b-btn href="#router"><i class="fas fa-arrow-up fa-2x"></i></b-btn>
    </span>
</template>

<script>
  // @flow

  export default {
    name: 'ScrollToTopButton',
    data() {
      return {
        scrollTop: 0,
      };
    },
    methods: {
      onScrollEvent(event: Object) {
        this.scrollTop = event.target.scrollTop;
      },
    },
    computed: {
      showScrollUpButton() {
        return this.scrollTop > 8;
      },
    },
  };
</script>

<style scoped>
  .button-up-style {
    z-index: 1030;
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