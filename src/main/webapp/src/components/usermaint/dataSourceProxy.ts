import isMock from '@/dataConfig';
import { http, toApiUrl, toAdminUrl } from '@/services/http';
import { UserDto } from './types';
import {
  userMockData,
} from '@/components/usermaint/mock_data';

async function fetchUserDtos(): Promise<UserDto[]> {
  let result: any;

  if (isMock.usermaint) {
    try {
      result = userMockData.getUsers();
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
      result = userMockData.addUser(user);
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
      result = userMockData.updateUser(user);
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
      result = userMockData.deleteUser(userId);
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
