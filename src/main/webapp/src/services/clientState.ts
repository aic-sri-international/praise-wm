let timeDeltaInMillis: number = 0;

export function setServerTimeDeltaInMillis(millis: number) {
  timeDeltaInMillis = millis;
}

export function getServerTimeDeltaInMillis() {
  return timeDeltaInMillis;
}
