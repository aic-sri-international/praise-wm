// @flow

import Vue from 'vue';
import {
  getDate,
  downloadFile,
} from './utils';

const showToastError = (text) => {
  const toastOpts = {
    theme: 'primary',
    duration: 5000,
    position: 'bottom-center',
    fullWidth: true,
    action: {
      text: 'Close',
      onClick: (e, t) => {
        t.goAway(0);
      },
    },
    icon: {
      name: ' ',
    },
  };
  Vue.toasted.error(text, toastOpts);
};

Vue.prototype.$$ = {
  getDate,
  showToastError,
  downloadFile,
};

