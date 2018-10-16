<template>
  <div class="header">
    <span class="headerTitle" @click.stop="$emit('notificationsTesterClicked')">PRAiSE-WM</span>

    <span class="headerIcon headerHelp" @click="toggleHelp()">
      <span class="fa-layers fa-fw">
         <i class="fas fa-circle" data-fa-transform="grow-12 down-3" style="color: white"></i>
        <i class="fas fa-question fa-inverse" style="color: green" data-fa-transform="shrink-1 down-3"></i>
      </span>
    </span>

    <span class="headerIcon headerNotification" @click="$emit('notificationsClicked')">
      <span class="fa-layers fa-fw">
        <i class="fas fa-bell" data-fa-transform="grow-12 down-2"></i>
        <span v-if="notificationUiHasNewMsg">
          <i class="fas fa-circle" data-fa-transform="shrink-10 up-3 right-3" style="color: blue"></i>
        </span>
      </span>
    </span>

    <span class="headerIcon headerSystemStatus" @click="$emit('systemsStatusClicked')">
      <span class="fa-layers fa-fw">
        <i class="fas fa-desktop" data-fa-transform="grow-14 down-4"></i>
        <i :class="systemStatusOverallClass"></i>
      </span>
    </span>

    <span class="time">
        {{time | formatDateTime}}
      </span>

    <span class="headerIcon headerLogout" @click="doLogout" v-b-tooltip.hover.auto title="Click to logout">
        <i class="fas fa-sign-out-alt" data-fa-transform="grow-14"></i>
      </span>

  </div>
</template>

<script>
  // @flow
  import { paths } from '@/router';
  import { logout } from '@/components/login/dataSourceProxy';
  import { mapGetters } from 'vuex';
  import {
    USER_VXC as UC,
    NOTIFICATIONS_VXC as NC,
    SYSTEM_STATUS_VXC as SS,
    HELP_VXC as HELP,
    store,
    vxcFp,
  } from '@/store';

  let intervalId;

  export default {
    name: 'TopBar',
    data() {
      return {
        time: null,
        paths,
        showHelp: false,
      };
    },
    methods: {
      doLogout() {
        logout();
      },
      toggleHelp() {
        this.showHelp = !this.showHelp;
        store.commit(vxcFp(HELP, HELP.SET.SHOW_HELP), this.showHelp);
      },
    },
    computed: {
      ...mapGetters(UC.MODULE, [
        UC.GET.USER,
      ]),
      ...mapGetters(NC.MODULE, [
        NC.GET.UI_HAS_NEW_MSG,
      ]),
      ...mapGetters(SS.MODULE, [
        SS.GET.STATUS_OVERALL_CLASS,
      ]),
    },
    created() {
      intervalId = window.setInterval(() => {
        this.time = this.$$.getDate();
      }, 1000);
    },
    beforeDestroy() {
      window.clearInterval(intervalId);
    },
  };

</script>

<style scoped>

  .header {
    color: white;
    background: #F5F5F5;
    width: 100%;
    height: 56px;
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
    right: 100px;
    font-size: 12px;
    font-weight: bold;
    color: black;
  }

  .headerLogout {
    right: 40px;
  }

</style>