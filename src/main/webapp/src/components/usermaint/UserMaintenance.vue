<template>
  <div v-if="dataLoaded">
    <card-table
      ref="cardTableRef"
      :items="users"
      header="User Maintenance"
    >
      <div
        v-show="user.isAdminRole"
        slot="header"
      >
        <new-object-button
          title="Add a new user"
          @clicked="curUser = null;showAddEditModal=true"
        />
      </div>

      <div
        slot="table"
        slot-scope="options"
      >
        <b-table
          :items="users"
          :fields="fields"
          :current-page="options.currentPage"
          :per-page="options.perPage"
          :filter="options.filter"
          bordered
          class="dataTable"
          hover
          small
          striped
          @filtered="array => $refs.cardTableRef.onFiltered(array)"
        >
          <div
            slot="name"
            slot-scope="item"
          >
            {{ item.value }}
          </div>
          <div
            slot="fullname"
            slot-scope="item"
          >
            {{ item.value }}
          </div>
          <div
            slot="lastLoginDate"
            slot-scope="item"
          >
            {{ item.value | formatDateTime }}
          </div>
          <div
            slot="loggedInCount"
            slot-scope="item"
          >
            {{ item.value }}
          </div>
          <div
            slot="actions"
            slot-scope="item"
          >
            <b-button-group>
              <action-button
                :disabled="actionsAreDisable(item.item)"
                title="Edit user information"
                type="edit"
                @clicked="curUser = item.item;showAddEditModal=true"
              />
              <action-button
                :disabled="actionsAreDisable(item.item)"
                title="Delete user"
                type="delete"
                @clicked="curUser = item.item;$refs.confirmDeleteModal.show()"
              />
            </b-button-group>
          </div>
        </b-table>
      </div>
    </card-table>
    <b-modal
      ref="confirmDeleteModal"
      :no-close-on-backdrop="true"
      :no-close-on-esc="true"
      title="Delete Account ?"
      @ok="deleteItem"
    >
      Are you sure you want to delete account <b>{{ curUser ? curUser.name : '' }}</b> ?
    </b-modal>
    <add-edit-user-modal
      v-if="showAddEditModal"
      :item="curUser"
      @hide="onAddOrEditHidden"
    />
  </div>
</template>

<script lang="ts">
  import { Component } from 'vue-property-decorator';
  import { mixins } from 'vue-class-component';
  import { namespace } from 'vuex-class';
  import isNil from 'lodash/isNil';
  import RefreshMixin from '@/services/data_refresh/refreshMixin';
  import { RefreshType } from '@/services/ws_notifications/types';
  import { USER_VXC } from '@/store';
  import CardTable from '@/components/CardTable.vue';
  import NewObjectButton from '@/components/NewObjectButton.vue';
  import ActionButton from '@/components/ActionButton.vue';
  import { VuexUserState } from '@/store/user/types';
  import AddEditUserModal from '@/components/usermaint/AddEditUserModal.vue';
  import { deleteUser, fetchUserDtos } from './dataSourceProxy';
  import { UserDto } from '@/components/usermaint/types';

  const userModule = namespace(USER_VXC.MODULE);

  @Component({
    components: {
      NewObjectButton,
      ActionButton,
      AddEditUserModal,
      CardTable,
    },
  })
  export default class UserMaintenance extends mixins(RefreshMixin) {
    @userModule.Getter user!: VuexUserState;

    dataLoaded = false;

    curUser: UserDto | null = null;

    showAddEditModal = false;

    users: UserDto[] = [];

    fields = {
      name: {
        label: 'Username',
        sortable: true,
      },
      fullname: {
        label: 'Name',
        sortable: true,
      },
      lastLoginDate: {
        label: 'Last Login',
        sortable: true,
      },
      loggedInCount: {
        label: 'Active Sessions',
        sortable: true,
      },
      actions: {
        label: 'Actions',
      },
    };

    async created() {
      await this.fetchUsers();
      this.registerForDataRefresh(RefreshType.USER, this.fetchUsers);
    }

    actionsAreDisable(user: VuexUserState): boolean {
      // Only allow a user that has the admin role to delete or modify accounts
      // Don't allow the special admin user account to be deleted or modified
      // Don't allow the current user to delete or modify their own account
      const loggedInUser: VuexUserState = this.user;

      return !loggedInUser.isAdminRole
        || USER_VXC.ADMIN_NAME === user.name
        || loggedInUser.name === user.name;
    }

    async fetchUsers() {
      try {
        this.users = await fetchUserDtos();
        this.dataLoaded = true;
      } catch (err) {
        this.users = [];
      }
    }

    async deleteItem(event: any) {
      if (event.trigger === 'ok' && this.curUser && !isNil(this.curUser.userId)) {
        try {
          await deleteUser(this.curUser.userId);
        } catch (ex) {
          //
        }
        await this.fetchUsers();
      }
    }

    async onAddOrEditHidden(action: string) {
      this.showAddEditModal = false;

      if (action === 'ok') {
        await this.fetchUsers();
      }
    }
  }
</script>
