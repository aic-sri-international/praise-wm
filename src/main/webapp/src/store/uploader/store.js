// @flow
import mutations from './mutations';
import actions from './actions';
import UP from './constants';
import type {
  UploadEntry,
  VuexUploaderStore,
  VuexUploaderState,
} from './types';

const getters = {
  [UP.GET.QUEUE]: (state: VuexUploaderState): UploadEntry[] => state.queue,
};


const store: VuexUploaderStore = {
  namespaced: true,
  state: {
    queue: [],
  },
  getters,
  mutations,
  actions,
};

export default store;
