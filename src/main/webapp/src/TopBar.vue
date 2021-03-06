<template>
  <div class="header">
    <span
      class="headerTitle"
      @click.stop="$emit('notificationsTesterClicked')"
    >PRAiSE-WM</span>

    <span
      v-tippy
      class="headerIcon headerHelp"
      title="Toggle Help"
      @click="toggleHelp()"
    >
      <span class="fa-layers fa-fw">
        <i
          class="fas fa-circle"
          data-fa-transform="grow-12 down-3"
          style="color: white"
        />
        <i
          class="fas fa-question fa-inverse"
          data-fa-transform="shrink-1 down-3"
          style="color: green"
        />
      </span>
    </span>

    <span
      v-tippy
      class="headerIcon headerNotification"
      title="Notifications"
      @click.stop="setShowNotificationsUi(!showNotificationsUi)"
    >
      <span class="fa-layers fa-fw">
        <i
          class="fas fa-bell"
          data-fa-transform="grow-12 down-2"
        />
        <span v-if="notificationUiHasNewMsg">
          <i
            class="fas fa-circle"
            data-fa-transform="shrink-10 up-3 right-3"
            style="color: blue"
          />
        </span>
      </span>
    </span>

    <span
      v-tippy
      class="headerIcon headerSystemStatus"
      title="System Status"
      @click.stop="setShowSystemStatus(!showSystemStatus)"
    >
      <span class="fa-layers fa-fw">
        <i
          class="fas fa-desktop"
          data-fa-transform="grow-14 down-4"
        />
        <font-awesome-icon
          v-if="iconInfo"
          :class="iconInfo.classes"
          :icon="iconInfo.iconName"
          transform="down-2 right-1"
        />
      </span>
    </span>

    <span class="time">
      {{ time | formatDateTime }}
    </span>

    <span
      v-tippy
      class="headerIcon headerPreferences"
      title="Preferences"
      @click.stop="setShowPreferences(!showPreferences)"
    >
      <i
        class="fas fa-cog"
        data-fa-transform="grow-14"
      />
    </span>

    <span
      v-tippy
      class="headerIcon headerLogout"
      title="Click to logout"
      @click="doLogout"
    >
      <i
        class="fas fa-sign-out-alt"
        data-fa-transform="grow-14"
      />
    </span>
  </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import Paths from '@/router';
  import { logout } from '@/components/login/dataSourceProxy';
  import { SystemStatusIconInfo, VuexSystemStatusState } from '@/store/system_status/types';
  import { NOTIFICATIONS_MODULE_NAME } from '@/store/notifications/constants';
  import { HELP_MODULE_NAME } from '@/store/help/constants';
  import { SYSTEM_STATUS_MODULE_NAME } from '@/store/system_status/constants';
  import { VuexNotificationsState } from '@/store/notifications/types';
  import { PREFERENCES_MODULE_NAME } from '@/store/preferences/constants';
  import { VuexPreferencesState } from '@/store/preferences/types';

  let intervalId: number = 0;

  const notificationsModule = namespace(NOTIFICATIONS_MODULE_NAME);
  const helpModule = namespace(HELP_MODULE_NAME);
  const systemStatusModule = namespace(SYSTEM_STATUS_MODULE_NAME);
  const preferencesModule = namespace(PREFERENCES_MODULE_NAME);

  @Component
  export default class TopBar extends Vue {
    time: Date | null = null;

    paths = Paths;

    showHelp = false;

    @notificationsModule.State showNotificationsUi!: VuexNotificationsState['showNotificationsUi'];

    @notificationsModule.State notificationUiHasNewMsg!: VuexNotificationsState['notificationUiHasNewMsg'];

    @preferencesModule.State showPreferences!: VuexPreferencesState['showPreferences'];

    @systemStatusModule.State showSystemStatus!: VuexSystemStatusState['showSystemStatus'];

    @systemStatusModule.Getter systemStatusOverallIconInfo?: SystemStatusIconInfo;

    @helpModule.Mutation('showHelp') mutateShowHelp!: (payload: boolean) => void;

    @notificationsModule.Mutation setShowNotificationsUi!: (isOpen: boolean) => void;

    @preferencesModule.Mutation setShowPreferences!: (show: boolean) => void;

    @systemStatusModule.Mutation setShowSystemStatus!: (show: boolean) => void;

    get iconInfo(): SystemStatusIconInfo | null {
      const iconInfo: SystemStatusIconInfo | undefined = this.systemStatusOverallIconInfo;
      if (iconInfo) {
        return { ...iconInfo };
      }
      return null;
    }

    created() {
      intervalId = window.setInterval(() => {
        this.time = this.$$.getDate();
      }, 1000);
    }

    beforeDestroy() {
      window.clearInterval(intervalId);
    }

    doLogout() {
      logout();
    }

    toggleHelp() {
      this.showHelp = !this.showHelp;
      this.mutateShowHelp(this.showHelp);
    }
  }

</script>

<style lang="scss" scoped>
  @import '~@/styles/_variables.scss';

  .header {
    color: white;
    background: #F5F5F5;
    width: 100%;
    height: $topBar-height;
    top: 0;
    left: 0;
    padding-left: 4px;
    padding-bottom: 4px;
    position: fixed;
    text-align: left;

    border-width: 1px;
    border-style: solid;
    border-bottom-color: #aaa;
    border-right-color: #aaa;
    border-top-color: #ddd;
    border-left-color: #ddd;
    border-radius: 3px;
    -moz-border-radius: 3px;
    -webkit-border-radius: 3px;
  }

  .headerTitle {
    margin-left: 24px;
    font-size: 32px;
    text-shadow: 2px 2px 4px #000000;
  }

  .headerIcon {
    position: fixed;
    top: 16px;
    color: #aaa;
    cursor: pointer;
  }

  .headerSystemStatus {
    right: 254px;
  }

  .headerNotification {
    right: 300px;
  }

  .headerHelp {
    right: 350px;
  }

  .time {
    position: fixed;
    top: 20px;
    right: 116px;
    font-size: 12px;
    font-weight: bold;
    color: black;
  }

  .headerPreferences {
    right: 80px;
  }

  .headerLogout {
    right: 40px;
  }

</style>
