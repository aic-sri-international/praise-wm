// @flow

import UP from './constants';
import type { UploadEntry } from './types';
import entryStatuses from './types';
import uploadFile from './dataSourceProxy';

let entryId : number = 0;

const uploadNext = async (commit, state) => {
  let entry: UploadEntry;
  entry = state.queue.find(e => e.status === entryStatuses.UPLOADING);
  if (entry) {
    return;
  }

  entry = state.queue.find(e => e.status === entryStatuses.WAITING);
  if (!entry) {
    return;
  }

  let update : UploadEntry;

  try {
    update = {
      ...entry,
      status: entryStatuses.UPLOADING,
      startUploadDate: new Date(),
    };
    commit(UP.SET.UPDATE_ENTRY, update);

    await uploadFile(entry.file);

    update = {
      ...entry,
      status: entryStatuses.COMPLETED,
      endUploadDate: new Date(),
    };
  } catch (e) {
    update = {
      ...entry,
      status: entryStatuses.ERROR,
      endUploadDate: new Date(),
    };
  }
  commit(UP.SET.UPDATE_ENTRY, update);

  uploadNext(commit, state);
};

export default {
  async [UP.ACTION.UPLOAD_FILE]({ commit, state }, file: File) {
    entryId += 1;
    const newEntry: UploadEntry = {
      id: entryId,
      queuedDate: new Date(),
      startUploadDate: undefined,
      endUploadDate: undefined,
      status: entryStatuses.WAITING,
      file,
    };

    commit(UP.SET.ADD_ENTRY, newEntry);
    uploadNext(commit, state);
  },
};
