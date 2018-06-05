// @flow
/* eslint-disable no-param-reassign */
import UP from './constants';
import type {
  UploadEntry,
  VuexUploaderState,
} from './types';

export default {
  [UP.SET.ADD_ENTRY](state: VuexUploaderState, entry: UploadEntry) {
    state.queue.push(entry);
  },
  [UP.SET.UPDATE_ENTRY](state: VuexUploaderState, entry: UploadEntry) {
    const arr: UploadEntry[] = state.queue;
    const found: ?UploadEntry = arr.find(e => e.id === entry.id);
    if (!found) {
      throw Error(`expected entry not in queue: ${JSON.stringify(entry)}`);
    }
    found.startUploadDate = entry.startUploadDate;
    found.endUploadDate = entry.endUploadDate;
    found.status = entry.status;
  },
  [UP.SET.REMOVE_ENTRY](state: VuexUploaderState, id: number) {
    const arr = state.queue;
    const ix = arr.findIndex(e => e.id === id);
    if (ix !== -1) {
      arr.splice(ix, 1);
    }
  },
  [UP.SET.REMOVE_ALL_ENTRIES](state: VuexUploaderState) {
    state.queue = [];
  },
};
