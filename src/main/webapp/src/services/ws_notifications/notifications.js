// @flow
/* eslint-disable no-console */
import Vue from 'vue';
import isMock from '@/dataConfig';
import { objectEntries } from '@/utils';
import {
  store,
  newWatchLogin,
  NOTIFICATIONS_VXC as NC,
  SYSTEM_STATUS_VXC as SS,
  USER_VXC as UC,
  vxcFp,
} from '@/store/index';

import {
  getUrlForWebsocketEndpoint,
  getWsClientInactivityTimeoutInMillis,
  getWsReconnectInterval,
  getWsMaxReconnectAttempts,
} from '@/services/http';

import { triggerRefresh } from '@/services/data_refresh/refreshManager';
import { getReasonForClose } from './websocket';

import type {
  MessageLevel,
  NotificationTextMessage,
  DataRefreshEvent,
  SystemStatusVuexPayload,
} from './types';

import { eventTypes, systemStatusTypes } from './types';

const serviceName = 'notification';
let url : string = serviceName;
let reconnectInterval : number;
let reconnectAttemptsLeft : number;
let ws : WebSocket;

let clientInactivityTimeout;
let clientInactivityInterval;

// Toast used for socket closure events other than a logout: timeout,
// system shutdown, etc.
let socketCloseToast;

function setLoggedOut(): void {
  store.commit(vxcFp(UC, UC.SET.LOGGED_OUT));
}

function showToast(text: string, duration: ?number) {
  const toastOpts = {
    theme: 'bubble',
    duration,
    action: {
      text: 'Close',
      onClick: (e, t) => {
        t.goAway(0);
      },
    },
  };

  return Vue.toasted.show(text, toastOpts);
}

function dismissToast() {
  if (socketCloseToast) {
    socketCloseToast.goAway(2);
    socketCloseToast = null;
  }
}

function replaceToast(text) {
  dismissToast();
  socketCloseToast = showToast(text);
}

function isLoggedIn() {
  return store.getters[vxcFp(UC, UC.GET.USER)].isLoggedIn;
}

function log(msg : string, isErr : ?boolean) : void {
  if (isErr) {
    console.error(`${url}: ${msg}`);
  } else {
    console.info(`${url}: ${msg}`);
  }
}

function clearClientInactivityInterval() {
  if (clientInactivityInterval) {
    window.clearInterval(clientInactivityInterval);
    clientInactivityInterval = null;
  }
}

function resetActivityTimeout() {
  clientInactivityTimeout = Date.now() + getWsClientInactivityTimeoutInMillis();
}

function ckActivity() {
  if (!isLoggedIn()) {
    clearClientInactivityInterval();
    return;
  }

  if (Date.now() > clientInactivityTimeout) {
    const timeout = getWsClientInactivityTimeoutInMillis();
    log(`clientInactivityTimeout after ${timeout} millis`, true);
    clearClientInactivityInterval();
    replaceToast('Lost connection to server. Will try to reconnect.');
    ws.close();
  }
}

function displayReconnectAttemptToast() {
  const max = getWsMaxReconnectAttempts();
  const cur = max - reconnectAttemptsLeft;
  replaceToast(`Trying to reconnect to server: ${cur} of ${max} attempts...`);
}

function handleNotificationTextMessage(nEvent: NotificationTextMessage) {
  store.commit(
    vxcFp(NC, NC.SET.ADD_NOTIFICATION_FOR_UI),
    {
      date: new Date(nEvent.time),
      level: nEvent.level,
      text: nEvent.text,
    },
  );
}

function handleDataRefreshEvent(nEvent: DataRefreshEvent) {
  triggerRefresh(nEvent.refreshType);
  // We do not send refresh notifications to the notification window
}

function handleSystemStatusEvent(event: any) {
  const setSystemStatus = (system: string, level: MessageLevel) => {
    let which: string = '';

    switch (system) {
      case systemStatusTypes.DATABASE:
        which = SS.SET.DATABASE;
        break;
      default:
        throw Error(`Undefined SystemStatusEvent, SystemStatusType ${system}`);
    }

    const payload: SystemStatusVuexPayload = {
      level,
      event,
    };

    store.commit(vxcFp(SS, which), payload);
  };

  objectEntries(event.systemStatuses).forEach(([system, value]) => {
    setSystemStatus(system, value);
  });
}

function open() : void {
  if (ws) {
    ws.close();
  }

  clearClientInactivityInterval();

  reconnectInterval = getWsReconnectInterval();
  if (reconnectAttemptsLeft === undefined) {
    // Init prior to first open attempt
    reconnectAttemptsLeft = getWsMaxReconnectAttempts();
  }
  url = getUrlForWebsocketEndpoint(serviceName);

  try {
    ws = new WebSocket(url);
  } catch (err) {
    throw Error(`Cannot connect to Websocket url ${url} : ${err}`);
  }

  ws.onopen = () => {
    reconnectAttemptsLeft = getWsMaxReconnectAttempts();
    resetActivityTimeout();
    clientInactivityInterval
        = window.setInterval(ckActivity, getWsClientInactivityTimeoutInMillis());
    dismissToast();
    log('socket is open');
  };

  ws.onmessage = (msg: any) => {
    resetActivityTimeout();
    const nEvent: DataRefreshEvent = JSON.parse(msg.data);

    if (nEvent.eventType !== eventTypes.PING_EVENT) {
      log(`message received: ${msg.data}`);
    }

    if (typeof nEvent.eventType !== 'string') {
      log(`message does not contain an eventType string: ${msg.data}`, true);
      return;
    }
    switch (nEvent.eventType) {
      case eventTypes.SESSION_TIMEOUT_EVENT:
        setLoggedOut();
        ws.close();
        replaceToast('Session has timed out');
        break;
      case eventTypes.SYSTEM_SHUTDOWN_EVENT:
        setLoggedOut();
        ws.close();
        replaceToast('System has shutdown');
        break;
      case eventTypes.DATA_REFRESH_EVENT:
        handleDataRefreshEvent(nEvent);
        break;
      case eventTypes.SYSTEM_STATUS_EVENT:
        handleSystemStatusEvent(nEvent);
        break;
      case eventTypes.NOTIFICATION_TEXTMESSAGE:
        handleNotificationTextMessage(nEvent);
        break;
      case eventTypes.PING_EVENT:
        // Ignore
        break;
      default:
        console.error(`Unexpected notification event: ${nEvent.eventType}`);
    }
  };

  ws.onclose = (event) => {
    if (isLoggedIn()) {
      if (reconnectAttemptsLeft > 0) {
        const reason = getReasonForClose(event);
        log(`socket closed unexpectedly. Will retry ${reconnectAttemptsLeft} times`
            + ` with a ${reconnectInterval} millis delay: ${reason}`);
        reconnectAttemptsLeft -= 1;
        displayReconnectAttemptToast();
        window.setTimeout(open, reconnectInterval);
      } else {
        const total = getWsMaxReconnectAttempts();
        log(`Cannot reconnect to socket, giving up after ${total} attempts`, true);
        setLoggedOut();
        replaceToast('Server is not available. Try again later.');
      }
    } else {
      log('socket closed');
    }
  };
}

// eslint-disable-next-line no-shadow
newWatchLogin((isLoggedIn) => {
  if (!isMock.login) {
    if (isLoggedIn) {
      open();
    } else if (ws) {
      ws.close();
    }
  }
});
