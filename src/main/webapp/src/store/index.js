// @flow

export { store } from './store';
export { default as USER_VXC } from './user/constants';
export { default as SIDEBAR_VXC } from './sidebar/constants';
export { default as NOTIFICATIONS_VXC } from './notifications/constants';
export { default as SYSTEM_STATUS_VXC } from './system_status/constants';
export { default as UPLOADER_VXC } from './uploader/constants';
export { default as MODEL_VXC } from './model/constants';
export { default as HELP_VXC } from './help/constants';

export { sideBarStyles } from '@/store/sidebar/store';

export type VXC = {
  MODULE: string,
  GET: Object,
  SET: Object,
}

export function vxcFp(vxc: VXC, vxcRelativeCommandPath: string) {
  return `${vxc.MODULE}/${vxcRelativeCommandPath}`;
}

export { newWatchLogin } from './user/store';
