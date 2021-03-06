<template>
  <div class="container">
    <b-tabs
      pills
      small
    >
      <b-tab title="Notifications">
        <b-form @submit="onSubmit">
          <b-form-select
            v-model="form.type"
            :options="notificationLevels"
            required
          />
          <b-form-input
            v-model="form.text"
            placeholder="Notification message"
            required
          />
          <b-form-checkbox
            v-model="form.server"
            :unchecked-value="false"
            :value="true"
          >
            Remote
          </b-form-checkbox>
          <b-button
            type="submit"
            variant="primary"
          >
            Submit
          </b-button>
          <b-button
            type="reset"
            variant="secondary"
          >
            Reset
          </b-button>
        </b-form>
      </b-tab>
      <b-tab title="System Status">
        <b-form @submit="onSubmit">
          <b-form-select
            v-model="form.type"
            :options="systemTypes"
            required
          />
          <b-form-select
            v-model="form.systemLevel"
            :options="notificationLevels"
            required
          />
          <b-form-checkbox
            v-model="form.server"
            :unchecked-value="false"
            :value="true"
          >
            Remote
          </b-form-checkbox>
          <b-button
            type="submit"
            variant="primary"
          >
            Submit
          </b-button>
          <b-button
            type="reset"
            variant="secondary"
          >
            Reset
          </b-button>
          <b-button
            variant="secondary"
            @click.stop="OnClearSystemsStatuses"
          >
            Clear
          </b-button>
        </b-form>
      </b-tab>
      <b-tab title="Data Refresh">
        <b-form @submit="onSubmit">
          <b-form-select
            v-model="form.type"
            :options="refreshTypes"
            required
          />
          <b-form-input
            v-model="form.text"
            placeholder="Optional refresh message"
          />
          <b-form-checkbox
            v-model="form.server"
            :unchecked-value="false"
            :value="true"
          >
            Remote
          </b-form-checkbox>
          <b-button
            type="submit"
            variant="primary"
          >
            Submit
          </b-button>
          <b-button
            type="reset"
            variant="secondary"
          >
            Reset
          </b-button>
        </b-form>
      </b-tab>
    </b-tabs>
  </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import {
    BroadcastType,
    MessageLevel,
    RefreshType,
    SystemStatusType,
  } from '@/services/ws_notifications/types';

  import { http, toApiUrl } from '@/services/http';
  import { NotificationInputDto } from '@/services/data_refresh/types';
  import { triggerRefresh } from '@/services/data_refresh/refreshManager';
  import { NotificationForUiPayload } from '@/store/notifications/types';
  import { NOTIFICATIONS_MODULE_NAME } from '@/store/notifications/constants';
  import { SYSTEM_STATUS_MODULE_NAME } from '@/store/system_status/constants';

  const isSystemStatus = (type: any) => type === SystemStatusType.DATABASE;
  const isDataRefresh = (type: any) => type === RefreshType.USER;

  const sendNotification = async (msg: NotificationInputDto): Promise<any> => {
    const result: any = await http.put(toApiUrl('notification'), msg);
    return Promise.resolve(result);
  };

  interface formData {
    type: MessageLevel | SystemStatusType | RefreshType | null,
    systemLevel: MessageLevel | null,
    text: '',
    server: boolean,
  }

  const notificationsModule = namespace(NOTIFICATIONS_MODULE_NAME);
  const systemStatusModule = namespace(SYSTEM_STATUS_MODULE_NAME);

  @Component
  export default class NotificationMessages extends Vue {
    form: formData = {
      type: null,
      systemLevel: null,
      text: '',
      server: false,
    };

    notificationLevels = [
      {
        text: 'Select Level',
        value: null,
      },
      MessageLevel.INFO,
      MessageLevel.WARN,
      MessageLevel.ERROR,
    ];

    systemTypes = [
      {
        text: 'Select System',
        value: null,
      },
      SystemStatusType.DATABASE,
    ];

    refreshTypes = [
      {
        text: 'Select Refresh Type',
        value: null,
      },
      RefreshType.USER,
    ];

    @notificationsModule.Mutation
    addNotificationForUi!: (payload: NotificationForUiPayload) => void;

    @systemStatusModule.Mutation
    setAllStatusesToUnknown!: () => void;

    @systemStatusModule.Mutation
    setSystemStatusDatabase!: (msgLvl: MessageLevel | null) => void;

    OnClearSystemsStatuses() {
      this.setAllStatusesToUnknown();
    }

    onSubmit(evt: Event) {
      evt.preventDefault();
      const remoteMsg: NotificationInputDto = {
        broadcast: BroadcastType.INCLUSIVE,
        text: this.form.text,
      };

      if (isDataRefresh(this.form.type)) {
        const type: RefreshType = this.form.type as RefreshType;
        if (this.form.server) {
          remoteMsg.dataRefreshType = type;
          sendNotification(remoteMsg);
        } else {
          triggerRefresh(type);
        }
      } else if (isSystemStatus(this.form.type)) {
        const type: SystemStatusType = this.form.type as SystemStatusType;
        if (this.form.server) {
          remoteMsg.systemStatusType = type;
          if (this.form.systemLevel) {
            remoteMsg.level = this.form.systemLevel;
          } else {
            remoteMsg.level = undefined;
          }
          sendNotification(remoteMsg);
        } else {
          switch (type) {
            case SystemStatusType.DATABASE:
              this.setSystemStatusDatabase(this.form.systemLevel);
              break;
            default:
              throw Error(`Unsupported systemStatusTypes: ${type}`);
          }
        }
      } else {
        const type: MessageLevel = this.form.type as MessageLevel;
        if (this.form.server) {
          remoteMsg.level = type;
          sendNotification(remoteMsg);
        } else {
          this.addNotificationForUi(
            {
              date: this.$$.getDate(),
              level: this.form.type as MessageLevel,
              text: this.form.text,
            },
          );
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding: 0;
    background-color: #efefef;
  }
</style>
