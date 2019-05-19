import { Module } from 'vuex';
import {
  SideBarStyle,
  VuexSideBarState,
} from './types';
import { RootState } from '@/store/types';

export const sideBarStyles = {
  expanded: {
    paddingLeft: '190px',
  },
  collapsed: {
    paddingLeft: '90px',
  },
};

const mutations = {
  toggleSideBarCollapse(state: VuexSideBarState) {
    state.isCollapsed = !state.isCollapsed;
    if (state.isCollapsed) {
      state.style = { ...sideBarStyles.collapsed };
    } else {
      state.style = { ...sideBarStyles.expanded };
    }
  },
};

const getters = {
  isSideBarCollapsed:
      (state: VuexSideBarState): boolean => state.isCollapsed,
  sideBarStyle:
      (state: VuexSideBarState): SideBarStyle => state.style,
};

const state: VuexSideBarState = {
  isCollapsed: true,
  style: { ...sideBarStyles.collapsed },
};

export const module: Module<VuexSideBarState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
};


export default module;
