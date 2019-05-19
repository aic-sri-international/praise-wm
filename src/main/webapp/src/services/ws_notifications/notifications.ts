import Vue from 'vue';
import isMock from '@/dataConfig';

import {
  getUrlForWebsocketEndpoint,
  getWsClientInactivityTimeoutInMillis,
  getWsMaxReconnectAttempts,
  getWsReconnectInterval,
} from '@/services/http';

import { triggerRefresh } from '@/services/data_refresh/refreshManager';
import { getReasonForClose } from './websocket';

import {
  DataRefreshEvent,
  EventType,
  MessageLevel,
  NotificationEvent,
  NotificationTextMessage,
  SystemStatusEvent,
  SystemStatusType,
} from './types';
import { isLoggedIn, setLoggedOut } from '@/store/user/userHelper';
import { addNotificationForUi } from '@/store/notifications/notificationsHelper';
import { setSystemStatusDatabase } from '@/store/system_status/systemStatusHelper';
import { getStore } from '@/store/store';

const serviceName = 'notification';
let url : string = serviceName;
let reconnectInterval : number;
let reconnectAttemptsLeft : number;
let ws : WebSocket;

let clientInactivityTimeout: number;
let clientInactivityInterval: number | null;

// Toast used for socket closure events other than a logout: timeout,
// system shutdown, etc.
let socketCloseToast: any;

function showToast(text: string, duration?: number) {
  const toastOpts = {
    theme: 'bubble',
    duration,
    action: {
      text: 'Close',
      onClick: (e: any, t: any) => {
        t.goAway(0);
      },
    },
  };

  // @ts-ignore
  return Vue.toasted.show(text, toastOpts);
}

function dismissToast() {
  if (socketCloseToast) {
    socketCloseToast.goAway(2);
    socketCloseToast = null;
  }
}

function replaceToast(text: string) {
  dismissToast();
  socketCloseToast = showToast(text);
}

function log(msg : string, isErr?: boolean) : void {
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
  addNotificationForUi({
      date: new Date(nEvent.time),
      level: nEvent.level ? nEvent.level : MessageLevel.INFO,
      text: nEvent.text ? nEvent.text : '',
    });
}

function handleDataRefreshEvent(nEvent: DataRefreshEvent) {
  // noinspection JSIgnoredPromiseFromCall
  triggerRefresh(nEvent.refreshType);
  // We do not send refresh notifications to the notification window
}

function handleSystemStatusEvent(event: SystemStatusEvent) {
  const setSystemStatus = (system: string, level: MessageLevel) => {
    // noinspection JSRedundantSwitchStatement
    switch (system) {
      case SystemStatusType.DATABASE:
        setSystemStatusDatabase(level);
        break;
      default:
        throw Error(`Undefined SystemStatusEvent, SystemStatusType ${system}`);
    }
  };

  Object.entries(event.systemStatuses).forEach(([system, value]) => {
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
    clientInactivityInterval = window.setInterval(
      ckActivity, getWsClientInactivityTimeoutInMillis(),
    );
    dismissToast();
    log('socket is open');
  };

  ws.onmessage = (msg: any) => {
    resetActivityTimeout();
    const nEvent: NotificationEvent = JSON.parse(msg.data);

    if (nEvent.eventType !== EventType.PING_EVENT) {
      log(`message received: ${msg.data}`);
    }

    if (typeof nEvent.eventType !== 'string') {
      log(`message does not contain an eventType string: ${msg.data}`, true);
      return;
    }
    switch (nEvent.eventType) {
      case EventType.SESSION_TIMEOUT_EVENT:
        setLoggedOut();
        ws.close();
        replaceToast('Session has timed out');
        break;
      case EventType.SYSTEM_SHUTDOWN_EVENT:
        setLoggedOut();
        ws.close();
        replaceToast('System has shutdown');
        break;
      case EventType.DATA_REFRESH_EVENT:
        handleDataRefreshEvent(nEvent as DataRefreshEvent);
        break;
      case EventType.SYSTEM_STATUS_EVENT:
        handleSystemStatusEvent(nEvent as SystemStatusEvent);
        break;
      case EventType.NOTIFICATION_TEXT_MESSAGE:
        handleNotificationTextMessage(nEvent);
        break;
      case EventType.PING_EVENT:
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

getStore().watch(
    isLoggedIn,
  (loggedIn) => {
    if (!isMock.login) {
      if (loggedIn) {
        open();
      } else if (ws) {
        ws.close();
      }
    }
  },
);
