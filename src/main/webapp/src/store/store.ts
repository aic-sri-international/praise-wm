import Vue from 'vue';
import Vuex, { StoreOptions } from 'vuex';
import { RootState, VXC } from './types';
import user from './user';
import sidebar from './sidebar';
import notifications from './notifications';
import systemStatus from './system_status';
import uploader from './uploader';
// eslint-disable-next-line import/no-cycle
import model from './model';
import help from './help';
import { VuexUserState } from '@/store/user/types';
import USER_VXC from '@/store/user/constants';
import SYSTEM_STATUS_VXC from '@/store/system_status/constants';
import NOTIFICATIONS_VXC from '@/store/notifications/constants';
import UPLOADER_VXC from '@/store/uploader/constants';

Vue.use(Vuex);

// A Vuex instance is created by combining the state, getters, actions,
// and mutations.
//
// Strict mode runs a synchronous deep watcher on the state tree for detecting
// inappropriate mutations, and it can be quite expensive when you make large
// amount of mutations to the state, so, we do not use it in production mode.

const storeOptions: StoreOptions<RootState> = {
  state: {
    version: '1.0.0',
  },
  modules: {
    user,
    notifications,
    systemStatus,
    uploader,
    sidebar,
    model,
    help,
  },
  strict: process.env.NODE_ENV !== 'production',
};

export const store = new Vuex.Store<RootState>(storeOptions);

export function vxcFp(vxc: VXC, vxcRelativeCommandPath: string) {
  return `${vxc.MODULE}/${vxcRelativeCommandPath}`;
}

store.watch(
  () : boolean => ((store.state as any).user as VuexUserState).isLoggedIn,
  async (isLoggedIn: boolean) => {
    if (!isLoggedIn) {
      store.commit(vxcFp(SYSTEM_STATUS_VXC, SYSTEM_STATUS_VXC.SET.ALL_STATUSES_UNKNOWN));
      store.commit(vxcFp(NOTIFICATIONS_VXC, NOTIFICATIONS_VXC.SET.REMOVE_ALL_NOTIFICATIONS_FOR_UI));
      store.commit(vxcFp(UPLOADER_VXC, UPLOADER_VXC.SET.REMOVE_ALL_ENTRIES));
    }
  },
);

// eslint-disable-next-line no-unused-vars
export function newWatchLogin(callback: (x:boolean)=>void) {
  return store.watch(
    () => store.getters[vxcFp(USER_VXC, USER_VXC.GET.USER)].isLoggedIn,
    callback,
  );
}

export const abortWatcher = (
  moduleName: string,
  stateFieldAbortFlagPath: string,
  abortFlagMutationName: string,
  type: string = 'Action',
  preAbort?: Function,
) => {
  const controller = new AbortController();
  const mutation = `${moduleName}/${abortFlagMutationName}`;

  store.commit(mutation, false);
  const start = Date.now();

  const getter = (state: any) => {
    const moduleState = state[moduleName];
    const abortFlag: boolean = moduleState[stateFieldAbortFlagPath];
    return abortFlag;
  };

  const unwatch = store.watch(
    getter,
    async (abortFlag: boolean) => {
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
