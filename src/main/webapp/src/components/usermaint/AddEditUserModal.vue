<template>
  <div>
    <b-modal ref="addEditModal" :title="isNewUser() ? 'New  User' : 'Edit User'"
             @shown="() => $refs.user_name_ref.focus()"
             :visible="true"
             ok-title="Submit"
             :no-close-on-backdrop="true"
             :no-close-on-esc="true"
              @hide="onHide">
      <div class="container-fluid">
        <b-row no-gutters>
          <b-col>
            <b-form-group label-class="requireField" :label="formLables.user.name" label-text-align="left">
              <b-form-input v-model.trim="user.name"
                            ref="user_name_ref"
                            placeholder="Enter user name">
              </b-form-input>
            </b-form-group>
          </b-col>
        </b-row>
        <b-row no-gutters>
          <b-col>
            <b-form-group label-class="requireField" :label="formLables.user.password" label-text-align="left">
              <b-form-input v-model.trim="user.password"
                            ref="user_password_ref"
                            placeholder="Enter password">
              </b-form-input>
            </b-form-group>
          </b-col>
        </b-row>
        <b-row no-gutters>
          <b-col>
            <b-form-group label-class="requireField" :label="formLables.user.pwd2" label-text-align="left">
              <b-form-input v-model.trim="user.pwd2"
                            ref="user_pwd2_ref"
                            placeholder="Confirm password">
              </b-form-input>
            </b-form-group>
          </b-col>
        </b-row>
        <b-row no-gutters>
          <b-col>
            <b-form-group label-class="requireField" :label="formLables.user.fullname" label-text-align="left">
              <b-form-input v-model.trim="user.fullname"
                            ref="user_fullname_ref"
                            placeholder="Enter full name">
              </b-form-input>
            </b-form-group>
          </b-col>
        </b-row>
      </div>
    </b-modal>
  </div>
</template>

<script>
  // @flow
  import isEqual from 'lodash/isEqual';
  import { USER_VXC as UC } from '@/store';
  import valmixin from '@/validations/valmixin';
  import { required, requiredIf, sameAs } from 'vuelidate/lib/validators';
  import { addUser, updateUser } from './dataSourceProxy';

  let origUser;

  function noAdmin(value : string) {
    return value !== UC.ADMIN_NAME;
  }

  export default {
    name: 'addEditUser',
    props: {
      item: Object,
    },
    mixins: [valmixin],
    validations: {
      user: {
        name: {
          required,
          noAdmin,
        },
        password: {
          required: requiredIf(user => user.userId === ''),
        },
        pwd2: {
          'Passwords must match': sameAs('password'),
        },
        fullname: {
          required,
        },
      },
    },
    data() {
      return {
        adminName: UC.ADMIN_NAME,
        user: {
          userId: '',
          name: '',
          password: '',
          pwd2: '',
          fullname: '',
        },
        formLables: {
          user: {
            name: 'Name',
            password: 'Password',
            pwd2: 'Confirm password',
            fullname: 'Full name',
          },
        },
      };
    },
    // The following is a work-around to a bug seen on Firefox when
    // running Webpack Dev Server.
    //
    // If the user.password input element has 'password' type, the value of
    // the user.name field gets set into the user.password field.
    watch: {
      '$v.user.password.$dirty': function passwordBugFix() {
        this.$refs.userPasswordRef.setAttribute('type', 'password');
        this.$refs.userPasswordRef.focus();
      },
    },
    methods: {
      isNewUser() : boolean { return this.user.userId === ''; },
      isUserModified() : boolean { return !isEqual(this.user, origUser); },
      async addOrUpdate() {
        const u = { ...this.user };

        u.pwd2 = undefined;

        try {
          if (this.isNewUser()) {
            u.userId = undefined;
            await addUser(u);
            this.$emit('hide', 'ok');
          } else if (this.isUserModified()) {
            await updateUser(u);
            this.$emit('hide', 'ok');
          } else {
            this.$emit('hide');
          }
        } catch (err) {
          // Already logged by http component
          this.$emit('hide');
        }
      },
      async onHide(event : Object) {
        if (event.trigger === 'ok') {
          if (this.validateForm()) {
            this.addOrUpdate();
          } else {
            event.preventDefault();
          }
        } else {
          this.$emit('hide');
        }
      },
    },
    created() {
      if (this.item) {
        this.user.userId = this.item.userId;
        this.user.name = this.item.name;
        this.user.fullname = this.item.fullname;
      }

      origUser = { ...this.user };
    },
  };

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->

<style scoped>
</style>

