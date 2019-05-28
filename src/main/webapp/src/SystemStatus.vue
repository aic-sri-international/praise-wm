<template>
  <div v-on-click-outside="onClickOutside">
    <b-container class="container panelShadowBox">
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

    @systemStatusModule.Mutation setShowSystemStatus!: (show: boolean) => void;

    get items(): Item[] {
      const items: Item[] = [];

      if (this.systemStatusDatabaseIconInfo) {
        items.push({ name: 'Database', ...this.systemStatusDatabaseIconInfo });
      }

      return items;
    }

    onClickOutside() {
      this.setShowSystemStatus(false);
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    background-color: white;
  }

</style>
