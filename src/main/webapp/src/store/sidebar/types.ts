export type SideBarStyle = {
  paddingLeft: string;
}

export interface VuexSideBarState {
  isCollapsed: boolean;
  style: SideBarStyle;
}
