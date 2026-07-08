type UnauthorizedListener = () => void;
type AuthUpdateListener = (accessToken: string, refreshToken: string | null) => void;

let unauthorizedListeners: UnauthorizedListener[] = [];
let authUpdateListeners: AuthUpdateListener[] = [];

export function subscribeToUnauthorized(cb: UnauthorizedListener) {
  unauthorizedListeners.push(cb);

  return () => {
    unauthorizedListeners = unauthorizedListeners.filter((l) => l !== cb);
  };
}

export function emitUnauthorized() {
  unauthorizedListeners.forEach((cb) => cb());
}

export function subscribeToAuthUpdate(cb: AuthUpdateListener) {
  authUpdateListeners.push(cb);

  return () => {
    authUpdateListeners = authUpdateListeners.filter((l) => l !== cb);
  };
}

export function emitAuthUpdate(accessToken: string, refreshToken: string | null) {
  authUpdateListeners.forEach((cb) => cb(accessToken, refreshToken));
}