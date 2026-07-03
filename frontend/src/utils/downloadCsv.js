import downloadAttachment from './downloadAttachment';

/**
 * Download a CSV file via authenticated GET and trigger a browser save.
 */
export async function downloadCsv(url, token, params = {}, defaultFilename = 'export.csv') {
  return downloadAttachment(url, token, params, defaultFilename);
}

export default downloadCsv;
