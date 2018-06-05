<template>
  <div v-on-click-outside="onClickOutside">
    <b-container class="container effect8">
      <div class="d-flex justify-content-start pt-2">
      <h4>System Status</h4>
      </div>
      <hr />
      <div v-for="item in items">
        <div class="d-flex justify-content-start">
          <div>{{item.name}}</div>
          <div class="ml-auto">
            <i :class="item.classes"></i>
          </div>
        </div>
        <hr />
      </div>
    </b-container>
  </div>
</template>

<script>
  // @flow

  import { mapGetters } from 'vuex';

  import {
    SYSTEM_STATUS_VXC as SS,
    vxcFp,
  } from '@/store';

  type Item = {
    name: string,
    classes: ?string,
  };


  export default {
    name: 'systemStatuses',
    methods: {
      onClickOutside() {
        this.$emit('close');
      },
    },
    computed: {
      ...mapGetters(SS.MODULE, [
        SS.GET.DATABASE_CLASS,
      ]),
      items() : Item[] {
        return [
          { name: 'Database', classes: this[SS.GET.DATABASE_CLASS] },
        ];
      },
    },
    created() {
      this.$store.commit(vxcFp(SS, SS.SET.UI_IS_OPEN), true);
    },
    beforeDestroy() {
      this.$store.commit(vxcFp(SS, SS.SET.UI_IS_OPEN), false);
    },
  };
</script>

<style scoped>
  .container {
    background-color: white;
  }
  .effect8
  {
    position:relative;
    box-shadow:0 1px 4px rgba(0, 0, 0, 0.3), 0 0 40px rgba(0, 0, 0, 0.1) inset;
  }
  .effect8:before, .effect8:after
  {
    content:"";
    position:absolute;
    z-index:-1;
    box-shadow:0 0 20px rgba(0,0,0,0.8);
    top:10px;
    bottom:10px;
    left:0;
    right:0;
    border-radius:100px / 10px;
  }
  .effect8:after
  {
    right:10px;
    left:auto;
    transform:skew(8deg) rotate(3deg);
  }
</style>