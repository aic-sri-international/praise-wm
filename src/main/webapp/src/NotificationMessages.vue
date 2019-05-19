<template>
  <!-- eslint-disable vue/no-v-html -->
  <div v-on-click-outside="onClickOutside">
    <b-container class="container effect8">
      <div class="d-flex justify-content-start pt-2">
        <h4>Notifications</h4>
        <div class="ml-auto">
          <div
            v-if="notificationsForUi.length"
            @click.stop="onRemoveAllMessages"
          >
            <i
              class="fas fa-times removeIcon"
              data-fa-transform="grow-6"
            />
          </div>
        </div>
      </div>
      <hr>
      <div v-if="notificationsForUi.length">
        <div
          v-for="msg in notificationsInDisplayOrder"
          :key="msg.id"
        >
          <div class="d-flex justify-content-start">
            <i
              class="fas fa-circle mt-1"
              :style="getCircleStyle(msg.level)"
            />
            <div class="font-weight-bold pl-2">
              {{ msg.level | lowercase | capitalize }}
            </div>
            <div class="flex-column">
              <div
                class="pl-2 text-left"
                v-html="$$.insertWordBreaks(msg.text)"
              />
              <div class="text-left">
                <i
                  class="pl-1 fas fa-clock"
                  style="color: #9bcd5f"
                /> {{ getTimeAgo(msg.date) }} ago
              </div>
            </div>
            <div class="ml-auto">
              <div @click.stop="onRemoveMessage(msg)">
                <i class="fas fa-times removeIcon" />
              </div>
            </div>
          </div>
          <hr>
        </div>
      </div>
      <div v-else>
        <h6>No New Notifications</h6><br><br>
      </div>
    </b-container>
    <b-modal
      ref="confirmDeleteModal"
      title="Remove Messages ?"
      :no-close-on-backdrop="true"
      :no-close-on-esc="true"
      @ok="removeAllMessages"
    >
      Are you sure you want to dismiss all messages ?
    </b-modal>
  </div>
</template>

<script lang="ts">
  import {
    Vue, Component, Emit,
  } from 'vue-property-decorator';
  import {
    namespace,
  } from 'vuex-class';
  import moment from 'moment';
  import { MessageLevel } from '@/services/ws_notifications/types';
  import { NotificationMessage } from '@/store/notifications/types';
  import { BModal } from 'bootstrap-vue';
  import { NOTIFICATIONS_MODULE_NAME } from '@/store/notifications/constants';


  const notificationsModule = namespace(NOTIFICATIONS_MODULE_NAME);

  @Component
  export default class NotificationMessages extends Vue {
    @notificationsModule.Getter
    notificationsForUi!: NotificationMessage[];

    @notificationsModule.Mutation
    setNotificationUiIsOpen!: (isOpen: boolean) => void;

    @notificationsModule.Mutation
    removeAllNotificationsForUi!: () => void;

    @notificationsModule.Mutation
    removeNotificationsForUi!: (ids:number[]) => void;

    @Emit('close')
    closeNotifications(): void {}

    $refs!: {
      confirmDeleteModal: BModal
    };

    get notificationsInDisplayOrder() {
      return [...this.notificationsForUi].reverse();
    }

    created() {
      this.setNotificationUiIsOpen(true);
    }

    beforeDestroy() {
      this.setNotificationUiIsOpen(false);
    }

    getTimeAgo(date: string) {
      return moment.utc(date).fromNow(true);
    }

    onClickOutside() {
      this.closeNotifications();
    }

    onRemoveAllMessages() {
      if (this.notificationsForUi.length) {
        this.$refs.confirmDeleteModal.show();
      }
    }

    removeAllMessages() {
      this.removeAllNotificationsForUi();
      this.closeNotifications();
    }

    onRemoveMessage(item: NotificationMessage) {
      const ids: number[] = [item.id];
      this.removeNotificationsForUi(ids);
    }

    getCircleStyle(level: MessageLevel) {
      switch (level) {
        case MessageLevel.INFO:
          return { color: 'green' };
        case MessageLevel.WARN:
          return { color: 'yellow' };
        case MessageLevel.ERROR:
          return { color: 'red' };
        default:
          return '';
      }
    }
  }
</script>

<style lang="scss" scoped>
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

  .removeIcon {
    cursor: pointer;
  }
</style>
