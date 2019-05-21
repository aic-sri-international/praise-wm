import isMock from '@/dataConfig';
import { http } from '@/services/http';
import { userMockData } from '@/components/usermaint/mock_data';

async function login(userPwd: { name: string, password: string }): Promise<any> {
  if (isMock.login) {
    try {
      userMockData.login(userPwd);
      return Promise.resolve();
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(err);
      return Promise.reject();
    }
  }

  await http.login(userPwd);
  return Promise.resolve();
}

function logout(): void {
  if (isMock.login) {
    try {
      userMockData.logout();
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(err);
    }
    return;
  }

  http.logout();
}

export { login, logout };
