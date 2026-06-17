export function toPhotoSrc(photo) {
  if (!photo) return null;
  if (typeof photo === 'string') {
    return photo.startsWith('data:') ? photo : `data:image/jpeg;base64,${photo}`;
  }
  return null;
}

export function extractBase64(photo) {
  if (!photo) return null;
  if (typeof photo === 'string') {
    return photo.startsWith('data:') ? photo.split(',')[1] : photo;
  }
  return null;
}

export function photoEntryFromBase64(base64) {
  const clean = extractBase64(base64);
  return clean ? { preview: toPhotoSrc(clean), base64: clean } : null;
}

export function parsePhotosFromJob(job, pluralKey, singularKey) {
  const list = job[pluralKey];
  if (Array.isArray(list) && list.length > 0) {
    return list.map(photoEntryFromBase64).filter(Boolean);
  }
  const legacy = job[singularKey];
  if (legacy) {
    const entry = photoEntryFromBase64(legacy);
    return entry ? [entry] : [];
  }
  return [];
}

export function photosToPayload(photos) {
  return photos.map((photo) => photo.base64);
}

export function sanitizeClientName(name) {
  return (name.trim() || 'Client').replace(/\s+/g, '_').replace(/[\\/:*?"<>|]/g, '');
}
