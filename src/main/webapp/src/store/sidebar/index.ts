import { Module } from 'vuex';
import {
  SideBarStyle,
  VuexSideBarState,
} from './types';
import SC from './constants';
import { RootState } from '@/store/types';

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
