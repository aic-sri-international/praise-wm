import { MutationTree } from 'vuex';
import { UploadEntry, VuexUploaderState } from './types';

const mutations: MutationTree<VuexUploaderState> = {
  addUploaderEntry(state: VuexUploaderState, entry: UploadEntry) {
    state.queue.push(entry);
  },
  updateUploaderEntry(state: VuexUploaderState, entry: UploadEntry) {
    const arr: UploadEntry[] = state.queue;
    const found: UploadEntry | undefined = arr.find(e => e.id === entry.id);
    if (!found) {
      throw Error(`expected entry not in queue: ${JSON.stringify(entry)}`);
    }
    found.startUploadDate = entry.startUploadDate;
    found.endUploadDate = entry.endUploadDate;
    found.status = entry.status;
  },
  removeUploaderEntry(state: VuexUploaderState, id: number) {
    const arr = state.queue;
    const ix = arr.findIndex(e => e.id === id);
    if (ix !== -1) {
      arr.splice(ix, 1);
    }
  },
  removeAllUploaderEntries(state: VuexUploaderState) {
    state.queue = [];
  },
};

export default mutations;
