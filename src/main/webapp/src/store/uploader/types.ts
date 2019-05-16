export enum UploadEntryStatus {
  UPLOADING = 'UPLOADING',
  WAITING = 'WAITING',
  COMPLETED = 'COMPLETED',
  ERROR = 'ERROR',
}

export interface UploadEntry {
  id: number;
  queuedDate: Date;
  startUploadDate?: Date;
  endUploadDate?: Date;
  status: UploadEntryStatus;
  file: File;
}

export interface VuexUploaderState {
  queue: Array<UploadEntry>;
}
