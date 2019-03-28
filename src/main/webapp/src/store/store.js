// @flow
import Vue from 'vue';
import Vuex from 'vuex';
import get from 'lodash/get';
import user from './user/store';
import sidebar from './sidebar/store';
import notifications from './notifications/store';
import systemStatus from './system_status/store';
import uploader from './uploader/store';
import model from './model/store';
import help from './help/store';

Vue.use(Vuex);

// A Vuex instance is created by combining the state, getters, actions,
// and mutations.
//
// Strict mode runs a synchronous deep watcher on the state tree for detecting
// inappropriate mutations, and it can be quite expensive when you make large
// amount of mutations to the state, so, we do not use it in production mode.
export const store = new Vuex.Store({
  namespaced: true,
  modules: {
    user,
    sidebar,
    notifications,
    systemStatus,
    uploader,
    model,
    help,
  },
  strict: process.env.NODE_ENV !== 'production',
});

export const abortWatcher = (
  moduleName: string,
  stateFieldAbortFlagPath: string,
  abortFlagMutationName: string,
  type: string = 'Action',
  preAbort?: Function,
) => {
  const controller = new window.AbortController();
  const mutation = `${moduleName}/${abortFlagMutationName}`;

  store.commit(mutation, false);
  const start = Date.now();

  const unwatch = store.watch(
    state => get(state[moduleName], stateFieldAbortFlagPath),
    async (abortFlag) => {
      if (abortFlag) {
        try {
          if (preAbort) {
            await preAbort();
          }
          controller.abort();
        } finally {
          // eslint-disable-next-line no-console
          console.log(`${type || 'Action'} aborted after ${Date.now() - start}ms`);
          store.commit(mutation, false);
        }
      }
    },
  );
  return { unwatch, signal: controller.signal };
};

export default store;
