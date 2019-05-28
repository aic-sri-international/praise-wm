import { VuexPreferencesState } from './types';
import { Module } from 'vuex';
import { RootState } from '@/store/types';
import {
  getLocalShowMapPopupOnMouseOver,
  setLocalShowMapPopupOnMouseOver,
} from '@/store/preferences/localStorage';

const mutations = {
  setShowPreferences(state: VuexPreferencesState, showPreferences: boolean) {
    state.showPreferences = showPreferences;
  },
  setMapPopupOnMouseOver(state: VuexPreferencesState, showMapPopupOnMouseOver: boolean) {
    state.showMapPopupOnMouseOver = showMapPopupOnMouseOver;
    setLocalShowMapPopupOnMouseOver(showMapPopupOnMouseOver);
  },
};

const state: VuexPreferencesState = {
  showPreferences: false,
  showMapPopupOnMouseOver: getLocalShowMapPopupOnMouseOver(),
};

const module: Module<VuexPreferencesState, RootState> = {
  namespaced: true,
  state,
  mutations,
};

export default module;
