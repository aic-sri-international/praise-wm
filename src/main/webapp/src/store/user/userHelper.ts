// This helper module is for use by modules that do not otherwise access Vue
import { VuexUserState } from '@/store/user/types';
import { USER_MODULE_NAME } from '@/store/user/constants';
import { getStore } from '@/store/store';

export const isLoggedIn = () => {
  const rootState: any = getStore().state;
  return (rootState[USER_MODULE_NAME] as VuexUserState).isLoggedIn;
};

export const setLoggedIn = (userData: VuexUserState)
  : void => getStore().commit(`${USER_MODULE_NAME}/setLoggedIn`, userData);

export const setLoggedOut = () => getStore().commit(`${USER_MODULE_NAME}/setLoggedOut`);
