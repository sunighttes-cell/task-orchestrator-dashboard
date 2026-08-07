// centralized API layer
import axios from "axios";
import { emitUnauthorized } from "@/auth/AuthEvents";
import { refreshAccessToken } from "@/auth/api/AuthApi";

// create an axios instance with base URL and interceptors for authorization and error handling
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
});

// add a request interceptor to include the token in the Authorization header if it exists  
apiClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

//refresh token logic
let isRefreshing = false;
let pendingRequests: any[] = [];

function processQueue(error: any, token: string | null) {
  pendingRequests.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });

  pendingRequests = [];
}

// add a response interceptor to handle 401 errors and attempt token refresh
apiClient.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config;
    console.log("401 interceptors", err.response?.status, originalRequest._retry);

    if (err.response?.status === 403) {
      return Promise.reject(err);
    }

    if (err.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // queue request
        return new Promise((resolve, reject) => {
          pendingRequests.push({
            resolve: (token: string) => {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              resolve(apiClient(originalRequest));
            },
            reject,
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;
      const refreshToken = sessionStorage.getItem("refreshToken");
      console.log("refreshToken", refreshToken);

      try {
        if (!refreshToken) {
          throw new Error("Missing refresh token");
        }

        const newToken = await refreshAccessToken(refreshToken);
        console.log("newToken in try", newToken);
        console.log("originalRequest", originalRequest);

        processQueue(null, newToken);

        originalRequest.headers.Authorization = `Bearer ${newToken}`;

        return apiClient(originalRequest);
      } catch (refreshError) {
        console.log("refreshError in catch", refreshError);

        processQueue(refreshError, null);

        emitUnauthorized(); //fallback logout

        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(err);
  }
);

export default apiClient;