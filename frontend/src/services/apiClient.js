import API_CONFIG from '../config/apiConfig';
import { HTTP_STATUS } from '../constants/httpStatus';
import { notifyUnauthorized } from './unauthorizedHandler';

function isFormDataBody(body) {
  return typeof FormData !== 'undefined' && body instanceof FormData;
}

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

  buildAuthHeaders() {
    return this.token ? { Authorization: `Bearer ${this.token}` } : {};
  }

  async parseResponse(response) {
    if (!response.ok) {
      const text = await response.text();
      const error = new Error(text || `API request failed: ${response.status}`);
      error.status = response.status;
      error.statusText = response.statusText;

      if (response.status === HTTP_STATUS.UNAUTHORIZED) {
        error.type = 'UNAUTHORIZED';
        if (this.token) {
          notifyUnauthorized();
        }
      } else if (response.status === HTTP_STATUS.FORBIDDEN) {
        error.type = 'FORBIDDEN';
      } else if (response.status === HTTP_STATUS.NOT_FOUND) {
        error.type = 'NOT_FOUND';
      } else if (response.status === HTTP_STATUS.CONFLICT) {
        error.type = 'CONFLICT';
      } else if (response.status >= HTTP_STATUS.INTERNAL_SERVER_ERROR) {
        error.type = 'SERVER_ERROR';
      }

      throw error;
    }

    if (response.status === HTTP_STATUS.NO_CONTENT) {
      return null;
    }

    const text = await response.text();
    if (!text) {
      return null;
    }

    return JSON.parse(text);
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const isFormData = isFormDataBody(options.body);
    const headers = isFormData
      ? { ...this.buildAuthHeaders(), ...options.headers }
      : { ...this.getHeaders(), ...options.headers };

    if (isFormData) {
      delete headers['Content-Type'];
      delete headers['content-type'];
    }

    const config = {
      ...options,
      headers,
    };

    try {
      const response = await fetch(url, config);
      return await this.parseResponse(response);
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
    if (!isFormDataBody(formData)) {
      throw new TypeError('postMultipart requires FormData');
    }

    return this.request(endpoint, {
      method: 'POST',
      body: formData,
    });
  }

  put(endpoint, data) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  patch(endpoint, data) {
    return this.request(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

const apiClient = new ApiClient(API_CONFIG.BASE_URL);

export default apiClient;
