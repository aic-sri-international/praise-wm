<template>

  <div id="app" :style="sideBarStyle">
    <top-bar
        @notificationsClicked="onNotificationsClicked"
        @notificationsTesterClicked="onNotificationsTesterClicked"
        @systemsStatusClicked="onSystemsStatusClicked"
        v-show="user.isLoggedIn"></top-bar>

    <div class="parent">
      <scroll-to-top-button ref="scrollToTop_ref"></scroll-to-top-button>

      <div class="sideBar" v-show="user.isLoggedIn">
        <side-bar></side-bar>
      </div>

      <div class="contentStyle" @scroll="ev => $refs.scrollToTop_ref.onScrollEvent(ev)" v-show="user.isLoggedIn">
        <notification-messages
            v-if="showNotifications"
            @close="showNotifications = false"
            class="notificationsPanel">
        </notification-messages>
        <notification-message-tester
            v-if="showNotificationsTester"
            class="notificationsTesterPanel">
        </notification-message-tester>
        <system-status
            v-if="showSystemsStatus"
            @close="showSystemsStatus = false"
            class="systemsStatusPanel">
        </system-status>
        <keep-alive>
          <router-view id="router"></router-view>
        </keep-alive>
        <span v-for="msg in httpErrors">
          {{displayError(msg)}}
        </span>
        <vue-snotify/>
      </div>
    </div>
  </div>
</template>

<script>
  // @flow

  import Vue from 'vue';

  import BootstrapVue from 'bootstrap-vue';
  import 'bootstrap/dist/css/bootstrap.css';
  import 'bootstrap-vue/dist/bootstrap-vue.css';

  import Toasted from 'vue-toasted';
  import moment from 'moment';

  import ScrollToTopButton from '@/ScrollToTopButton';
  import TopBar from '@/TopBar';
  import SideBar from '@/SideBar';
  import NotificationMessages from '@/NotificationMessages';
  import MsgTester from '@/notifications/testmsg/MsgTester';
  import SystemStatus from '@/SystemStatus';

  import { mapGetters } from 'vuex';
  import Vue2Filters from 'vue2-filters';
  import VueTippy from 'vue-tippy';
  import Snotify from 'vue-snotify';
  import 'vue-snotify/styles/material.css';

  import {
    USER_VXC as UC,
    SIDEBAR_VXC as SB,
    SYSTEM_STATUS_VXC as SS,
    NOTIFICATIONS_VXC as NC,
    UPLOADER_VXC as UP,
    vxcFp,
  } from '@/store';
  import type { HttpError } from '@/store/notifications/types';

  Vue.use(BootstrapVue);
  Vue.use(Toasted);
  Vue.use(Vue2Filters);
  Vue.use(VueTippy, {
    flipDuration: 0,
    popperOptions: {
      modifiers: {
        preventOverflow: {
          enabled: false,
        },
        hide: {
          enabled: false,
        },
      },
    },
  });
  Vue.use(Snotify);

  Vue.filter('formatDateTime', value => (value ? `${moment.utc(value).format('MM/DD/YYYY HH:mm:ss')}Z` : null));

  export default {
    name: 'app',
    components: {
      TopBar,
      SideBar,
      ScrollToTopButton,
      NotificationMessages,
      'notification-message-tester': MsgTester,
      SystemStatus,
    },
    data() {
      return {
        showNotifications: false,
        showNotificationsTester: false,
        showSystemsStatus: false,
      };
    },
    methods: {
      displayError(msg: HttpError) {
        this.$$.showToastError(msg.error);
        this.$store.commit(vxcFp(NC, NC.SET.REMOVE_HTTP_ERROR), msg.id);
      },
      onNotificationsClicked() {
        this.showNotifications = !this.showNotifications;
      },
      onNotificationsTesterClicked() {
        if (process.env.NODE_ENV !== 'production') {
          this.showNotificationsTester = !this.showNotificationsTester;
        }
      },
      onSystemsStatusClicked() {
        this.showSystemsStatus = !this.showSystemsStatus;
      },
    },
    computed: {
      ...mapGetters(NC.MODULE, [
        NC.GET.HTTP_ERRORS,
      ]),
      ...mapGetters(UC.MODULE, [
        UC.GET.USER,
      ]),
      ...mapGetters(SB.MODULE, [
        SB.GET.SIDEBAR_STYLE,
      ]),
    },
    watch: {
      [UC.GET.USER](user) {
        if (!user.isLoggedIn) {
          this.showSystemsStatus = false;
          this.$store.commit(vxcFp(SS, SS.SET.ALL_STATUSES_UNKNOWN));
          this.showNotifications = false;
          this.$store.commit(vxcFp(NC, NC.SET.REMOVE_ALL_NOTIFICATIONS_FOR_UI));
          this.$store.commit(vxcFp(UP, UP.SET.REMOVE_ALL_ENTRIES));
        }
      },
    },
  };
</script>

<style>
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
