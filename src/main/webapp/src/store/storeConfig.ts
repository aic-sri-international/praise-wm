import Vue from 'vue';
import Vuex, { StoreOptions } from 'vuex';
import { RootState } from './types';
import user from './user';
import sidebar from './sidebar';
import notifications from './notifications';
import systemStatus from './system_status';
import uploader from './uploader';
import model from './model';
import featureCollection from './feature_collection';
import help from './help';
import { createStore } from '@/store/store';

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
    featureCollection,
    help,
  },
  strict: process.env.NODE_ENV !== 'production',
};

createStore(storeOptions);
