import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import OperationalDocumentsPanel from '../../components/assets/OperationalDocumentsPanel';

const documentForm = {
  documentType: '',
  ownerType: '',
  ownerId: '',
  documentDate: '',
  file: null,
};

describe('OperationalDocumentsPanel', () => {
  it('renders upload controls when upload is allowed', () => {
    render(
      <OperationalDocumentsPanel
        canUploadDocuments
        selectedAssetId="5"
        documentForm={documentForm}
        assetDocuments={[]}
        documentsLoading={false}
        documentUploading={false}
        onDocumentFormChange={vi.fn()}
        onDocumentFileChange={vi.fn()}
        onUpload={vi.fn((event) => event.preventDefault())}
        onDownload={vi.fn()}
      />
    );

    expect(screen.getByLabelText('Document Type')).toBeInTheDocument();
    expect(screen.getByLabelText('File')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Upload Document' })).toBeInTheDocument();
  });

  it('renders document rows', () => {
    render(
      <OperationalDocumentsPanel
        canUploadDocuments={false}
        selectedAssetId="5"
        documentForm={documentForm}
        assetDocuments={[
          {
            id: 10,
            originalFileName: 'manual.pdf',
            documentType: 'MANUAL',
            ownerType: 'ASSET',
            ownerId: null,
            uploadedAt: '2026-06-01T10:00:00',
          },
        ]}
        documentsLoading={false}
        documentUploading={false}
        onDocumentFormChange={vi.fn()}
        onDocumentFileChange={vi.fn()}
        onUpload={vi.fn()}
        onDownload={vi.fn()}
      />
    );

    expect(screen.getByText('manual.pdf')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Download' })).toBeInTheDocument();
  });
});
