// @flow

const UPLOADER_VXC = {
  MODULE: 'uploader',
  GET: {
    QUEUE: 'uploaderQueue',
  },
  SET: {
    ADD_ENTRY: 'addUploaderEntry',
    UPDATE_ENTRY: 'updateUploaderEntry',
    REMOVE_ENTRY: 'removeUploaderEntry',
    REMOVE_ALL_ENTRIES: 'removeAllUploaderEntries',
  },
  ACTION: {
    UPLOAD_FILE: 'uploadFile',
  },
};

export default UPLOADER_VXC;
