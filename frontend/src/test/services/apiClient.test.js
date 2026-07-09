import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import apiClient from '../../services/apiClient';
import { registerUnauthorizedHandler } from '../../services/unauthorizedHandler';

describe('ApiClient unauthorized handling', () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    vi.clearAllMocks();
    apiClient.setToken(null);
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    registerUnauthorizedHandler(null);
    apiClient.setToken(null);
  });

  it('notifies unauthorized handler when protected request returns 401', async () => {
    const onUnauthorized = vi.fn();
    registerUnauthorizedHandler(onUnauthorized);
    apiClient.setToken('jwt-token');

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      text: async () => 'Unauthorized',
    });

    await expect(apiClient.get('/api/assets')).rejects.toMatchObject({
      status: 401,
      type: 'UNAUTHORIZED',
    });

    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it('does not notify unauthorized handler for login 401 without token', async () => {
    const onUnauthorized = vi.fn();
    registerUnauthorizedHandler(onUnauthorized);

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      text: async () => 'Email or password is incorrect.',
    });

    await expect(
      apiClient.post('/api/auth/login', { email: 'user@example.com', password: 'wrong' })
    ).rejects.toMatchObject({
      status: 401,
      type: 'UNAUTHORIZED',
    });

    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it('does not notify unauthorized handler when multipart upload returns 400', async () => {
    const onUnauthorized = vi.fn();
    registerUnauthorizedHandler(onUnauthorized);
    apiClient.setToken('jwt-token');

    const formData = new FormData();
    formData.append('file', new File(['%PDF'], 'report.pdf', { type: 'application/pdf' }));
    formData.append('documentType', 'MANUAL');

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      text: async () => 'Invalid multipart request: could not bind documentType.',
    });

    await expect(apiClient.postMultipart('/api/assets/1/documents', formData)).rejects.toMatchObject({
      status: 400,
    });

    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it('notifies unauthorized handler when multipart upload returns 401', async () => {
    const onUnauthorized = vi.fn();
    registerUnauthorizedHandler(onUnauthorized);
    apiClient.setToken('jwt-token');

    const formData = new FormData();
    formData.append('file', new File(['%PDF'], 'report.pdf', { type: 'application/pdf' }));
    formData.append('documentType', 'MANUAL');

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
      text: async () => 'Unauthorized',
    });

    await expect(apiClient.postMultipart('/api/assets/1/documents', formData)).rejects.toMatchObject({
      status: 401,
      type: 'UNAUTHORIZED',
    });

    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });
});
