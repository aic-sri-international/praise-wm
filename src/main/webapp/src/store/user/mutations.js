// @flow
/* eslint-disable no-param-reassign */
import type { VuexUserState } from './types';
import UC from './constants';

export default {
  [UC.SET.LOGGED_IN](state, userData: VuexUserState) {
    Object.assign(state, userData);
  },
  [UC.SET.LOGGED_OUT](state) {
    state.isLoggedIn = false;
  },
};
