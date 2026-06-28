export const INSPECTION_TEMPLATE_QUESTION_TYPE_OPTIONS = [
  { value: 'BOOLEAN', label: 'Boolean (Yes/No)' },
  { value: 'TEXT', label: 'Text' },
  { value: 'NUMBER', label: 'Number' },
  { value: 'CHOICE', label: 'Choice' },
  { value: 'PHOTO', label: 'Photo' },
];

export function getInspectionTemplateQuestionTypeLabel(type) {
  const option = INSPECTION_TEMPLATE_QUESTION_TYPE_OPTIONS.find((item) => item.value === type);
  return option ? option.label : type;
}
