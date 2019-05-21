import uploadFile from './dataSourceProxy';
import { UploadEntry, UploadEntryStatus, VuexUploaderState } from './types';
import { ActionTree, Commit } from 'vuex';
import { RootState } from '@/store/types';

let entryId: number = 0;

const uploadNext = async (commit: Commit, state: VuexUploaderState): Promise<any> => {
  let entry: UploadEntry | undefined;
  entry = state.queue.find(e => e.status === UploadEntryStatus.UPLOADING);
  if (entry) {
    return;
  }

  entry = state.queue.find(e => e.status === UploadEntryStatus.WAITING);
  if (!entry) {
    return;
  }

  let update: UploadEntry;

  try {
    update = {
      ...entry,
      status: UploadEntryStatus.UPLOADING,
      startUploadDate: new Date(),
    };
    commit('updateUploaderEntry', update);

    await uploadFile(entry.file);

    update = {
      ...entry,
      status: UploadEntryStatus.COMPLETED,
      endUploadDate: new Date(),
    };
  } catch (e) {
    update = {
      ...entry,
      status: UploadEntryStatus.ERROR,
      endUploadDate: new Date(),
    };
  }
  commit('updateUploaderEntry', update);

  await uploadNext(commit, state);
};

const actions: ActionTree<VuexUploaderState, RootState> = {
  async uploadFile({ commit, state }, file: File): Promise<any> {
    entryId += 1;
    const newEntry: UploadEntry = {
      id: entryId,
      queuedDate: new Date(),
      startUploadDate: undefined,
      endUploadDate: undefined,
      status: UploadEntryStatus.WAITING,
      file,
    };

    commit('addUploaderEntry', newEntry);
    return uploadNext(commit, state);
  },
};

export default actions;
