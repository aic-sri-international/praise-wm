<template>
  <div v-if="dataLoaded">
    <card-table
        ref="cardTableRef"
        header="User Maintenance"
        :vmodel="vmodel">

      <div slot="header" v-show="user.isAdminRole">
        <new-button title="Add a new user" @clicked="curUser = null;showAddEditModal=true"></new-button>
      </div>

      <div slot="table" slot-scope="options">
        <b-table bordered striped hover small v-model="vmodel" class="dataTable"
                 :items="users"
                 :fields="fields"
                 :current-page="options.currentPage"
                 :per-page="options.perPage"
                 :filter="options.filter"
                 @filtered="array => $refs.cardTableRef.onFiltered(array)">
        <div slot="name" slot-scope="item">
          {{item.value}}
        </div>
        <div slot="fullname" slot-scope="item">
          {{item.value}}
        </div>
        <div slot="lastLoginDate" slot-scope="item">
          {{item.value | formatDateTime}}
        </div>
        <div slot="loggedInCount" slot-scope="item">
          {{item.value}}
        </div>
        <div slot="actions" slot-scope="item">
          <b-button-group>
            <action-button
                type="edit"
                title="Edit user information"
                :disabled="actionsAreDisable(item.item)"
                @clicked="curUser = item.item;showAddEditModal=true">
            </action-button>
            <action-button
                type="delete"
                title="Delete user"
                :disabled="actionsAreDisable(item.item)"
                @clicked="curUser = item.item;$refs.confirmDeleteModal.show()">
            </action-button>
          </b-button-group>
        </div>
        </b-table>
      </div>
    </card-table>
    <b-modal ref="confirmDeleteModal" title="Delete Account ?" @ok="deleteItem"
             :no-close-on-backdrop="true"
             :no-close-on-esc="true">
      Are you sure you want to delete account <b>{{curUser ? curUser.name : ''}}</b> ?
    </b-modal>
    <add-edit-user v-if="showAddEditModal" :item="curUser" @hide="onAddOrEditHidden"></add-edit-user>
  </div>
</template>

<script>
  // @flow
  import refreshMixin from '@/services/data_refresh/refreshMixin';
  import { refreshTypes } from '@/services/ws_notifications/types';
  import { USER_VXC as UC } from '@/store';
  import { mapGetters } from 'vuex';
  import CardTable from '@/components/CardTable';
  import NewObjectButton from '@/components/NewObjectButton';
  import ActionButton from '@/components/ActionButton';
  import type { VuexUserState } from '@/store/user/types';
  import AddEditUserModal from '@/components/usermaint/AddEditUserModal';
  import { fetchUserDtos, deleteUser } from './dataSourceProxy';

  export default {
    name: 'userMaintenance',
    components: {
      'new-button': NewObjectButton,
      'action-button': ActionButton,
      'add-edit-user': AddEditUserModal,
      'card-table': CardTable,
    },
    mixins: [refreshMixin],
    data() {
      return {
        dataLoaded: false,
        curUser: null,
        showAddEditModal: false,
        vmodel: [],
        users: [],
        fields: {
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
        },
      };
    },
    methods: {
      actionsAreDisable(user : Object) : boolean {
        // Only allow a user that has the admin role to delete or modify accounts
        // Don't allow the special admin user account to be deleted or modified
        // Don't allow the current user to delete or modify their own account
        const loggedInUser : VuexUserState = this[UC.GET.USER];

        return !loggedInUser.isAdminRole
            || UC.ADMIN_NAME === user.name
            || loggedInUser.name === user.name;
      },
      async fetchUsers() {
        try {
          this.users = await fetchUserDtos();
          this.dataLoaded = true;
        } catch (err) {
          this.users = [];
        }
      },
      async deleteItem(e : any) {
        if (e.isOK) {
          try {
            await deleteUser(this.curUser.userId);
          } catch (ex) {
            //
          }
          this.fetchUsers();
        }
      },
      async onAddOrEditHidden(action : string) {
        this.showAddEditModal = false;

        if (action === 'ok') {
          this.fetchUsers();
        }
      },
    },
    computed: {
      ...mapGetters(UC.MODULE, [
        UC.GET.USER,
      ]),
    },
    async created() {
      this.fetchUsers();
      this.registerForDataRefresh(refreshTypes.USER, this.fetchUsers);
    },
  };
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->

<style scoped>
</style>

