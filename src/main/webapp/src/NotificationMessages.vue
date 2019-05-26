<template>
  <!-- eslint-disable vue/no-v-html -->
  <div v-on-click-outside="onClickOutside">
    <b-container class="container panelShadowBox">
      <div class="d-flex justify-content-start pt-2">
        <h4>Notifications</h4>
        <div class="ml-auto">
          <div
            v-if="notificationsForUi.length"
            @click.stop="onRemoveAllMessages"
          >
            <i
              class="fas fa-times cursorPointer"
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
              :style="getCircleStyle(msg.level)"
              class="fas fa-circle mt-1"
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
                <i class="fas fa-times cursorPointer" />
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
      :no-close-on-backdrop="true"
      :no-close-on-esc="true"
      title="Remove Messages ?"
      @ok="removeAllMessages"
    >
      Are you sure you want to dismiss all messages ?
    </b-modal>
  </div>
</template>

<script lang="ts">
  import { Component, Emit, Vue } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import moment from 'moment';
  import { MessageLevel } from '@/services/ws_notifications/types';
  import { NotificationMessage, VuexNotificationsState } from '@/store/notifications/types';
  import { BModal } from 'bootstrap-vue';
  import { NOTIFICATIONS_MODULE_NAME } from '@/store/notifications/constants';


  const notificationsModule = namespace(NOTIFICATIONS_MODULE_NAME);

  @Component
  export default class NotificationMessages extends Vue {
    @notificationsModule.State notificationsForUi!: VuexNotificationsState['notificationsForUi'];

    @notificationsModule.Mutation setNotificationUiIsOpen!: (isOpen: boolean) => void;

    @notificationsModule.Mutation removeAllNotificationsForUi!: () => void;

    @notificationsModule.Mutation removeNotificationsForUi!: (ids: number[]) => void;

    @Emit('close')
    closeNotifications(): void {
    }

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
      if (!this.notificationsForUi.length) {
        this.closeNotifications();
      }
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

  .cursorPointer {
    cursor: pointer;
  }
</style>
