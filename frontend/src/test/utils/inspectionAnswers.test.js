import { describe, it, expect } from 'vitest';
import {
  buildInspectionAnswerPayload,
  getNumberConstraintHint,
  isSupportedInspectionAnswerType,
  validateNumberAnswerValue,
  validateRequiredTemplateAnswers,
  validateTemplateAnswerValues,
  getQuestionUnitSymbol,
  getAnswerUnitSymbol,
} from '../../utils/inspectionAnswers';

describe('inspectionAnswers utils', () => {
  it('identifies supported answer types', () => {
    expect(isSupportedInspectionAnswerType('BOOLEAN')).toBe(true);
    expect(isSupportedInspectionAnswerType('TEXT')).toBe(true);
    expect(isSupportedInspectionAnswerType('NUMBER')).toBe(true);
    expect(isSupportedInspectionAnswerType('CHOICE')).toBe(true);
    expect(isSupportedInspectionAnswerType('PHOTO')).toBe(false);
  });

  it('builds boolean answer payload', () => {
    expect(buildInspectionAnswerPayload({ id: 1, questionType: 'BOOLEAN' }, 'true')).toEqual({
      questionId: 1,
      booleanValue: true,
    });
  });

  it('builds text, number, and choice payloads', () => {
    expect(
      buildInspectionAnswerPayload({ id: 2, questionType: 'TEXT' }, 'Minor corrosion')
    ).toEqual({
      questionId: 2,
      textValue: 'Minor corrosion',
    });
    expect(buildInspectionAnswerPayload({ id: 3, questionType: 'NUMBER' }, '87.5')).toEqual({
      questionId: 3,
      numberValue: 87.5,
    });
    expect(buildInspectionAnswerPayload({ id: 4, questionType: 'CHOICE' }, 'good')).toEqual({
      questionId: 4,
      choiceCodeValue: 'GOOD',
    });
  });

  it('detects missing required answers', () => {
    const questions = [
      { id: 1, active: true, required: true, questionType: 'BOOLEAN' },
      { id: 2, active: true, required: false, questionType: 'TEXT' },
      { id: 3, active: true, required: true, questionType: 'PHOTO' },
    ];
    const missing = validateRequiredTemplateAnswers(questions, { 1: 'true' });
    expect(missing).toHaveLength(0);
  });

  it('flags missing required supported answers', () => {
    const questions = [{ id: 1, active: true, required: true, questionType: 'BOOLEAN' }];
    const missing = validateRequiredTemplateAnswers(questions, {});
    expect(missing).toHaveLength(1);
  });

  it('validates number constraints before submit', () => {
    const question = {
      id: 5,
      code: 'TEMPERATURE',
      questionType: 'NUMBER',
      minValue: 0,
      maxValue: 120,
      decimalPlaces: 1,
      active: true,
    };
    expect(validateNumberAnswerValue(question, '-1')).toMatch(/at least 0/i);
    expect(validateNumberAnswerValue(question, '121')).toMatch(/at most 120/i);
    expect(validateNumberAnswerValue(question, '87.55')).toMatch(/decimal places/i);
    expect(validateNumberAnswerValue(question, '87.5')).toBeNull();
  });

  it('returns first invalid number answer from template validation', () => {
    const questions = [
      {
        id: 5,
        code: 'TEMPERATURE',
        questionType: 'NUMBER',
        minValue: 0,
        maxValue: 120,
        decimalPlaces: 1,
        active: true,
      },
    ];
    const result = validateTemplateAnswerValues(questions, { 5: '200' });
    expect(result?.question.code).toBe('TEMPERATURE');
  });

  it('builds number constraint helper text', () => {
    const hint = getNumberConstraintHint({
      minValue: 0,
      maxValue: 120,
      decimalPlaces: 1,
    });
    expect(hint).toContain('Allowed range: 0 to 120');
    expect(hint).toContain('Maximum decimal places: 1');
  });

  it('resolves unit symbol from structured or legacy question fields', () => {
    expect(getQuestionUnitSymbol({ unitSymbol: '°C', unit: 'legacy' })).toBe('°C');
    expect(getQuestionUnitSymbol({ unit: 'bar' })).toBe('bar');
    expect(getAnswerUnitSymbol({ unitSymbolSnapshot: '°C', numberUnitSnapshot: 'legacy' })).toBe(
      '°C'
    );
  });
});
