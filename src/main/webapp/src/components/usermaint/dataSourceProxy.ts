import isMock from '@/dataConfig';
import { http, toApiUrl, toAdminUrl } from '@/services/http';
import { UserDto } from './types';
import {
  getUsers,
  addUser as addMockUser,
  updateUser as updateMockUser,
  deleteUser as deleteMockUser,
} from './mock_data';

async function fetchUserDtos(): Promise<UserDto[]> {
  let result: any;

  if (isMock.usermaint) {
    try {
      result = getUsers();
      return Promise.resolve(result);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(err);
      return Promise.reject();
    }
  }

  result = await http.get(toApiUrl('users'));
  return Promise.resolve(result);
}

async function addUser(user: UserDto): Promise<any> {
  let result;

  if (isMock.usermaint) {
    try {
      result = addMockUser(user);
      return Promise.resolve(result);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(err);
      return Promise.reject();
    }
  }

  result = await http.post(toAdminUrl('users'), user);
  return Promise.resolve(result);
}

async function updateUser(user: UserDto): Promise<any> {
  let result;

  if (isMock.usermaint) {
    try {
      result = updateMockUser(user);
      return Promise.resolve(result);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(err);
      return Promise.reject();
    }
  }

  result = await http.put(toAdminUrl('users'), user);
  return Promise.resolve(result);
}

async function deleteUser(userId: number): Promise<any> {
  let result;

  if (isMock.usermaint) {
    try {
      result = deleteMockUser(userId);
      return Promise.resolve(result);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(err);
      return Promise.reject();
    }
  }

  result = http.delete(toAdminUrl(`users/${userId}`));
  return Promise.resolve(result);
}

export {
 fetchUserDtos, addUser, updateUser, deleteUser,
};
