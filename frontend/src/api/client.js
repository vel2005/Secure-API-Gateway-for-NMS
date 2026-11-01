import axios from 'axios';

// Base API URL strictly points to Kong Gateway OSS Proxy (port 8000)
export const KONG_API_URL = 'http://localhost:8000';
export const KONG_ADMIN_URL = 'http://localhost:8001';

const apiClient = axios.create({
  baseURL: KONG_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Listener callbacks for rate-limit updates
const rateLimitListeners = new Set();
export const onRateLimitUpdate = (callback) => {
  rateLimitListeners.add(callback);
  return () => rateLimitListeners.delete(callback);
};

// Request interceptor: attach token or basic auth
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      // If token starts with "Basic ", use Basic auth; otherwise Bearer
      config.headers.Authorization = token.startsWith('Basic ') ? token : `Bearer ${token}`;
    }
    config.metadata = { startTime: new Date() };
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: extract rate limit headers and record metrics
export const apiMetrics = {
  totalRequests: 0,
  successfulRequests: 0,
  failedRequests: 0,
  rateLimitViolations: 0,
  lastLatency: 0,
  history: [],
};

apiClient.interceptors.response.use(
  (response) => {
    const duration = new Date() - response.config.metadata.startTime;
    apiMetrics.totalRequests += 1;
    apiMetrics.successfulRequests += 1;
    apiMetrics.lastLatency = duration;

    // Extract Kong Rate-Limiting headers
    const limit = response.headers['x-ratelimit-limit-minute'] || response.headers['ratelimit-limit'];
    const remaining = response.headers['x-ratelimit-remaining-minute'] || response.headers['ratelimit-remaining'];
    const reset = response.headers['ratelimit-reset'];

    const rateInfo = {
      limit: limit ? parseInt(limit, 10) : 100,
      remaining: remaining ? parseInt(remaining, 10) : null,
      reset: reset ? parseInt(reset, 10) : null,
      latency: duration,
      status: response.status,
      url: response.config.url,
      method: response.config.method?.toUpperCase(),
      timestamp: new Date().toLocaleTimeString(),
    };

    apiMetrics.history.unshift(rateInfo);
    if (apiMetrics.history.length > 50) apiMetrics.history.pop();

    rateLimitListeners.forEach((cb) => cb(rateInfo));
    return response;
  },
  (error) => {
    apiMetrics.totalRequests += 1;
    apiMetrics.failedRequests += 1;

    if (error.response?.status === 429) {
      apiMetrics.rateLimitViolations += 1;
    }

    const rateInfo = {
      status: error.response?.status || 0,
      url: error.config?.url || '',
      method: error.config?.method?.toUpperCase() || 'GET',
      error: error.response?.data?.message || error.message,
      timestamp: new Date().toLocaleTimeString(),
    };

    apiMetrics.history.unshift(rateInfo);
    if (apiMetrics.history.length > 50) apiMetrics.history.pop();
    rateLimitListeners.forEach((cb) => cb(rateInfo));

    return Promise.reject(error);
  }
);

export default apiClient;

