import { INSPECTION_TEMPLATE_QUESTION_TYPE_OPTIONS } from '../constants/inspectionTemplateQuestionTypes';

export const SUPPORTED_INSPECTION_ANSWER_TYPES = new Set(['BOOLEAN', 'TEXT', 'NUMBER', 'CHOICE']);

export function isSupportedInspectionAnswerType(questionType) {
  return SUPPORTED_INSPECTION_ANSWER_TYPES.has(questionType);
}

export function getUnsupportedQuestionTypeMessage() {
  return 'This question type will be supported in a future sprint.';
}

export function buildInspectionAnswerPayload(question, value) {
  const payload = { questionId: question.id };

  switch (question.questionType) {
    case 'BOOLEAN':
      payload.booleanValue = value === true || value === 'true';

      break;

    case 'TEXT':
      payload.textValue = String(value ?? '').trim();

      break;

    case 'NUMBER':
      payload.numberValue = Number(value);

      break;

    case 'CHOICE':
      payload.choiceCodeValue = String(value ?? '')
        .trim()
        .toUpperCase();

      break;

    default:
      break;
  }

  return payload;
}

export function validateNumberAnswerValue(question, value) {
  if (value === undefined || value === null || value === '') {
    return null;
  }

  const numericValue = Number(value);

  if (Number.isNaN(numericValue)) {
    return 'Enter a valid number.';
  }

  if (question.minValue != null && numericValue < Number(question.minValue)) {
    return `Value must be at least ${question.minValue}.`;
  }

  if (question.maxValue != null && numericValue > Number(question.maxValue)) {
    return `Value must be at most ${question.maxValue}.`;
  }

  if (question.decimalPlaces != null) {
    const decimalPart = String(value).includes('.') ? String(value).split('.')[1] : '';

    if (decimalPart.length > question.decimalPlaces) {
      return `Maximum decimal places: ${question.decimalPlaces}`;
    }
  }

  return null;
}

export function validateRequiredTemplateAnswers(questions, answersByQuestionId) {
  const missing = questions.filter(
    (question) =>
      question.active &&
      question.required &&
      isSupportedInspectionAnswerType(question.questionType) &&
      !hasAnswerValue(question, answersByQuestionId[question.id])
  );

  return missing;
}

export function validateTemplateAnswerValues(questions, answersByQuestionId) {
  for (const question of questions) {
    if (!question.active || question.questionType !== 'NUMBER') {
      continue;
    }

    const value = answersByQuestionId[question.id];

    if (value === undefined || value === null || value === '') {
      continue;
    }

    const error = validateNumberAnswerValue(question, value);

    if (error) {
      return { question, error };
    }
  }

  return null;
}

function hasAnswerValue(question, value) {
  if (value === undefined || value === null || value === '') {
    return false;
  }

  if (question.questionType === 'BOOLEAN') {
    return value === true || value === false || value === 'true' || value === 'false';
  }

  if (question.questionType === 'NUMBER') {
    return value !== '' && !Number.isNaN(Number(value));
  }

  return String(value).trim().length > 0;
}

export function getActiveChoices(question) {
  return (question.choices || [])

    .filter((choice) => choice.active)

    .sort((left, right) => left.displayOrder - right.displayOrder);
}

export function getQuestionUnitSymbol(question) {
  return question.unitSymbol || question.unit || '';
}

export function getAnswerUnitSymbol(answer) {
  return answer?.unitSymbolSnapshot || answer?.numberUnitSnapshot || '';
}

export function getQuestionTypeLabel(questionType) {
  const option = INSPECTION_TEMPLATE_QUESTION_TYPE_OPTIONS.find(
    (item) => item.value === questionType
  );

  return option ? option.label : questionType;
}

export function getNumberInputStep(decimalPlaces) {
  if (decimalPlaces == null) {
    return 'any';
  }

  if (decimalPlaces === 0) {
    return '1';
  }

  return `0.${'0'.repeat(decimalPlaces - 1)}1`;
}

export function getNumberConstraintHint(question) {
  const hints = [];

  if (question.minValue != null || question.maxValue != null) {
    const min = question.minValue != null ? question.minValue : '—';

    const max = question.maxValue != null ? question.maxValue : '—';

    hints.push(`Allowed range: ${min} to ${max}`);
  }

  if (question.decimalPlaces != null) {
    hints.push(`Maximum decimal places: ${question.decimalPlaces}`);
  }

  return hints.join(' · ');
}
