// @flow
/* eslint-disable no-console */

import type { RefreshType } from '@/services/ws_notifications/types';

import type {
  RegistryEntry,
  RegistryComponent,
} from './types';

const enableDebugMessages = false;

let nextId = 1;

type RegistryComponentsById = {
  [id: number]: RegistryComponent,
}

const refreshRegistry: {
  [type: RefreshType]: RegistryComponentsById,
} = {
};

function debugLog(msg: string) {
  if (enableDebugMessages) {
    console.debug(msg);
  }
}

function register(e: RegistryEntry): () => void {
  const newComp: RegistryComponent = { name: e.name, callback: e.callback };

  const id = nextId;
  nextId += 1;

  let componentsById: RegistryComponentsById = refreshRegistry[e.type];
  if (!componentsById) {
    componentsById = {};
    refreshRegistry[e.type] = componentsById;
  }

  componentsById[id] = newComp;

  debugLog(`Data Refresh registered for ${JSON.stringify(e, null, 2)}`);

  return () => {
    delete componentsById[id];
    debugLog(`Data Refresh deregistered for ${JSON.stringify(e, null, 2)}`);
  };
}

async function triggerRefresh(type: RefreshType) : Promise<any> {
  debugLog(`triggerRefresh called for type: ${type}`);

  const componentsById: RegistryComponentsById = refreshRegistry[type];
  if (componentsById) {
    // $FlowFixMe https://github.com/facebook/flow/issues/2221
    const comps: RegistryComponent[] = Object.values(componentsById);
    comps.forEach(async (e: RegistryComponent) => {
      try {
        debugLog(`Calling refresh method for ${e.name}`);
        await e.callback();
      } catch (err) {
        console.error(`Error return from auto-refresh call for type: ${type}, name: ${e.name}`);
      }
    });
  } else {
    debugLog(`No registered refresh callbacks for type: ${type}`);
  }

  return Promise.resolve();
}

export { register, triggerRefresh };
