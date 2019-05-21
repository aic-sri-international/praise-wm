import Vue from 'vue';
import Component from 'vue-class-component';

import { RefreshType } from '@/services/ws_notifications/types';
import { register } from './refreshManager';

import { RegistryCallback, RegistryEntry } from './types';

@Component
export default class RefreshMixin extends Vue {
  dataRefreshDeregisterFunctions: (() => void)[] = [];

  registerForDataRefresh(type: RefreshType, callback: RegistryCallback) {
    if (!type) {
      throw Error('type is a required parameter');
    }
    if (!callback) {
      throw Error('callback is a required parameter');
    }

    if (!this.$options.name) {
      throw Error('Components to be registered must have defined a name');
    }
    const entry: RegistryEntry = {
      type,
      name: this.$options.name,
      callback,
    };

    const unregFunction: () => void = register(entry);
    this.dataRefreshDeregisterFunctions.push(unregFunction);
  }

  mounted(): void {
    if (!this.dataRefreshDeregisterFunctions) {
      throw Error('registerForDataRefresh has not been called');
    }
  }

  beforeDestroy(): void {
    this.dataRefreshDeregisterFunctions.forEach(f => f());
  }
}
