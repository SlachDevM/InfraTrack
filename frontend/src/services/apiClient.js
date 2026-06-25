import API_CONFIG from '../config/apiConfig';

class ApiClient {
  constructor(baseURL, token = null) {
    this.baseURL = baseURL;
    this.token = token;
  }

  setToken(token) {
    this.token = token;
  }

  getHeaders() {
    const headers = {
      'Content-Type': 'application/json',
    };
    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`;
    }
    return headers;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      ...options,
      headers: {
        ...this.getHeaders(),
        ...options.headers,
      },
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        const text = await response.text();
        const error = new Error(text || `API request failed: ${response.status}`);
        error.status = response.status;
        error.statusText = response.statusText;

        if (response.status === 401) {
          error.type = 'UNAUTHORIZED';
        } else if (response.status === 403) {
          error.type = 'FORBIDDEN';
        } else if (response.status === 404) {
          error.type = 'NOT_FOUND';
        } else if (response.status === 409) {
          error.type = 'CONFLICT';
        } else if (response.status >= 500) {
          error.type = 'SERVER_ERROR';
        }

        throw error;
      }

      if (response.status === 204) {
        return null;
      }

      const text = await response.text();
      if (!text) {
        return null;
      }

      return JSON.parse(text);
    } catch (err) {
      if (err instanceof TypeError) {
        err.type = 'NETWORK_ERROR';
      }
      throw err;
    }
  }

  get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  }

  post(endpoint, data) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  postMultipart(endpoint, formData) {
    const headers = {};
    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`;
    }
    return this.request(endpoint, {
      method: 'POST',
      headers,
      body: formData,
    });
  }

  put(endpoint, data) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

const apiClient = new ApiClient(API_CONFIG.BASE_URL);

export default apiClient;
