export type SideBarStyle = {
  paddingLeft: string;
}

export interface VuexSideBarState {
  isSideBarCollapsed: boolean;
  sideBarStyle: SideBarStyle;
}
