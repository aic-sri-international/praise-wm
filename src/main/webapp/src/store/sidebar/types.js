// @flow

type SideBarStyle = {
  paddingLeft: string,
}

type VuexSideBarState = {
  isCollapsed: boolean,
  style: SideBarStyle,
};

type VuexSideBarStore = {
  namespaced: boolean,
  state: VuexSideBarState,
  getters: any,
  mutations: any,
};

export type {
  SideBarStyle,
  VuexSideBarState,
  VuexSideBarStore,
};
