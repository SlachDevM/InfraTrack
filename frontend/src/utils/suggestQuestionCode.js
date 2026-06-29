const MAX_LENGTH = 100;

export function suggestQuestionCode(questionText) {
  if (!questionText || !questionText.trim()) {
    return '';
  }

  let normalized = questionText
    .trim()
    .replace(/[^a-zA-Z0-9]+/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_|_$/g, '')
    .toUpperCase();

  if (!normalized) {
    return 'QUESTION';
  }
  if (!/^[A-Z]/.test(normalized)) {
    normalized = `Q_${normalized}`;
  }
  if (normalized.length > MAX_LENGTH) {
    normalized = normalized.substring(0, MAX_LENGTH).replace(/_+$/, '');
  }
  return normalized;
}

export function isValidQuestionCode(code) {
  return typeof code === 'string' && /^[A-Z][A-Z0-9_]*$/.test(code) && code.length <= MAX_LENGTH;
}
