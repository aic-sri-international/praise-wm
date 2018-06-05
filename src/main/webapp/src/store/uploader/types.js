// @flow

const entryStatuses = {
  UPLOADING: 'UPLOADING',
  WAITING: 'WAITING',
  COMPLETED: 'COMPLETED',
  ERROR: 'ERROR',
};

// eslint-disable-next-line no-undef
type UploadEntryStatus = $Keys<typeof entryStatuses>;

type UploadEntry = {
  id: number,
  queuedDate: Date,
  startUploadDate?: Date,
  endUploadDate?: Date,
  status: UploadEntryStatus,
  file: File,
}

type VuexUploaderState = {
  queue: Array<UploadEntry>,
};

type VuexUploaderStore = {
  namespaced: boolean,
  state: VuexUploaderState,
  getters: Object,
  mutations: Object,
};

export type {
  UploadEntry,
  UploadEntryStatus,
  VuexUploaderState,
  VuexUploaderStore,
};

export default entryStatuses;
