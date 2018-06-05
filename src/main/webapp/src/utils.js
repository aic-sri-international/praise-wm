// @flow

import Vue from 'vue';
import { serverTimeDeltaInMillis } from './services/http';

function getDate() {
  return new Date(Date.now() + serverTimeDeltaInMillis());
}

// A flow-typed version of Object.entries; until there's a better solution
// https://github.com/facebook/flow/issues/2174
function objectEntries<T>(obj : { [string]: T }) : Array<[string, T]> {
  const keys : string[] = Object.keys(obj);
  return keys.map(key => [key, obj[key]]);
}

type FileInfo = {
  filename: string,
  basename: string,
  extension: string,
  text: string,
};

function readTextFile(file: File) : Promise<FileInfo> {
  return new Promise((resolve, reject) => {
    if (!file) {
      reject(new Error('file is undefined'));
      return;
    }

    const fr = new FileReader();
    const filename = file.name;
    let basename = filename;
    let extension = '';

    const ix = filename.lastIndexOf('.');
    if (ix !== -1) {
      basename = filename.slice(0, ix);
      extension = filename.slice(ix + 1);
    }
    fr.onload = () => {
      const text: any = fr.result;
      const result : FileInfo = {
        filename,
        basename,
        extension,
        text,
      };
      resolve(result);
    };
    fr.readAsText(file);
  });
}

function cnvFormatDate(v: mixed) : string {
  if (typeof v === 'string' || v instanceof Date) {
    return Vue.options.filters.formatDateTime(v);
  }
  if (typeof v === 'number') {
    return Vue.options.filters.formatDateTime(new Date(v));
  }
  if (!v) {
    return '';
  }
  throw Error(`value not a string: '${(v:any)}'`);
}

function downloadFile(objectOrObjectUrl: Object | string, filename: string) {
  let blob;
  if (typeof objectOrObjectUrl === 'string') {
    blob = new Blob([objectOrObjectUrl], { type: 'application/text' });
  } else {
    const jsonString = JSON.stringify(objectOrObjectUrl, null, 2);
    blob = new Blob([jsonString], { type: 'application/json' });
  }
  const objectUrl = URL.createObjectURL(blob);

  const elem = document.createElement('a');
  elem.href = objectUrl;
  elem.download = filename;
  const docBody = document.body;
  if (docBody) {
    docBody.appendChild(elem);
  } else {
    throw Error('download method requires the document to have a body');
  }
  setTimeout(() => {
    elem.click();
    docBody.removeChild(elem);
  }, 66);
}

export {
  getDate,
  objectEntries,
  readTextFile,
  downloadFile,
  cnvFormatDate,
};
