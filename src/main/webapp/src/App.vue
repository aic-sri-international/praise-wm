<template>
  <div
    id="app"
    :style="sideBarStyle"
  >
    <top-bar
      v-show="isLoggedIn"
      @notificationsClicked="onNotificationsClicked"
      @notificationsTesterClicked="onNotificationsTesterClicked"
      @systemsStatusClicked="onSystemsStatusClicked"
    />

    <div class="parent">
      <scroll-to-top-button ref="scrollToTopRef" />

      <div
        v-show="isLoggedIn"
        class="sideBar"
      >
        <side-bar />
      </div>

      <div
        class="contentStyle"
        @scroll="ev => $refs.scrollToTopRef.onScrollEvent(ev)"
      >
        <notification-messages
          v-if="showNotifications"
          class="notificationsPanel"
          @close="showNotifications = false"
        />
        <notification-message-tester
          v-if="showNotificationsTester"
          class="notificationsTesterPanel"
        />
        <system-status
          v-if="showSystemsStatus"
          class="systemsStatusPanel"
          @close="showSystemsStatus = false"
        />
        <router-view id="router" />
        <span
          v-for="msg in httpErrors"
          :key="msg.id"
        >
          {{ displayError(msg) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import {
    Vue, Component, Watch,
  } from 'vue-property-decorator';
  import {
    namespace,
  } from 'vuex-class';

  import BootstrapVue from 'bootstrap-vue';
  import 'bootstrap/dist/css/bootstrap.css';
  import 'bootstrap-vue/dist/bootstrap-vue.css';

  import Toasted from 'vue-toasted';
  import moment, { MomentInput } from 'moment';

  import ScrollToTopButton from '@/ScrollToTopButton.vue';
  import TopBar from '@/TopBar.vue';
  import SideBar from '@/SideBar.vue';
  import NotificationMessages from '@/NotificationMessages.vue';
  import MsgTester from '@/MsgTester.vue';
  import SystemStatus from '@/SystemStatus.vue';
  import Vue2Filters from 'vue2-filters';
  import VueTippy from 'vue-tippy';
  import { HttpError, VuexNotificationsState } from '@/store/notifications/types';
  import { SideBarStyle, VuexSideBarState } from '@/store/sidebar/types';
  import { USER_MODULE_NAME } from '@/store/user/constants';
  import { SIDEBAR_MODULE_NAME } from '@/store/sidebar/constants';
  import { NOTIFICATIONS_MODULE_NAME } from '@/store/notifications/constants';
  import { SYSTEM_STATUS_MODULE_NAME } from '@/store/system_status/constants';
  import { UPLOADER_MODULE_NAME } from '@/store/uploader/constants';

  Vue.use(BootstrapVue);
  Vue.use(Toasted);
  Vue.use(Vue2Filters);
  Vue.use(VueTippy, {
    flipDuration: 0,
    theme: 'light',
    arrow: true,
    popperOptions: {
      modifiers: {
        preventOverflow: {
          enabled: true,
        },
        hide: {
          enabled: false,
        },
      },
    },
  });

  const dtFormat = 'MM/DD/YYYY HH:mm:ss';
  Vue.filter('formatDateTime', (value: MomentInput) => (value ? `${moment.utc(value).format(dtFormat)}Z` : null));

  const userModule = namespace(USER_MODULE_NAME);
  const sideBarModule = namespace(SIDEBAR_MODULE_NAME);
  const notificationsModule = namespace(NOTIFICATIONS_MODULE_NAME);
  const systemStatusModule = namespace(SYSTEM_STATUS_MODULE_NAME);
  const uploaderModule = namespace(UPLOADER_MODULE_NAME);

  @Component({
    components: {
      TopBar,
      SideBar,
      ScrollToTopButton,
      NotificationMessages,
      'notification-message-tester': MsgTester,
      SystemStatus,
    },
  })
  export default class App extends Vue {
    showNotifications = false;

    showNotificationsTester = false;

    showSystemsStatus = false;

    @userModule.State
    isLoggedIn!: boolean;

    @notificationsModule.State
    httpErrors!: VuexNotificationsState['httpErrors'];

    @sideBarModule.State
    sideBarStyle!: VuexSideBarState['sideBarStyle'];

    @notificationsModule.Mutation
    removeHttpError!: (id: number) => void;

    @systemStatusModule.Mutation
    setAllStatusesToUnknown!: () => void;

    @notificationsModule.Mutation
    removeAllNotificationsForUi!: () => void;

    @uploaderModule.Mutation
    removeAllUploaderEntries!: () => void;

    @Watch('isLoggedIn')
    onIsLoggedIn(loggedIn: boolean) {
      if (!loggedIn) {
        this.showSystemsStatus = false;
        this.showNotifications = false;
        this.setAllStatusesToUnknown();
        this.removeAllNotificationsForUi();
        this.removeAllUploaderEntries();
      }
    }

    displayError(msg: HttpError) {
      this.$$.showToastError(msg.error);
      this.removeHttpError(msg.id);
    }

    onNotificationsClicked() {
      this.showNotifications = !this.showNotifications;
    }

    onNotificationsTesterClicked() {
      if (process.env.NODE_ENV !== 'production') {
        this.showNotificationsTester = !this.showNotificationsTester;
      }
    }

    onSystemsStatusClicked() {
      this.showSystemsStatus = !this.showSystemsStatus;
    }
  }
</script>

<style lang="scss" scoped>
  #app {
    font-family: 'Segoe UI', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    padding-top: 56px;
    padding-right: 8px;
  }

  .parent {
    display: flex;
    align-items: stretch;
  }

  .sideBar {
    z-index: 100;
    position: fixed;
    left: 0;
    overflow-y: auto;
    overflow-x: hidden;
    height: calc(100vh - 56px);
    background-color: #e1e1e1;
  }

  /*
    Bootstap uses a z-index of 1030 for its 'fixed' components.
    Since the notification's panel will display a Bootstrap Model, we need to set its
    z-index to a value >= 1030 so that the dialog is not hidden by the TopBar.
  */
  .notificationsPanel {
    position: fixed;
    width: 440px;
    right: 12px;
    max-width: 100vw;
    max-height: calc(100vh - (56px + 8px));
    overflow-y: auto;
    overflow-x: hidden;
    margin-top: 4px;
    z-index: 1030;
  }

  .notificationsTesterPanel {
    position: fixed;
    max-height: 300px;
    max-width: 340px;
    overflow-x: auto;
    left: 0;
    z-index: 1030;
  }

  .systemsStatusPanel {
    position: fixed;
    width: 200px;
    right: 240px;
    max-width: calc(100vw - 240px);
    max-height: calc(100vh - (56px + 8px));
    overflow-y: auto;
    overflow-x: hidden;
    margin-top: 4px;
    z-index: 1030;
  }

  .contentStyle {
    flex-grow: 1;
    padding-top: 8px;
    overflow-y: auto;
    height: calc(100vh - (56px + 8px));
  }
</style>
