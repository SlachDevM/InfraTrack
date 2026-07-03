import { getInspectionTemplateStatusLabel } from '../../constants/inspectionTemplateStatuses';

export default function InspectionTemplateQuestionHeader({ template, canManage, isDraft }) {
  return (
    <section className="reference-form-section">
      <h2>{template.name}</h2>
      <p>
        Asset Category: {template.assetCategoryName}
        {' · '}
        Version: {template.version}
        {' · '}
        Status: {getInspectionTemplateStatusLabel(template.status)}
      </p>
      {!canManage && (
        <p className="read-only-note">
          Checklist questions are read-only. Administrators can create, edit, deactivate, and
          reorder questions on draft templates.
        </p>
      )}
      {canManage && !isDraft && (
        <p className="read-only-note">
          This template is {getInspectionTemplateStatusLabel(template.status).toLowerCase()}.
          Checklist questions cannot be modified. Create a new template version in a future release
          to change published templates.
        </p>
      )}
    </section>
  );
}
