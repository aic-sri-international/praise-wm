<template>
  <div class="container">

    <b-tabs pills small>
      <b-tab title="Notifications">
        <b-form @submit="onSubmit">
          <b-form-select :options="notificationLevels" required
                         v-model="form.type">

          </b-form-select>
          <b-form-input v-model="form.text" required
                        placeholder="Notification message">
          </b-form-input>
          <b-form-checkbox v-model="form.server"
                           :value="true"
                           :unchecked-value="false">
            Remote
          </b-form-checkbox>
          <b-button type="submit" variant="primary">Submit</b-button>
          <b-button type="reset" variant="secondary">Reset</b-button>
        </b-form>

      </b-tab>
      <b-tab title="System Status">
        <b-form @submit="onSubmit">
          <b-form-select :options="systemTypes" required
                         v-model="form.type">

          </b-form-select>
          <b-form-select :options="notificationLevels" required
                         v-model="form.systemLevel">

          </b-form-select>
          <b-form-checkbox v-model="form.server"
                           :value="true"
                           :unchecked-value="false">
            Remote
          </b-form-checkbox>
          <b-button type="submit" variant="primary">Submit</b-button>
          <b-button type="reset" variant="secondary">Reset</b-button>
          <b-button @click.stop="OnClearSystemsStatuses" variant="secondary">Clear</b-button>
        </b-form>

      </b-tab>
      <b-tab title="Data Refresh">
        <b-form @submit="onSubmit">
          <b-form-select :options="refreshTypes" required
                         v-model="form.type">

          </b-form-select>
          <b-form-input v-model="form.text"
                        placeholder="Optional refresh message">
          </b-form-input>
          <b-form-checkbox v-model="form.server"
                           :value="true"
                           :unchecked-value="false">
            Remote
          </b-form-checkbox>
          <b-button type="submit" variant="primary">Submit</b-button>
          <b-button type="reset" variant="secondary">Reset</b-button>
        </b-form>

      </b-tab>
    </b-tabs>
  </div>

</template>

<script>
  // @flow
  import {
    messageLevels,
    refreshTypes,
    broadcastTypes,
    systemStatusTypes,
  } from '@/services/ws_notifications/types';

  import { http, toApiUrl } from '@/services/http';
  import type { NotificationInputDto } from '@/services/data_refresh/types';

  import { triggerRefresh } from '@/services/data_refresh/refreshManager';
  import {
    NOTIFICATIONS_VXC as NC,
    SYSTEM_STATUS_VXC as SS,
    vxcFp,
  } from '@/store';

  const isSystemStatus = type =>
    type === systemStatusTypes.DATABASE;

  const toVuexPath = (type) => {
    let which = '';

    switch (type) {
      case systemStatusTypes.DATABASE:
        which = SS.SET.DATABASE;
        break;
      default:
        throw Error(`Unsupported systemStatusTypes: ${type}`);
    }
    return vxcFp(SS, which);
  };

  const sendNotification = async (msg: NotificationInputDto) : Promise<any> => {
    const result: NotificationInputDto = await http.put(toApiUrl('notification'), msg);
    return Promise.resolve(result);
  };

  const isDataRefresh = type =>
    type === refreshTypes.USER;

  export default {
    data() {
      return {
        form: {
          type: null,
          systemLevel: null,
          text: '',
          server: false,
        },
        notificationLevels: [
          { text: 'Select Level', value: null },
          messageLevels.INFO,
          messageLevels.WARN,
          messageLevels.ERROR,
        ],
        systemTypes: [
          { text: 'Select System', value: null },
          systemStatusTypes.DATABASE,
        ],
        refreshTypes: [
          { text: 'Select Refresh Type', value: null },
          refreshTypes.USER,
        ],
      };
    },
    methods: {
      OnClearSystemsStatuses() {
        this.$store.commit(vxcFp(SS, SS.SET.ALL_STATUSES_UNKNOWN));
      },
      onSubmit(evt: Event) {
        evt.preventDefault();
        const remoteMsg: NotificationInputDto
            = {
              broadcast: broadcastTypes.INCLUSIVE,
              text: this.form.text,
            };

        if (isDataRefresh(this.form.type)) {
          if (this.form.server) {
            remoteMsg.dataRefreshType = this.form.type;
            sendNotification(remoteMsg);
          } else {
            triggerRefresh(this.form.type);
          }
        } else if (isSystemStatus(this.form.type)) {
          if (this.form.server) {
            remoteMsg.systemStatusType = this.form.type;
            remoteMsg.level = this.form.systemLevel;
            sendNotification(remoteMsg);
          } else {
            this.$store.commit(toVuexPath(this.form.type), this.form.systemLevel);
          }
        } else if (this.form.server) {
          remoteMsg.level = this.form.type;
          sendNotification(remoteMsg);
        } else {
          this.$store.commit(
            vxcFp(NC, NC.SET.ADD_NOTIFICATION_FOR_UI),
            {
              date: this.$$.getDate(),
              level: this.form.type,
              text: this.form.text,
            },
          );
        }
      },
    },
  };
</script>

<style scoped>
  .container {
    padding: 0;
    background-color: #efefef;
  }
</style>