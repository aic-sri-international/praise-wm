import Vue from 'vue';
import {
 downloadFile, getDate, insertWordBreaks, IUtils,
} from './utils';

const showToastError = (text: string): void => {
  const toastOpts = {
    theme: 'toasted-primary',
    duration: 5000,
    position: 'bottom-center',
    fullWidth: true,
    action: {
      text: 'Close',
      onClick: (e: any, t: any) => {
        t.goAway(0);
      },
    },
    icon: {
      name: ' ',
    },
  };

  // @ts-ignore
  Vue.toasted.error(text, toastOpts);
};

Vue.prototype.$$ = {
  downloadFile,
  getDate,
  insertWordBreaks,
  showToastError,
};

export interface Vue$$ extends IUtils {
  showToastError(text: string): void;
}
