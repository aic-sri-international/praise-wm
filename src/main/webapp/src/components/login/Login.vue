<template>
  <div v-if="autoLogin" />
  <div v-else>
    <b-modal
      v-model="showModal"
      :no-close-on-backdrop="true"
      :no-close-on-esc="true"
      size="md"
      @shown="focusOnUsername"
    >
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
                <i class="fas fa-user" />
              </b-input-group-text>
              <b-form-input
                ref="usernameRef"
                v-model="name"
                placeholder="Enter username"
              />
            </b-input-group>
          </div>
          <div
            class="row"
            style="margin-top: 12px"
          >
            Password
          </div>
          <div class="row">
            <b-input-group>
              <b-input-group-text
                slot="prepend"
                @click.stop="toggleShowPwd"
              >
                <div v-show="showPwd">
                  <i class="fas fa-unlock" />
                </div>
                <div v-show="!showPwd">
                  <i class="fas fa-lock" />
                </div>
              </b-input-group-text>
              <b-form-input
                ref="passwordRef"
                v-model="password"
                :type="showPwd ? 'text' : 'password'"
                autocomplete="off"
                placeholder="Enter password"
                @keydown.enter.native="doLogin"
              />
              <b-input-group-text
                slot="append"
                @click.stop="toggleShowPwd"
              >
                <div v-show="showPwd">
                  <i class="fas fa-eye-slash" />
                </div>
                <div v-show="!showPwd">
                  <i class="fas fa-eye" />
                </div>
              </b-input-group-text>
            </b-input-group>
          </div>
        </form>
      </div>
      <template slot="modal-footer">
        <b-button
          :disabled="!isValid"
          class="btn-block"
          type="submit"
          @click.stop.prevent="doLogin"
        >
          Log In
        </b-button>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';
  import paths from '@/router/paths';
  import { login } from './dataSourceProxy';

  @Component
  export default class Login extends Vue {
    $refs!: {
      usernameRef: HTMLElement
      passwordRef: HTMLElement
    };

    autoLogin = false;

    showModal = true;

    name = '';

    password = '';

    showPwd = false;

    get isValid(): boolean {
      return this.name !== '' && this.password !== '';
    }

    focusOnUsername() {
      this.$refs.usernameRef.focus();
    }

    toggleShowPwd() {
      this.showPwd = !this.showPwd;
      this.$refs.passwordRef.focus();
    }

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
    }

    async created() {
      if (this.autoLogin) {
        this.name = 'admin';
        this.password = 'admin';
        await this.doLogin();
      }
    }
  }
</script>
