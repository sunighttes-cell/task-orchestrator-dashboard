type UnauthorizedListener = () => void;
type AuthUpdateListener = (accessToken: string, refreshToken: string | null) => void;

const authUpdateListeners = new Set<AuthUpdateListener>();
const unauthorizedListeners = new Set<UnauthorizedListener>();

//Notify the application that authentication changed.
export function emitAuthUpdate(accessToken: string | null, 
  refreshToken: string | null): void {
  authUpdateListeners.forEach((listener) => 
    listener(accessToken, refreshToken));
}

//Subscribe to authentication changes.
export function subscribeToAuthUpdate(listener: AuthUpdateListener): () => void {
  authUpdateListeners.add(listener);
  return () => {
    authUpdateListeners.delete(listener);
  };
}

//Notify the application that authentication can no longer be maintained.
export function emitUnauthorized(): void {
  unauthorizedListeners.forEach(
    (listener) => {
       listener(); 
    }
  );
}

//Subscribe to unauthorized events.
export function subscribeToUnauthorized(
  listener: UnauthorizedListener
): () => void {
  unauthorizedListeners.add(listener);
  return () => {
    unauthorizedListeners.delete(listener);
  };
}
