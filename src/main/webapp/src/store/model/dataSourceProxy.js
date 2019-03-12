// @flow
import isMock from '@/dataConfig';
import { http, toApiUrl } from '@/services/http';

async function uploadFile(file: File): Promise<any> {
  let result;

  const fd = new FormData();
  fd.append(file.name, file, file.name);

  // eslint-disable-next-line no-unused-vars
  if (isMock.upload) {
    try {
      result = '';
      return Promise.resolve(result);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(err);
      return Promise.reject();
    }
  }

  result = await http.post(toApiUrl('upload'), fd);
  return Promise.resolve(result);
}

export default uploadFile;

