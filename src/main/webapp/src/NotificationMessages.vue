<template>
  <div v-on-click-outside="onClickOutside">
    <b-container class="container effect8">
      <div class="d-flex justify-content-start pt-2">
      <h4>Notifications</h4>
        <div class="ml-auto">
          <div v-if="notificationsForUi.length" @click.stop="onRemoveAllMessages">
            <i class="close fas fa-times fa-lg"></i>
          </div>
        </div>
      </div>
      <hr />
      <div v-if="notificationsForUi.length">
        <div v-for="msg in notificationsInDisplayOrder">
          <div class="d-flex justify-content-start">
            <i class="fas fa-circle mt-1" :style="getCircleStyle(msg.level)"></i>
            <div class="font-weight-bold pl-2">{{msg.level | lowercase | capitalize }}</div>
            <div class="flex-column">
              <div class="pl-2 text-left"><span v-html=msg.text></span></div>
              <div class="text-left">
                <i class="pl-1 fas fa-clock"></i> {{getTimeAgo(msg.date)}} ago
              </div>
            </div>
            <div class="ml-auto">
              <div @click.stop="onRemoveMessage(msg)">
                <i class="fas fa-times"></i>
              </div>
            </div>

          </div>
          <hr />
        </div>
      </div>
      <div v-else>
        <h6>No New Notifications</h6><br/><br>
      </div>
    </b-container>
    <b-modal ref="confirmDeleteModal" title="Remove Messages ?" @ok="removeAllMessages"
             :no-close-on-backdrop="true"
             :no-close-on-esc="true">
      Are you sure you want to dismiss all messages ?
    </b-modal>

  </div>
</template>

<script>
  // @flow

  import { mapGetters } from 'vuex';
  import moment from 'moment';
  import { messageLevels } from '@/services/ws_notifications/types';

  import type { MessageLevel } from '@/services/ws_notifications/types';

  import type { NotificationMessage } from '@/store/notifications/types';

  import {
    NOTIFICATIONS_VXC as NC,
    vxcFp,
  } from '@/store';

  export default {
    name: 'notificationMessages',
    methods: {
      closeNotifications() {
        this.$emit('close');
      },
      onClickOutside() {
        this.closeNotifications();
      },
      getTimeAgo(date: string) {
        return moment.utc(date).fromNow(true);
      },
      onRemoveAllMessages() {
        if (this.notificationsForUi.length) {
          this.$refs.confirmDeleteModal.show();
        }
      },
      removeAllMessages() {
        this.$store.commit(vxcFp(NC, NC.SET.REMOVE_ALL_NOTIFICATIONS_FOR_UI));
        this.closeNotifications();
      },
      onRemoveMessage(item: NotificationMessage) {
        const ids: number[] = [item.id];
        this.$store.commit(vxcFp(NC, NC.SET.REMOVE_NOTIFICATIONS_FOR_UI), ids);
      },
      getCircleStyle(level: MessageLevel) {
        switch (level) {
          case messageLevels.INFO:
            return { color: 'green' };
          case messageLevels.WARN:
            return { color: 'yellow' };
          case messageLevels.ERROR:
            return { color: 'red' };
          default:
            return '';
        }
      },
    },
    computed: {
      ...mapGetters(NC.MODULE, [
        NC.GET.NOTIFICATIONS_FOR_UI,
      ]),
      notificationsInDisplayOrder() {
        return [...this.notificationsForUi].reverse();
      },
    },
    created() {
      this.$store.commit(vxcFp(NC, NC.SET.UI_IS_OPEN), true);
    },
    beforeDestroy() {
      this.$store.commit(vxcFp(NC, NC.SET.UI_IS_OPEN), false);
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