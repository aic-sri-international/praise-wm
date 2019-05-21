import { Module } from 'vuex';
import { VuexSideBarState } from './types';
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
    state.isSideBarCollapsed = !state.isSideBarCollapsed;
    if (state.isSideBarCollapsed) {
      state.sideBarStyle = { ...sideBarStyles.collapsed };
    } else {
      state.sideBarStyle = { ...sideBarStyles.expanded };
    }
  },
};

const state: VuexSideBarState = {
  isSideBarCollapsed: true,
  sideBarStyle: { ...sideBarStyles.collapsed },
};

export const module: Module<VuexSideBarState, RootState> = {
  namespaced: true,
  state,
  mutations,
};


export default module;
