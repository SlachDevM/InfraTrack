import API_CONFIG from '../config/apiConfig';

/**
 * Download a CSV file via authenticated GET and trigger a browser save.
 *
 * @param {string} url - API path (e.g. /api/reporting/exports/assets.csv)
 * @param {string} [token] - JWT bearer token
 * @param {object} [params] - Optional query params (from, to as epoch millis)
 * @param {string} [defaultFilename] - Fallback filename when Content-Disposition is absent
 */
export async function downloadCsv(url, token, params = {}, defaultFilename = 'export.csv') {
  const search = new URLSearchParams();
  if (params.from != null) {
    search.set('from', String(params.from));
  }
  if (params.to != null) {
    search.set('to', String(params.to));
  }
  const query = search.toString();
  const fullUrl = `${API_CONFIG.BASE_URL}${url}${query ? `?${query}` : ''}`;

  const response = await fetch(fullUrl, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  if (response.status === 403) {
    const text = await response.text();
    const error = new Error(text || 'You do not have permission to export.');
    error.status = 403;
    throw error;
  }

  if (!response.ok) {
    const text = await response.text();
    const error = new Error(text || `Export failed: ${response.status}`);
    error.status = response.status;
    throw error;
  }

  const blob = await response.blob();
  const disposition = response.headers.get('Content-Disposition') || '';
  const match = disposition.match(/filename="(.+)"/);
  const filename = match ? match[1] : defaultFilename;

  const objectUrl = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = filename;
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(objectUrl);

  return { blob, filename };
}

export default downloadCsv;
