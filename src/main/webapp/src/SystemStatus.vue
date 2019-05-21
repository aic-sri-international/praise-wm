<template>
  <div v-on-click-outside="onClickOutside">
    <b-container class="container effect8">
      <div class="d-flex justify-content-start pt-2">
        <h4>System Status</h4>
      </div>
      <hr>
      <div
        v-for="item in items"
        :key="item.name"
      >
        <div class="d-flex justify-content-start">
          <div>{{ item.name }}</div>
          <div
            v-if="item.iconName"
            class="ml-auto"
          >
            <font-awesome-icon
              :class="item.classes"
              :icon="item.iconName"
            />
          </div>
        </div>
        <hr>
      </div>
    </b-container>
  </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import { SystemStatusIconInfo } from '@/store/system_status/types';
  import { SYSTEM_STATUS_MODULE_NAME } from '@/store/system_status/constants';

  type Item = SystemStatusIconInfo & {
    name: string,
  };

  const systemStatusModule = namespace(SYSTEM_STATUS_MODULE_NAME);

  @Component
  export default class SystemStatus extends Vue {
    @systemStatusModule.Getter systemStatusDatabaseIconInfo?: SystemStatusIconInfo;

    @systemStatusModule.Mutation setSystemStatusUiIsOpen!: (show: boolean) => void;

    get items(): Item[] {
      const items: Item[] = [];

      if (this.systemStatusDatabaseIconInfo) {
        items.push({ name: 'Database', ...this.systemStatusDatabaseIconInfo });
      }

      return items;
    }

    created() {
      this.setSystemStatusUiIsOpen(true);
    }

    beforeDestroy() {
      this.setSystemStatusUiIsOpen(false);
    }

    onClickOutside() {
      this.$emit('close');
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    background-color: white;
  }

  .effect8 {
    position: relative;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3), 0 0 40px rgba(0, 0, 0, 0.1) inset;
  }

  .effect8:before, .effect8:after {
    content: "";
    position: absolute;
    z-index: -1;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.8);
    top: 10px;
    bottom: 10px;
    left: 0;
    right: 0;
    border-radius: 100px / 10px;
  }

  .effect8:after {
    right: 10px;
    left: auto;
    transform: skew(8deg) rotate(3deg);
  }
</style>
