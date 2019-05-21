import { RefreshType } from '@/services/ws_notifications/types';

import { RegistryComponent, RegistryEntry } from './types';

const enableDebugMessages = false;

let nextId = 1;

type RegistryComponentsById = {
  [id: number]: RegistryComponent,
}

const refreshRegistry: {
  [K in RefreshType]?: RegistryComponentsById;
} = {};

function debugLog(msg: string) {
  if (enableDebugMessages) {
    console.debug(msg);
  }
}

function getOrCreateRegistryComponentsById(refreshType: RefreshType): RegistryComponentsById {
  let componentsById: RegistryComponentsById | undefined = refreshRegistry[refreshType];
  if (!componentsById) {
    componentsById = {};
    refreshRegistry[refreshType] = componentsById;
  }
  return componentsById;
}

function register(e: RegistryEntry): () => void {
  const newComp: RegistryComponent = { name: e.name, callback: e.callback };

  const id = nextId;
  nextId += 1;

  const componentsById: RegistryComponentsById = getOrCreateRegistryComponentsById(e.type);
  componentsById[id] = newComp;

  debugLog(`Data Refresh registered for ${JSON.stringify(e, null, 2)}`);

  return () => {
    delete componentsById[id];
    debugLog(`Data Refresh deregistered for ${JSON.stringify(e, null, 2)}`);
  };
}

async function triggerRefresh(type: RefreshType): Promise<any> {
  debugLog(`triggerRefresh called for type: ${type}`);

  const componentsById: RegistryComponentsById | undefined = refreshRegistry[type];
  if (componentsById) {
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
