import { VuexUserState } from '@/store/user/types';
import isNil from 'lodash/isNil';
import { UserDto } from './types';
import isMock from '@/dataConfig';
import { setLoggedIn, setLoggedOut } from '@/store/user/userHelper';
import { USER_ADMIN_NAME } from '@/store/user/constants';

const totalUsers: number = 10;

class UserMockData {
  nxtUserId = 1;

  users: UserDto[] = [];

  oldest: number = Date.now() - (1000 * 60 * totalUsers);

  constructor() {
    // We need to be logged in for other mock apps to function
    if (isMock.usermaint) {
      this.users.push({
        userId: this.nxtUserId,
        name: USER_ADMIN_NAME,
        fullname: 'The Admin',
        createDate: new Date(this.oldest).toISOString(),
        lastLoginDate: new Date().toISOString(),
        loggedInCount: 1,
      });
      this.nxtUserId += 1;
      this.setLoggedInUser(this.users[0]);
      this.createUsers();
    }
  }

  setLoggedInUser = (user: UserDto) => {
    if (isNil(user.userId)) {
      throw Error('user.userId is not defined');
    }
    const loggedInData: VuexUserState = {
      isLoggedIn: true,
      userId: user.userId,
      name: user.name,
      isAdminRole: user.name === 'admin',
    };
    setLoggedIn(loggedInData);
  };

  createUsers() {
    let startTime = this.oldest;

    for (let i = 0; i < totalUsers - 1; i += 1) {
      const userSuffix = i === 0 ? '' : i + 1;

      this.users.push({
        userId: this.nxtUserId,
        name: `user${userSuffix}`,
        fullname: `The User${userSuffix}`,
        createDate: new Date(startTime).toISOString(),
        lastLoginDate: new Date(startTime).toISOString(),
        loggedInCount: i,
      });
      this.nxtUserId += 1;
      startTime += (1000 * 60);
    }
  }

// Login the user and create the mock user if it does not already exist
  login(userPwd: { name: string, password: string }) {
    if (!(userPwd && userPwd.name && userPwd.password)) {
      throw Error('userPwd must contain both a user and a password');
    }

    this.users.forEach((u) => {
      u.loggedInCount = 0;
    });
    this.logout();

    let userDto: UserDto | undefined = this.users.find(u => u.name === userPwd.name);
    if (!userDto) {
      userDto = {
        name: userPwd.name,
        userId: this.nxtUserId,
        fullname: userPwd.name,
        createDate: new Date(this.oldest).toISOString(),
        lastLoginDate: new Date().toISOString(),
        loggedInCount: 1,
      };
      this.users.push(userDto);
      this.nxtUserId += 1;
    } else {
      userDto.loggedInCount = 1;
    }

    userDto.lastLoginDate = new Date().toISOString();

    this.setLoggedInUser(userDto);
  }

  logout() {
    setLoggedOut();
  }

  getUsers(): UserDto[] {
    return [...this.users];
  }

  addUser(user: UserDto) {
    if (this.users.find(u => u.name === user.name)) {
      throw Error('User already exists');
    }
    this.users.push({ ...user, userId: this.nxtUserId });
    this.nxtUserId += 1;
  }

  updateUser(user: UserDto) {
    const ix = this.users.findIndex(u => u.userId === user.userId);
    if (ix !== -1) {
      this.users[ix] = { ...this.users[ix], ...user };
    } else {
      throw Error('User not found');
    }
  }

  deleteUser(userId: number) {
    const ix = this.users.findIndex(u => u.userId === userId);
    if (ix !== -1) {
      this.users.splice(ix, 1);
    } else {
      throw Error('User not found');
    }
  }
}

export const userMockData: UserMockData = new UserMockData();
