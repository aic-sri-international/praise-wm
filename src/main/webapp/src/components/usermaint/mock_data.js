// @flow

import isMock from '@/dataConfig';
import { store, USER_VXC as UC, vxcFp } from '@/store';
import type { VuexUserState } from '@/store/user/types';
import type { UserDto } from './types';

const totalUsers : number = 10;
let nxtUserId = 1;
const users: UserDto[] = [];
const oldest: number = Date.now() - (1000 * 60 * totalUsers);

const setLoggedInUser = (user: UserDto) => {
  const loggedInData: VuexUserState = {
    isLoggedIn: true,
    userId: user.userId,
    name: user.name,
    isAdminRole: user.name === 'admin',
  };

  store.commit(vxcFp(UC, UC.SET.LOGGED_IN), loggedInData);
};

function createUsers() {
  let startTime = oldest;

  for (let i = 0; i < totalUsers - 1; i += 1) {
    users.push({
      userId: nxtUserId,
      name: `user${i === 0 ? '' : i + 1}`,
      fullname: `The User${nxtUserId}`,
      createDate: new Date(startTime).toISOString(),
      lastLoginDate: new Date(startTime).toISOString(),
      loggedInCount: i,
    });
    nxtUserId += 1;
    startTime += (1000 * 60);
  }
}

// Login the user and create the mock user if it does not already exist
function login(userPwd: { name: string, password: string }) {
  if (!(userPwd && userPwd.name && userPwd.password)) {
    throw Error('userPwd must contain both a user and a password');
  }

  // eslint-disable-next-line no-param-reassign
  users.forEach((u) => { u.loggedInCount = 0; });
  store.commit(vxcFp(UC, UC.SET.LOGGED_OUT));

  let userDto: ?UserDto = users.find(u => u.name === userPwd.name);
  if (!userDto) {
    userDto = {
      name: userPwd.name,
      userId: nxtUserId,
      fullname: userPwd.name,
      createDate: new Date(oldest).toISOString(),
      lastLoginDate: new Date().toISOString(),
      loggedInCount: 1,
    };
    users.push(userDto);
    nxtUserId += 1;
  } else {
    userDto.loggedInCount = 1;
  }

  userDto.lastLoginDate = new Date().toISOString();

  setLoggedInUser(userDto);
}

function logout() {
  store.commit(vxcFp(UC, UC.SET.LOGGED_OUT));
}

// We need to be logged in for other mock apps to function
if (isMock.usermaint && !store.getters[vxcFp(UC, UC.GET.IS_LOGGED_IN)]) {
  users.push({
    userId: nxtUserId,
    name: UC.ADMIN_NAME,
    fullname: 'The Admin',
    createDate: new Date(oldest).toISOString(),
    lastLoginDate: new Date().toISOString(),
    loggedInCount: 1,
  });
  nxtUserId += 1;
  setLoggedInUser(users[0]);
  createUsers();
}

function getUsers(): UserDto[] {
  return [...users];
}

function addUser(user: UserDto) {
  if (users.find(u => u.name === user.name)) {
    throw Error('User already exists');
  }
  users.push({ ...user, userId: nxtUserId });
  nxtUserId += 1;
}

function updateUser(user: UserDto) {
  const ix = users.findIndex(u => u.userId === user.userId);
  if (ix !== -1) {
    users[ix] = { ...users[ix], ...user };
  } else {
    throw Error('User not found');
  }
}

function deleteUser(userId: number) {
  const ix = users.findIndex(u => u.userId === userId);
  if (ix !== -1) {
    users.splice(ix, 1);
  } else {
    throw Error('User not found');
  }
}

export { login, logout, getUsers, addUser, updateUser, deleteUser };
