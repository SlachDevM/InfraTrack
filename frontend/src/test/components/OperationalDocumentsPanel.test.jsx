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

const defaultProps = {
  canUploadDocuments: false,
  selectedAssetId: '5',
  documentForm,
  eligibleOwners: [],
  eligibleOwnersLoading: false,
  assetDocuments: [],
  documentsLoading: false,
  documentUploading: false,
  documentsPage: 0,
  documentsTotalPages: 0,
  onDocumentFormChange: vi.fn(),
  onDocumentFileChange: vi.fn(),
  onUpload: vi.fn((event) => event.preventDefault()),
  onDownload: vi.fn(),
  onDocumentsPrevious: vi.fn(),
  onDocumentsNext: vi.fn(),
};

describe('OperationalDocumentsPanel', () => {
  it('renders upload controls when upload is allowed', () => {
    render(
      <OperationalDocumentsPanel
        {...defaultProps}
        canUploadDocuments
      />
    );

    expect(screen.getByLabelText('Document Type')).toBeInTheDocument();
    expect(screen.getByLabelText('File')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Upload Document' })).toBeInTheDocument();
  });

  it('accepts DOCX and XLSX in file input', () => {
    render(
      <OperationalDocumentsPanel
        {...defaultProps}
        canUploadDocuments
      />
    );

    const fileInput = screen.getByLabelText('File');
    expect(fileInput).toHaveAttribute('accept', expect.stringContaining('.docx'));
    expect(fileInput).toHaveAttribute('accept', expect.stringContaining('.xlsx'));
  });

  it('shows owner selector when owner type is selected', () => {
    render(
      <OperationalDocumentsPanel
        {...defaultProps}
        canUploadDocuments
        documentForm={{ ...documentForm, ownerType: 'INSPECTION' }}
        eligibleOwners={[
          {
            id: 100,
            label: 'Inspection #100',
            status: 'COMPLETED',
            businessDate: '2026-06-01',
            contextSummary: 'NORMAL',
          },
        ]}
      />
    );

    expect(screen.getByLabelText('Owner')).toBeInTheDocument();
    expect(screen.getByRole('option', { name: /Inspection #100/i })).toBeInTheDocument();
    expect(screen.queryByLabelText('Owner ID (optional)')).not.toBeInTheDocument();
  });

  it('shows empty state when no eligible owners exist', () => {
    render(
      <OperationalDocumentsPanel
        {...defaultProps}
        canUploadDocuments
        documentForm={{ ...documentForm, ownerType: 'ISSUE' }}
        eligibleOwners={[]}
      />
    );

    expect(screen.getByText(/No eligible records found for this asset and owner type/i))
      .toBeInTheDocument();
  });

  it('renders document rows', () => {
    render(
      <OperationalDocumentsPanel
        {...defaultProps}
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
      />
    );

    expect(screen.getByText('manual.pdf')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Download' })).toBeInTheDocument();
  });
});
