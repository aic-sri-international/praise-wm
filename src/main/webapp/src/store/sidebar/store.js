// @flow
import type {
  SideBarStyle,
  VuexSideBarState,
  VuexSideBarStore,
} from './types';
import SC from './constants';

export const sideBarStyles = {
  expanded: {
    paddingLeft: '190px',
  },
  collapsed: {
    paddingLeft: '90px',
  },
};

/* eslint-disable no-param-reassign */
const mutations = {
  [SC.SET.TOGGLE_SIDEBAR_COLLAPSE](state: VuexSideBarState) {
    state.isCollapsed = !state.isCollapsed;
    if (state.isCollapsed) {
      state.style = { ...sideBarStyles.collapsed };
    } else {
      state.style = { ...sideBarStyles.expanded };
    }
  },
};

const getters = {
  [SC.GET.IS_SIDEBAR_COLLAPSED]:
      (state: VuexSideBarState): boolean => state.isCollapsed,
  [SC.GET.SIDEBAR_STYLE]:
      (state: VuexSideBarState): SideBarStyle => state.style,
};

const store: VuexSideBarStore = {
  namespaced: true,
  state: {
    isCollapsed: true,
    style: { ...sideBarStyles.collapsed },
  },
  getters,
  mutations,
};

export default store;
