import axios, {
  type AxiosError,
  type InternalAxiosRequestConfig,
} from "axios";
import { emitUnauthorized } from "@/auth/AuthEvents";
import { refreshAccessToken } from "@/auth/api/AuthApi";

//Centralized API client.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
});

//Extend Axios request configuration to track if a request has already been retried.
type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

 //A request waiting for the refresh operation to finish.
type PendingRequest = {
  resolve: (accessToken: string) => void;
  reject: (error: Error) => void;
};

//Refresh state.
let isRefreshing = false;
let pendingRequests: PendingRequest[] = [];

//Resolve or reject all requests waiting for token refresh.
function processQueue(
  error: Error | null,
  accessToken: string | null
): void {

  pendingRequests.forEach((pendingRequest) => {

    if (error) {
      pendingRequest.reject(error);
      return;
    }

    if (accessToken) {
      pendingRequest.resolve(accessToken);
    }
  });

  pendingRequests = [];
}

//Add access token to every authenticated API request.
apiClient.interceptors.request.use(
  (config) => {

    const token = sessionStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

//Handle API responses. 401: Access token may have expired.
//Attempt: request -> 401 -> refresh token -> new access token + new refresh token -> retry original request
//403: Do not attempt refresh. A 403 means authentication succeeded but authorization was denied.
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    //If Axios does not provide the original request,
    //there is nothing we can retry.
    if (!originalRequest) {
      return Promise.reject(error);
    }

    //Never attempt token refresh for forbidden responses.
    if (error.response?.status === 403) {
      return Promise.reject(error);
    }

    //Only handle 401 responses.
    if (error.response?.status !== 401) {
      return Promise.reject(error);
    }

    //Prevent an infinite retry loop.
    if (originalRequest._retry) {
      return Promise.reject(error);
    }

    //Mark this request so it can only be retried once.
    originalRequest._retry = true;

    //If another request is already refreshing the token,
    //place this request into the queue.
    if (isRefreshing) {

      return new Promise((resolve, reject) => {

        pendingRequests.push({

          resolve: (newAccessToken: string) => {

            originalRequest.headers.Authorization =
              `Bearer ${newAccessToken}`;

            resolve(
              apiClient(originalRequest)
            );
          },

          reject,
        });
      });
    }

    //Responsible for refreshing authentication.
    isRefreshing = true;
    const refreshToken = sessionStorage.getItem("refreshToken");
    try {
      if (!refreshToken) {
        throw new Error("Missing refresh token");
      }

      //Backend rotates BOTH tokens.
      const { accessToken } = await refreshAccessToken(refreshToken);

      //Resolve all requests waiting for refresh.
      processQueue(
        null,
        accessToken
      );

      //Retry the original request with the new access token.
      originalRequest.headers.Authorization =
        `Bearer ${accessToken}`;

      return apiClient(originalRequest);

    } catch (refreshError) {
      const normalizedError =
        refreshError instanceof Error ? refreshError : new Error("Token refresh failed");

      //Reject all queued requests.
      processQueue(
        normalizedError,
        null
      );

      //This means the refresh token itself failed.
      //Possible backend reasons: refresh token expired, refresh token revoked, refresh token reuse detected
      // invalid refresh token. The backend may have revoked all sessions in the token-reuse case.
      emitUnauthorized();

      return Promise.reject(normalizedError);
    } finally {
      isRefreshing = false;
    }
  }
);

export default apiClient;
