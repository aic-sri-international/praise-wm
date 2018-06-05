// @flow

type VuexUserState = {
  isLoggedIn: boolean,
  userId: number,
  name: string,
  isAdminRole: boolean,
};

type VuexUserStore = {
  namespaced: boolean,
  state: VuexUserState,
  getters: any,
  mutations: any,
};

export type {
  VuexUserState,
  VuexUserStore,
};
