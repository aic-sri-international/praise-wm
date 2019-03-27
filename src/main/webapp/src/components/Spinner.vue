<template>
  <div @click.stop="$emit('click')" class="spinner" id="spinnerId"
       v-if="show">
        <span class="fa-layers fa-fw">
           <i class="fas fa-spinner fa-pulse" :data-fa-transform="transform" :style="`color: ${color}`" ></i>
        </span>
    <b-popover v-if="hoverText" target="spinnerId" triggers="hover">
      <span>{{hoverText}}</span>
    </b-popover>
  </div>
</template>

<script>
  export default {
    name: 'Spinner',
    props: {
      transform: {
        type: String,
        default: 'grow-80',
        required: false,
      },
      color: {
        type: String,
        default: 'green',
      },
      hoverText: {
        type: String,
      },
      show: {
        type: Boolean,
      },
    },
  };
</script>

<style scoped>

  .spinner {
    position: fixed;
    top: 50%;
    left: 50%;
    cursor: pointer;
    z-index: 1030;
  }

</style>
