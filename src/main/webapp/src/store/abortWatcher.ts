import { getStore } from '@/store/store';

export const abortWatcher = (
  moduleName: string,
  stateFieldAbortFlagPath: string,
  abortFlagMutationName: string,
  type: string = 'Action',
  preAbort?: Function,
) => {
  const controller = new AbortController();
  const mutation = `${moduleName}/${abortFlagMutationName}`;

  getStore().commit(mutation, false);
  const start = Date.now();

  const getter = (state: any) => {
    const moduleState = state[moduleName];
    const abortFlag: boolean = moduleState[stateFieldAbortFlagPath];
    return abortFlag;
  };

  const unwatch = getStore().watch(
    getter,
    async (abortFlag: boolean) => {
      if (abortFlag) {
        try {
          if (preAbort) {
            await preAbort();
          }
          controller.abort();
        } finally {
          // eslint-disable-next-line no-console
          console.log(`${type || 'Action'} aborted after ${Date.now() - start}ms`);
          getStore().commit(mutation, false);
        }
      }
    },
  );
  return { unwatch, signal: controller.signal };
};
