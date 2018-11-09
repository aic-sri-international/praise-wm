<template>
  <div>
    <div @click.stop="showMap = !showMap">
      <transition name="fade" v-on:after-leave="showIcon = true">
        <img v-if="showMap" src="../../../static/images/South_Sudan_food_security.jpg">
      </transition>
    </div>
    <div @click.stop="showIcon = !showIcon">
      <transition name="fade" v-on:after-leave="showMap = true">
        <span v-if="showIcon" >
          <i class="fas fa-map mapIcon"></i>
        </span>
      </transition>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'MapImage',
    data() {
      return {
        showMap: true,
        showIcon: false,
      };
    },
  };
</script>

<style scoped>
  img {
    max-width: 400px;
    cursor: pointer;
  }
  .mapIcon {
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