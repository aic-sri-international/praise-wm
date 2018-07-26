// @flow

type VuexHelpState = {
  showHelp: boolean,
};

type VuexHelpStore = {
  namespaced: boolean,
  state: VuexHelpState,
  getters: any,
  mutations: any,
};

export type {
  VuexHelpState,
  VuexHelpStore,
};
