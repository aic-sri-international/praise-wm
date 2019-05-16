<template>
  <div>
    <b-modal
      ref="addEditModal"
      :no-close-on-backdrop="true"
      :no-close-on-esc="true"
      :title="isNewUser() ? 'New  User' : 'Edit User'"
      :visible="true"
      ok-title="Submit"
      @hide="onHide"
      @shown="() => $refs.userNameRef.focus()"
    >
      <div class="container-fluid">
        <b-row no-gutters>
          <b-col>
            <b-form-group
              label="Name"
              label-align="left"
              label-class="requiredField"
            >
              <b-form-input
                ref="userNameRef"
                v-model.trim="user.name"
                placeholder="Enter user name"
              />
            </b-form-group>
          </b-col>
        </b-row>
        <b-row no-gutters>
          <b-col>
            <b-form-group
              label="Password"
              label-align="left"
              label-class="requiredField"
            >
              <b-form-input
                ref="userPasswordRef"
                v-model.trim="user.password"
                placeholder="Enter password"
                type="password"
              />
            </b-form-group>
          </b-col>
        </b-row>
        <b-row no-gutters>
          <b-col>
            <b-form-group
              label="Confirm Password"
              label-align="left"
              label-class="requiredField"
            >
              <b-form-input
                ref="userPwd2Ref"
                v-model.trim="user.pwd2"
                placeholder="Confirm password"
                type="password"
              />
            </b-form-group>
          </b-col>
        </b-row>
        <b-row no-gutters>
          <b-col>
            <b-form-group
              label="Full Name"
              label-align="left"
              label-class="requiredField"
            >
              <b-form-input
                ref="userFullnameRef"
                v-model.trim="user.fullname"
                placeholder="Enter full name"
              />
            </b-form-group>
          </b-col>
        </b-row>
      </div>
    </b-modal>
  </div>
</template>

<script lang="ts">
  import { mixins } from 'vue-class-component';
  import { Component, Prop } from 'vue-property-decorator';
  import { UserDto } from '@/components/usermaint/types';
  import { USER_VXC as UC } from '@/store';
  import { addUser, updateUser } from './dataSourceProxy';
  import ValidationMixin from '@/services/validations/valmixin';

  let origUser: UserDto;

  @Component
  export default class AddEditUser extends mixins(ValidationMixin) {
    @Prop(Object) readonly item?: UserDto;

    $refs!: {
      userNameRef: HTMLInputElement,
      userPasswordRef: HTMLInputElement,
      userPwd2Ref: HTMLInputElement,
      userFullnameRef: HTMLInputElement,
    };

    user: UserDto & { pwd2: string } = {
      userId: null,
      name: '',
      password: '',
      pwd2: '',
      fullname: '',
    };

    created() {
      if (this.item) {
        this.user.userId = this.item.userId;
        this.user.name = this.item.name;
        this.user.fullname = this.item.fullname;
      }

      origUser = { ...this.user };
    }

    getValidations() {
      const noadmin = () => (
        this.user.name === UC.ADMIN_NAME ? `Name cannot be ${UC.ADMIN_NAME}` : null
      );

      const pwdsMustMatch = () => (
        this.user.password !== this.user.pwd2 ? 'Passwords must match' : null
      );

      return {
        user: {
          name: {
            required: true,
            validator: noadmin,
            ref: this.$refs.userNameRef,
          },
          password: {
            required: () => this.user.userId === null,
            ref: this.$refs.userPasswordRef,
          },
          pwd2: {
            validator: pwdsMustMatch,
            ref: this.$refs.userPwd2Ref,
          },
          fullname: {
            required: true,
            ref: this.$refs.userFullnameRef,
          },
        },
      };
    }

    isNewUser(): boolean {
      return this.user.userId === null;
    }

    isModifiedUser(): boolean {
      return this.user.name !== origUser.name
        || this.user.fullname !== origUser.fullname
        || this.user.password !== '';
    }

    async addOrUpdate() {
      const u = { ...this.user };

      delete u.pwd2;

      try {
        if (this.isNewUser()) {
          delete u.userId;
          await addUser(u);
          this.$emit('hide', 'ok');
        } else if (this.isModifiedUser()) {
          await updateUser(u);
          this.$emit('hide', 'ok');
        } else {
          this.$emit('hide');
        }
      } catch (err) {
        // Already logged by http component
        this.$emit('hide');
      }
    }

    async onHide(event: any) {
      if (event.trigger === 'ok') {
        if (this.validateForm(this.getValidations())) {
          await this.addOrUpdate();
        } else {
          event.preventDefault();
        }
      } else {
        this.$emit('hide');
      }
    }
  }

</script>
