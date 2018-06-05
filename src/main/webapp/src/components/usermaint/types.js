// @flow

export type UserDto = {
  userId: number,
  name: string,
  fullname: string,
  createDate: string,
  lastLoginDate?: string,
  loggedInCount: number,
};
