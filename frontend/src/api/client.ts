import axios, {
  AxiosError,
  type AxiosRequestConfig,
} from "axios";

import { emitUnauthorized } from "@/auth/AuthEvents";
import { refreshAccessToken } from "@/auth/api/AuthApi";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
});

/**
 * Axios config extended with our retry flag.
 *
 * This prevents an original request from continuously
 * retrying if the refreshed access token also receives
 * a 401.
 */
interface RetryableRequestConfig extends AxiosRequestConfig {
  _retry?: boolean;
}

/**
 * Add the current access token to every API request.
 */
apiClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("token");

  if (token) {
    config.headers.set("Authorization", `Bearer ${token}`);
  }

  return config;
});

/**
 * Refresh state.
 *
 * Only ONE refresh request should happen at a time.
 */
let isRefreshing = false;

/**
 * Requests waiting for the refresh operation to finish.
 */
type PendingRequest = {
  resolve: (token: string) => void;
  reject: (error: Error) => void;
};

let pendingRequests: PendingRequest[] = [];

/**
 * Resolve or reject all requests waiting for token refresh.
 */
function processQueue(
  error: Error | null,
  token: string | null
): void {

  pendingRequests.forEach((pendingRequest) => {

    if (error) {
      pendingRequest.reject(error);
      return;
    }

    if (token) {
      pendingRequest.resolve(token);
    }
  });

  pendingRequests = [];
}

/**
 * Handle authentication failures.
 */
apiClient.interceptors.response.use(
  (response) => response,

  async (error: AxiosError) => {

    const originalRequest =
      error.config as RetryableRequestConfig | undefined;

    /*
     * If Axios does not provide a request configuration,
     * there is nothing we can safely retry.
     */
    if (!originalRequest) {
      return Promise.reject(error);
    }

    /*
     * 403 means the user is authenticated but does not
     * have permission.
     *
     * Do NOT attempt token refresh.
     */
    if (error.response?.status === 403) {
      return Promise.reject(error);
    }

    /*
     * Only handle 401 responses.
     */
    if (error.response?.status !== 401) {
      return Promise.reject(error);
    }

    /*
     * Prevent infinite refresh loops.
     */
    if (originalRequest._retry) {
      emitUnauthorized();
      return Promise.reject(error);
    }

    /*
     * If another request is already refreshing the token,
     * wait for that refresh to finish.
     */
    if (isRefreshing) {

      return new Promise((resolve, reject) => {

        pendingRequests.push({
          resolve: (newAccessToken: string) => {

            originalRequest._retry = true;

            originalRequest.headers =
              originalRequest.headers ?? {};

            originalRequest.headers.Authorization =
              `Bearer ${newAccessToken}`;

            resolve(apiClient(originalRequest));
          },

          reject,
        });

      });
    }

    /*
     * This request becomes responsible for refreshing.
     */
    originalRequest._retry = true;
    isRefreshing = true;

    const refreshToken =
      sessionStorage.getItem("refreshToken");

    try {

      if (!refreshToken) {
        throw new Error("Missing refresh token");
      }

      /*
       * Backend returns a NEW access token AND a NEW
       * refresh token.
       */
      const tokenResponse =
        await refreshAccessToken(refreshToken);

      const newAccessToken =
        tokenResponse.accessToken;

      /*
       * The refresh function already stored both tokens.
       */

      processQueue(null, newAccessToken);

      /*
       * Retry the original request using the new
       * access token.
       */
      originalRequest.headers =
        originalRequest.headers ?? {};

      originalRequest.headers.Authorization =
        `Bearer ${newAccessToken}`;

      return apiClient(originalRequest);

    } catch (refreshError) {

      const normalizedError =
        refreshError instanceof Error
          ? refreshError
          : new Error("Token refresh failed");

      /*
       * Every request waiting for the refresh must fail.
       */
      processQueue(normalizedError, null);

      /*
       * Refresh failure means the authentication session
       * can no longer be trusted.
       *
       * AuthContext will clear the session and redirect.
       */
      emitUnauthorized();

      return Promise.reject(normalizedError);

    } finally {

      isRefreshing = false;
    }
  }
);

export default apiClient;