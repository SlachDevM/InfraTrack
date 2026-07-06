import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import OperationalDocumentsPanel from '../../components/assets/OperationalDocumentsPanel';

const documentForm = {
  documentType: 'MANUAL',
  ownerType: '',
  documentDate: '',
  file: null,
};

const defaultProps = {
  canUploadDocuments: false,
  selectedAssetId: '5',
  documentForm,
  selectedOwnerId: '',
  eligibleOwners: [],
  eligibleOwnersLoading: false,
  eligibleOwnersError: null,
  assetDocuments: [],
  documentsLoading: false,
  documentUploading: false,
  documentsPage: 0,
  documentsTotalPages: 0,
  onDocumentFormChange: vi.fn(),
  onOwnerSelect: vi.fn(),
  onDocumentFileChange: vi.fn(),
  onUpload: vi.fn((event) => event.preventDefault()),
  onDownload: vi.fn(),
  onDeleteClick: vi.fn(),
  onDocumentsPrevious: vi.fn(),
  onDocumentsNext: vi.fn(),
};

describe('OperationalDocumentsPanel', () => {
  afterEach(cleanup);

  it('renders upload controls when upload is allowed', () => {
    render(<OperationalDocumentsPanel {...defaultProps} canUploadDocuments />);

    expect(screen.getByLabelText('Document Type')).toBeInTheDocument();
    expect(screen.getByLabelText('File')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Upload Document' })).toBeInTheDocument();
  });

  it('does not display manual Owner ID input', () => {
    render(
      <OperationalDocumentsPanel
        {...defaultProps}
        canUploadDocuments
        documentForm={{ ...documentForm, ownerType: 'INSPECTION' }}
      />
    );

    expect(screen.queryByLabelText(/Owner ID/i)).not.toBeInTheDocument();
    expect(screen.queryByRole('spinbutton')).not.toBeInTheDocument();
  });

  it('accepts DOCX and XLSX in file input', () => {
    render(<OperationalDocumentsPanel {...defaultProps} canUploadDocuments />);

    const fileInput = screen.getByLabelText('File');
    expect(fileInput).toHaveAttribute('accept', expect.stringContaining('.docx'));
    expect(fileInput).toHaveAttribute('accept', expect.stringContaining('.xlsx'));
  });

  it('shows owner dropdown when owner type is selected', () => {
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
    expect(
      screen.getByRole('option', {
        name: 'Inspection #100 — COMPLETED — 2026-06-01 — NORMAL',
      })
    ).toBeInTheDocument();
  });

  it('hides owner dropdown when owner type is empty', () => {
    render(<OperationalDocumentsPanel {...defaultProps} canUploadDocuments />);

    expect(screen.queryByLabelText('Owner')).not.toBeInTheDocument();
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

    expect(screen.getByText('No eligible records found for this owner type.')).toBeInTheDocument();
  });

  it('calls onOwnerSelect when owner is chosen', async () => {
    const user = userEvent.setup();
    const onOwnerSelect = vi.fn();

    render(
      <OperationalDocumentsPanel
        {...defaultProps}
        canUploadDocuments
        documentForm={{ ...documentForm, ownerType: 'WORK_ORDER' }}
        eligibleOwners={[
          {
            id: 200,
            label: 'Work Order #200',
            status: 'COMPLETED',
            businessDate: '2026-06-02',
            contextSummary: 'INTERNAL_MAINTENANCE',
          },
        ]}
        onOwnerSelect={onOwnerSelect}
      />
    );

    await user.selectOptions(screen.getByLabelText('Owner'), '200');

    expect(onOwnerSelect).toHaveBeenCalled();
  });

  it('disables upload when owner type is selected but no owner chosen', () => {
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

    expect(screen.getByRole('button', { name: 'Upload Document' })).toBeDisabled();
  });

  it('allows upload for asset-level document without owner', () => {
    render(<OperationalDocumentsPanel {...defaultProps} canUploadDocuments />);

    expect(screen.getByRole('button', { name: 'Upload Document' })).toBeEnabled();
  });

  it('renders document rows and download action', () => {
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
    expect(screen.queryByRole('button', { name: 'Delete' })).not.toBeInTheDocument();
  });

  it('shows delete action when upload is allowed', () => {
    render(
      <OperationalDocumentsPanel
        {...defaultProps}
        canUploadDocuments
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

    expect(screen.getByRole('button', { name: 'Delete document manual.pdf' })).toBeInTheDocument();
  });

  it('calls onDeleteClick when delete is pressed', async () => {
    const user = userEvent.setup();
    const onDeleteClick = vi.fn();
    const document = {
      id: 10,
      originalFileName: 'manual.pdf',
      documentType: 'MANUAL',
      ownerType: 'ASSET',
      ownerId: null,
      uploadedAt: '2026-06-01T10:00:00',
    };

    render(
      <OperationalDocumentsPanel
        {...defaultProps}
        canUploadDocuments
        assetDocuments={[document]}
        onDeleteClick={onDeleteClick}
      />
    );

    await user.click(screen.getByRole('button', { name: 'Delete document manual.pdf' }));

    expect(onDeleteClick).toHaveBeenCalledWith(document);
  });

  it('calls onDownload when download is pressed', async () => {
    const user = userEvent.setup();
    const onDownload = vi.fn();

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
        onDownload={onDownload}
      />
    );

    await user.click(screen.getByRole('button', { name: 'Download' }));

    expect(onDownload).toHaveBeenCalledWith(10);
  });
});
