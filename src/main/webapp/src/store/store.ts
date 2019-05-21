import Vuex, { Store, StoreOptions } from 'vuex';
import { RootState } from '@/store/types';
import { oneLine } from 'common-tags';

let store: Store<RootState> = (null as any);

export const getStore = (): Store<RootState> => {
  if (!store) {
    throw Error(oneLine`createStore has not been called. 
    Make sure that '@/store/storeConfig' is imported at the top of main.ts`);
  }
  return store;
};

export function createStore(storeOptions: StoreOptions<RootState>) {
  if (store) {
    throw Error('createStore cannot be called more than once');
  }
  store = new Vuex.Store<RootState>(storeOptions);
}
