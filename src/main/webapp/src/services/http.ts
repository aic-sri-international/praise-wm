// eslint-disable-next-line import/no-cycle
import {
  store,
  USER_VXC as U_VXC,
  NOTIFICATIONS_VXC as N_VXC,
  vxcFp,
} from '@/store';

import { VuexUserState } from '@/store/user/types';
// eslint-disable-next-line import/no-cycle
import {
  getDate,
  downloadFile,
} from '@/utils';
import { MessageLevel } from '@/services/ws_notifications/types';

type FetchDataParams = {
  request: Request,
  isLogin?: boolean,
  isBlob?: boolean,
}

type BlobResponse = {
  blob: Blob,
  filename: string
}

type User = {
  userId: number,
  name: string,
};

type LoginResponse = {
  user: User,
  isAdminRole: boolean,
  localPort: number,
  serverTime: string,
  wsReconnectInterval: number,
  wsMaxReconnectAttempts: number,
  wsClientInactivityTimeout: number,
}

interface ErrorResponseBody {
  userMessage?: string;
  techMessage?: string;
  displayTechMessage?: boolean;
  displayHttpStatus?: boolean;
  message?: any,
}

interface ErrorResponseMessage {
  userMessage: string;
  techMessage: string;
  displayTechMessage: boolean;
  displayHttpStatus: boolean;
}

const toErrorResponseMessage = (body: ErrorResponseBody) : ErrorResponseMessage => {
  if (body.userMessage) {
    return {
      userMessage: body.userMessage,
      techMessage: body.techMessage || '',
      displayTechMessage: body.displayTechMessage || false,
      displayHttpStatus: body.displayHttpStatus || false,
    };
  }

  let userMessage = '';

  if (body.message && typeof body.message === 'string') {
    // The Fetch API using the Chrome browser returns this message if
    // it cannot connect to the server.
    // Return a message that is more meaningful to the user.
    if (body.message.toLowerCase() === 'failed to fetch') {
      userMessage = 'Cannot connect to the server';
    } else {
      userMessage = body.message;
    }
  }

  return {
    userMessage, techMessage: '', displayTechMessage: false, displayHttpStatus: true,
  };
};


const HTTP_STATUS_NO_CONTENT = 204;

// The location header contains a path that can be passed to
// the http.get method to obtain the object that was created
// (or info on it - exactly what gets returned from the server is implementation
// dependent).
const HTTP_STATUS_CREATED = 201;

let secSessionId: string = '';
let timeDeltaInMillis : number = 0;
let reconnectInternalInMillis = 0;
let maxReconnectAttempts = 0;
let clientInactivityTimeoutInMillis = 0;

export function serverTimeDeltaInMillis() {
  return timeDeltaInMillis;
}

export function getWsClientInactivityTimeoutInMillis() : number {
  return clientInactivityTimeoutInMillis;
}

export function getWsReconnectInterval() : number {
  return reconnectInternalInMillis;
}

export function getWsMaxReconnectAttempts() : number {
  return maxReconnectAttempts;
}

function appendSessionIdQuery(url: string) {
  return `${url}?SEC_SESSION_ID=${secSessionId}`;
}

export function getUrlForWebsocketEndpoint(endpoint : string) : string {
  const loc = window.location;
  const protocol : string = loc.protocol === 'http:' ? 'ws:' : 'wss:';
  return appendSessionIdQuery(`${protocol}//${loc.hostname}:${loc.port}/ws/${endpoint}`);
}

function getHeaderValue(headers: Headers, key: string) : string {
  try {
    const value : string | null = headers.get(key);
    if (value !== null) {
      return value;
    }

    // noinspection ExceptionCaughtLocallyJS eslint-disable-next-line semi
    throw Error('value not set');
  } catch (err) {
    throw Error(`${key} not found in response header: ${err.message}`);
  }
}

function getFilenameFromHeader(headers: Headers) : string {
  const key = 'Content-Disposition';
  const fnmPrefix = 'filename=';
  const headerValue = getHeaderValue(headers, key);
  const arr = headerValue.split(fnmPrefix);
  if (arr && arr.length === 2) {
    return arr[1];
  }
  throw Error(`Response header for key ${key} `
              + `did not include '${fnmPrefix}' with value: ${headerValue}'`);
}

function saveLoginHeaders(headers : Headers) : void {
  secSessionId = getHeaderValue(headers, 'SEC_SESSION_ID');
}

type HeadersAndBody = {
  headers: Headers,
  body?: any,
};

function getHeadersAndBody(body?: any) : HeadersAndBody {
  const headers : Headers = new Headers();
  let bodyData;
  if (typeof body === 'object' && !(body instanceof FormData)) {
    bodyData = JSON.stringify(body);
    headers.append('Content-Type', 'application/json');
  } else {
    bodyData = body;
    if (typeof bodyData === 'string') {
      headers.append('Content-Type', 'text/plain');
    }
  }

  headers.append('SEC_SESSION_ID', secSessionId);

  if (bodyData !== undefined && bodyData !== null) {
    return { headers, body: bodyData };
  }

  return { headers };
}

const buildMessage = (
  ex: ErrorResponseMessage,
  allInfo: boolean,
  response: Response | null,
): string => {
  let msg = ex.userMessage;

  if ((ex.displayTechMessage || allInfo) && ex.techMessage) {
    if (msg) {
      msg += ': ';
    }
    msg += ex.techMessage;
  }

  if ((ex.displayHttpStatus || allInfo) && response) {
    if (msg) {
      msg += ': ';
    }

    msg += `HTTP Status (${response.status}): ${response.statusText}`;
  }

  return msg;
};

async function fetchData(params: FetchDataParams) : Promise<Object> {
  let response: Response | null = null;
  let errRespEx: ErrorResponseMessage;

  try {
    response = await fetch(params.request);
    let body: any = { };
    let blob: Blob | undefined;

    if (response.status === HTTP_STATUS_CREATED) {
      body.location = getHeaderValue(response.headers, 'LOCATION');
    } else if (response.status !== HTTP_STATUS_NO_CONTENT) {
      try {
        if (params.isBlob && response.ok) {
          blob = await response.blob();
        } else {
          body = await response.json();
        }
      } catch (e) {
        if (response.ok) {
          // Many errors (504, etc) will not contain a body message,
          // so, don't bother to display a json parse error.
          // eslint-disable-next-line no-console
          console.error(`Error ${params.isBlob ? 'loading blob' : 'parsing json'}: ${e.message}`);
        }
      }
    }

    if (response.ok) {
      if (params.isLogin) {
        saveLoginHeaders(response.headers);
      }

      if (blob) {
        return {
          blob,
          filename: getFilenameFromHeader(response.headers),
        };
      }

      return body;
    }
    errRespEx = toErrorResponseMessage(body);
  } catch (e) {
    errRespEx = toErrorResponseMessage(e);
  }

  const errAll = buildMessage(errRespEx, true, response);
  const errAbbr = buildMessage(errRespEx, false, response);

  // eslint-disable-next-line no-console
  console.error(errAll);

  store.commit(vxcFp(N_VXC, N_VXC.SET.ADD_HTTP_ERROR), errAbbr);
  store.commit(
    vxcFp(N_VXC, N_VXC.SET.ADD_NOTIFICATION_FOR_UI),
    {
      date: getDate(),
      level: MessageLevel.ERROR,
      text: errAbbr,
    },
  );
  return Promise.reject(errAll);
}

export function toApiUrl(path:string) : string {
  return `${window.location.origin}/api/${path}`;
}

export function toAdminUrl(path:string) : string {
  return `${window.location.origin}/admin/${path}`;
}

function logOutIfNeeded() {
  const isLoggedIn = store.getters[vxcFp(U_VXC, U_VXC.GET.IS_LOGGED_IN)];
  if (isLoggedIn) {
    store.commit(vxcFp(U_VXC, U_VXC.SET.LOGGED_OUT));
  }
}

export const http = {
  login(body : { }) : Promise<Object> {
    // If the user backed-out to the login form they may still be logged in
    logOutIfNeeded();
    const req : Request = new Request(toApiUrl('login'), {
      method: 'POST',
      mode: 'cors',
      ...getHeadersAndBody(body),
    });
    return fetchData({ request: req, isLogin: true }).then((data: Object) => {
      const loginResponse: LoginResponse = data as LoginResponse;
      const serverTime : Date = new Date(loginResponse.serverTime);
      timeDeltaInMillis = serverTime.getTime() - Date.now();

      reconnectInternalInMillis = loginResponse.wsReconnectInterval;
      maxReconnectAttempts = loginResponse.wsMaxReconnectAttempts;
      clientInactivityTimeoutInMillis = loginResponse.wsClientInactivityTimeout;

      const loggedInData: VuexUserState = {
        isLoggedIn: true,
        userId: loginResponse.user.userId,
        name: loginResponse.user.name,
        isAdminRole: loginResponse.isAdminRole,
      };

      store.commit(vxcFp(U_VXC, U_VXC.SET.LOGGED_IN), loggedInData);
      return data;
    });
  },
  logout() : void {
    store.commit(vxcFp(U_VXC, U_VXC.SET.LOGGED_OUT));
    const req : Request = new Request(toApiUrl('logout'), {
      method: 'POST', mode: 'cors', ...getHeadersAndBody(),
    });
    fetchData({ request: req }).catch((e) => {
      // eslint-disable-next-line no-console
      console.error(`Error on logout request: ${e.message}`);
    });
  },
  download(path: string, body?: { }, init?: Object) : Promise<Object> {
    const req : Request = new Request(path, {
      method: 'POST',
      mode: 'cors',
      ...getHeadersAndBody(body),
      ...(init || {}),
    });
    return fetchData({ request: req, isBlob: true }).then((resp: Object) => {
      const blobResponse: BlobResponse = resp as BlobResponse;
      downloadFile(URL.createObjectURL(blobResponse.blob), blobResponse.filename);
      return { filename: blobResponse.filename };
    });
  },
  post(path: string, body: { }, init?: Object) {
    const req : Request = new Request(path, {
      method: 'POST',
      mode: 'cors',
      ...getHeadersAndBody(body),
      ...(init || {}),
    });
    return fetchData({ request: req });
  },
  get(path: string, init?: Object) {
    const req : Request = new Request(path, {
      method: 'GET',
      mode: 'cors',
      ...getHeadersAndBody(),
      ...(init || {}),
    });
    return fetchData({ request: req });
  },
  put(path: string, body: { }, init?: Object) {
    const req : Request = new Request(path, {
      method: 'PUT',
      mode: 'cors',
      ...getHeadersAndBody(body),
      ...(init || {}),
    });
    return fetchData({ request: req });
  },
  delete(path:string, init?: Object) {
    const req : Request = new Request(path, {
      method: 'DELETE',
      mode: 'cors',
      ...getHeadersAndBody(),
      ...(init || {}),
    });
    return fetchData({ request: req });
  },
};
