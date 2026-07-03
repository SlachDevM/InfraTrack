import {
  conditionTypeForQuestionType,
  getOperatorsForConditionType,
} from '../../constants/decisionRules';

export const EMPTY_QUESTION_FORM = {
  code: '',
  questionText: '',
  helpText: '',
  questionType: 'BOOLEAN',
  required: false,
  unitOfMeasureId: '',
  minValue: '',
  maxValue: '',
  decimalPlaces: '',
};

export const EMPTY_CHOICE_FORM = {
  code: '',
  label: '',
};

export const EMPTY_RULE_FORM = {
  ruleCode: '',
  ruleName: '',
  description: '',
  conditionType: 'NUMBER',
  operator: 'GREATER_THAN',
  comparisonValue: '',
  actionType: 'SUGGEST_ISSUE',
  actionPayload: '',
};

export function isDraftTemplate(template) {
  return template?.status === 'DRAFT';
}

export function defaultRuleFormForQuestion(question) {
  const conditionType = conditionTypeForQuestionType(question.questionType);
  const operators = getOperatorsForConditionType(conditionType);
  return {
    ...EMPTY_RULE_FORM,
    conditionType,
    operator: operators[0]?.value || 'EQUALS',
    comparisonValue: '',
  };
}
