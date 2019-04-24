<template>
  <div v-if="autoLogin">
  </div>
  <div v-else>
    <b-modal @shown="focusOnUsername"
             v-model="showModal"
             size="md"
             :no-close-on-esc="true"
             :no-close-on-backdrop="true">
      <template slot="modal-header">
        Login
      </template>
      <div class="container-fluid">
        <form>
          <div class="row">
            <!--The following id is solely here to validate unit and e2e tests -->
            <span id="usernameText">Username</span>
          </div>
          <div class="row">
            <b-input-group>
              <b-input-group-text slot="prepend">
                <i class="fas fa-user"></i>
              </b-input-group-text>
              <b-form-input
                  ref="usernameRef"
                  v-model="name"
                  placeholder="Enter username">
              </b-form-input>
            </b-input-group>
          </div>
          <div class="row" style="margin-top: 12px">
            Password
          </div>
          <div class="row">
            <b-input-group>
              <b-input-group-text slot="prepend" @click.stop="toggleShowPwd">
                <div v-show="showPwd">
                  <i class="fas fa-unlock"></i>
                </div>
                <div v-show="!showPwd">
                  <i class="fas fa-lock"></i>
                </div>
              </b-input-group-text>
            <b-form-input ref="passwordRef" v-model="password"
                  autocomplete="off"
                  :type="showPwd ? 'text' : 'password'"
                  placeholder="Enter password"
                  @keydown.enter.native="doLogin">
              </b-form-input>
              <b-input-group-text slot="append" @click.stop="toggleShowPwd">
                <div v-show="showPwd">
                  <i class="fas fa-eye-slash"></i>
                </div>
                <div v-show="!showPwd">
                  <i class="fas fa-eye"></i>
                </div>
              </b-input-group-text>
            </b-input-group>
          </div>
        </form>
      </div>
      <template slot="modal-footer">
        <b-button class="btn-block" type="submit" @click.stop.prevent="doLogin"
                  :disabled="!isValid">
          Log In
        </b-button>
      </template>
    </b-modal>
  </div>
</template>

<script>
  // @flow
  import { paths } from '@/router/index';
  import { login } from './dataSourceProxy';

  export default {
    name: 'login',
    data() {
      return {
        autoLogin: true,
        showModal: true,
        showPwd: false,
        name: '',
        password: '',
      };
    },
    computed: {
      isValid(): boolean {
        return this.name !== '' && this.password !== '';
      },
    },
    methods: {
      focusOnUsername() {
        this.$refs.usernameRef.focus();
      },
      toggleShowPwd() {
        this.showPwd = !this.showPwd;
        this.$refs.passwordRef.focus();
      },
      async doLogin() {
        try {
          const { name } = this;
          const { password } = this;
          await login({ name, password });
          this.name = '';
          this.password = '';
          this.showPwd = false;
          this.$router.push(paths.HOME);
        } catch (err) {
          // errors already logged/displayed
        }
      },
    },
    async created() {
      if (this.autoLogin) {
        this.name = 'admin';
        this.password = 'admin';
        await this.doLogin();
      }
    },
  };
</script>

