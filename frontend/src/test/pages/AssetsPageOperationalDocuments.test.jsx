import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import AssetsPage from '../../pages/AssetsPage';
import assetApi from '../../services/assetApi';
import operationalDocumentApi from '../../services/operationalDocumentApi';
import departmentApi from '../../services/departmentApi';
import assetCategoryApi from '../../services/assetCategoryApi';
import userApi from '../../services/userApi';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: {
    token: 'test-token',
    user: { userId: 30, role: 'FIELD_EMPLOYEE', departmentId: 1 },
  },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/assetApi', () => ({
  default: {
    list: vi.fn(),
    listEligibleForOperationalDocumentUpload: vi.fn(),
    getHistory: vi.fn(),
    register: vi.fn(),
  },
}));

vi.mock('../../services/operationalDocumentApi', () => ({
  default: {
    list: vi.fn(),
    listEligibleOwners: vi.fn(),
    upload: vi.fn(),
    download: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock('../../services/departmentApi', () => ({
  default: { list: vi.fn() },
}));

vi.mock('../../services/assetCategoryApi', () => ({
  default: { list: vi.fn() },
}));

vi.mock('../../services/userApi', () => ({
  default: { getCurrentUser: vi.fn() },
}));

vi.mock('../../services/apiClient', () => ({
  default: { setToken: vi.fn() },
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    auth: mockAuth,
    logout: mockLogout,
  }),
}));

vi.mock('../../components/NotificationButton', () => ({
  default: () => <button type="button">Notifications</button>,
}));

function pageResponse(content, number = 0, totalPages = 1) {
  return { content, number, totalPages };
}

const assetA = {
  id: 5,
  name: 'Central Playground',
  location: 'Memorial Park',
  departmentName: 'Parks',
  assetCategoryName: 'Playground',
  status: 'ACTIVE',
};

async function readFormDataPart(part) {
  if (part == null) {
    return null;
  }
  if (typeof part === 'string') {
    return part;
  }
  if (typeof part.text === 'function') {
    return part.text();
  }
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject(reader.error);
    reader.readAsText(part);
  });
}

async function selectAsset(user, assetId) {
  const assetSelect = await screen.findByLabelText('Asset');
  await waitFor(() => {
    expect(assetSelect).not.toBeDisabled();
    expect(assetSelect.querySelector(`option[value="${assetId}"]`)).toBeInTheDocument();
  });
  await user.selectOptions(assetSelect, String(assetId));
}

function uploadDocumentFile(file) {
  const fileInput = screen.getByLabelText('File');
  Object.defineProperty(fileInput, 'files', {
    configurable: true,
    value: [file],
  });
  fireEvent.change(fileInput);
}

function submitDocumentUpload() {
  const form = document.querySelector('.document-upload-form');
  fireEvent.submit(form);
}

describe('AssetsPage operational document owner selection', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    assetApi.listEligibleForOperationalDocumentUpload.mockResolvedValue(pageResponse([assetA]));
    assetApi.getHistory.mockResolvedValue(pageResponse([]));
    operationalDocumentApi.list.mockResolvedValue(pageResponse([]));
    operationalDocumentApi.listEligibleOwners.mockResolvedValue([]);
    departmentApi.list.mockResolvedValue([]);
    assetCategoryApi.list.mockResolvedValue([]);
    userApi.getCurrentUser.mockResolvedValue({ departmentId: 1 });
  });

  it('loads department-scoped assets for upload users', async () => {
    render(
      <MemoryRouter>
        <AssetsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(assetApi.listEligibleForOperationalDocumentUpload).toHaveBeenCalled();
    });
    expect(assetApi.list).not.toHaveBeenCalled();
  });

  it('calls eligible owner lookup when asset and owner type are selected', async () => {
    const user = userEvent.setup();
    operationalDocumentApi.listEligibleOwners.mockResolvedValue([
      {
        id: 100,
        label: 'Inspection #100',
        status: 'COMPLETED',
        businessDate: '2026-06-01',
        contextSummary: 'NORMAL',
      },
    ]);

    render(
      <MemoryRouter>
        <AssetsPage />
      </MemoryRouter>
    );

    await selectAsset(user, 5);
    await user.selectOptions(screen.getByLabelText('Owner Type (optional)'), 'INSPECTION');

    await waitFor(() => {
      expect(operationalDocumentApi.listEligibleOwners).toHaveBeenCalledWith(5, 'INSPECTION');
      expect(screen.getByRole('option', {
        name: 'Inspection #100 — COMPLETED — 2026-06-01 — NORMAL',
      })).toBeInTheDocument();
    });
  });

  it('resets owner selection when owner type changes', async () => {
    const user = userEvent.setup();
    operationalDocumentApi.listEligibleOwners
      .mockResolvedValueOnce([
        {
          id: 100,
          label: 'Inspection #100',
          status: 'COMPLETED',
          businessDate: '2026-06-01',
          contextSummary: 'NORMAL',
        },
      ])
      .mockResolvedValueOnce([
        {
          id: 200,
          label: 'Issue #200',
          status: 'HIGH',
          businessDate: '2026-06-02',
          contextSummary: 'Broken chain',
        },
      ]);

    render(
      <MemoryRouter>
        <AssetsPage />
      </MemoryRouter>
    );

    await selectAsset(user, 5);
    await user.selectOptions(screen.getByLabelText('Owner Type (optional)'), 'INSPECTION');
    await waitFor(() => {
      expect(screen.getByLabelText('Owner')).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText('Owner'), '100');
    await user.selectOptions(screen.getByLabelText('Owner Type (optional)'), 'ISSUE');

    await waitFor(() => {
      expect(screen.getByRole('option', {
        name: 'Issue #200 — HIGH — 2026-06-02 — Broken chain',
      })).toBeInTheDocument();
    });
    expect(screen.getByLabelText('Owner')).toHaveValue('');
  });

  it('resets owner type and owner when asset changes', async () => {
    const user = userEvent.setup();
    assetApi.listEligibleForOperationalDocumentUpload.mockResolvedValue(
      pageResponse([
        assetA,
        {
          id: 6,
          name: 'North Playground',
          location: 'North Park',
          departmentName: 'Parks',
          assetCategoryName: 'Playground',
          status: 'ACTIVE',
        },
      ])
    );
    operationalDocumentApi.listEligibleOwners.mockResolvedValue([
      {
        id: 100,
        label: 'Inspection #100',
        status: 'COMPLETED',
        businessDate: '2026-06-01',
        contextSummary: 'NORMAL',
      },
    ]);

    render(
      <MemoryRouter>
        <AssetsPage />
      </MemoryRouter>
    );

    await selectAsset(user, 5);
    await user.selectOptions(screen.getByLabelText('Owner Type (optional)'), 'INSPECTION');
    await waitFor(() => {
      expect(screen.getByLabelText('Owner')).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText('Asset'), '6');

    expect(screen.getByLabelText('Owner Type (optional)')).toHaveValue('');
    expect(screen.queryByLabelText('Owner')).not.toBeInTheDocument();
  });

  it('submits upload with selected owner id', async () => {
    const user = userEvent.setup();
    operationalDocumentApi.listEligibleOwners.mockResolvedValue([
      {
        id: 100,
        label: 'Inspection #100',
        status: 'COMPLETED',
        businessDate: '2026-06-01',
        contextSummary: 'NORMAL',
      },
    ]);
    operationalDocumentApi.upload.mockResolvedValue({ id: 1 });

    render(
      <MemoryRouter>
        <AssetsPage />
      </MemoryRouter>
    );

    await selectAsset(user, 5);
    await user.selectOptions(screen.getByLabelText('Document Type'), 'MANUAL');
    await user.selectOptions(screen.getByLabelText('Owner Type (optional)'), 'INSPECTION');
    await waitFor(() => {
      expect(screen.getByLabelText('Owner')).not.toBeDisabled();
      expect(screen.getByRole('option', {
        name: 'Inspection #100 — COMPLETED — 2026-06-01 — NORMAL',
      })).toBeInTheDocument();
    });
    await user.selectOptions(screen.getByLabelText('Owner'), '100');

    const file = new File(['%PDF-1.4'], 'report.pdf', { type: 'application/pdf' });
    uploadDocumentFile(file);
    submitDocumentUpload();

    await waitFor(() => {
      expect(operationalDocumentApi.upload).toHaveBeenCalledWith(5, expect.any(FormData));
    });

    const formData = operationalDocumentApi.upload.mock.calls[0][1];
    expect(JSON.parse(await readFormDataPart(formData.get('ownerType')))).toBe('INSPECTION');
    expect(JSON.parse(await readFormDataPart(formData.get('ownerId')))).toBe(100);
    expect(formData.get('file')).toBeInstanceOf(File);
  });

  it('submits asset-level upload without owner fields', async () => {
    const user = userEvent.setup();
    operationalDocumentApi.upload.mockResolvedValue({ id: 2 });

    render(
      <MemoryRouter>
        <AssetsPage />
      </MemoryRouter>
    );

    await selectAsset(user, 5);
    await user.selectOptions(screen.getByLabelText('Document Type'), 'MANUAL');

    const file = new File(['%PDF-1.4'], 'manual.pdf', { type: 'application/pdf' });
    uploadDocumentFile(file);
    submitDocumentUpload();

    await waitFor(() => {
      expect(operationalDocumentApi.upload).toHaveBeenCalled();
    });

    const formData = operationalDocumentApi.upload.mock.calls[0][1];
    expect(formData.get('ownerType')).toBeNull();
    expect(formData.get('ownerId')).toBeNull();
  });

  it('shows delete confirmation and removes document after confirm', async () => {
    const user = userEvent.setup();
    operationalDocumentApi.list.mockResolvedValue(pageResponse([
      {
        id: 10,
        originalFileName: 'manual.pdf',
        documentType: 'MANUAL',
        ownerType: 'ASSET',
        ownerId: null,
        uploadedAt: '2026-06-01T10:00:00',
      },
    ]));
    operationalDocumentApi.delete.mockResolvedValue(undefined);
    operationalDocumentApi.list.mockResolvedValueOnce(pageResponse([
      {
        id: 10,
        originalFileName: 'manual.pdf',
        documentType: 'MANUAL',
        ownerType: 'ASSET',
        ownerId: null,
        uploadedAt: '2026-06-01T10:00:00',
      },
    ])).mockResolvedValueOnce(pageResponse([]));

    render(
      <MemoryRouter>
        <AssetsPage />
      </MemoryRouter>
    );

    await selectAsset(user, 5);
    await waitFor(() => {
      expect(screen.getByText('manual.pdf')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Delete' }));
    expect(screen.getByRole('heading', { name: 'Delete Operational Document' })).toBeInTheDocument();
    expect(screen.getByText(/Delete document "manual.pdf"/)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.queryByRole('heading', { name: 'Delete Operational Document' })).not.toBeInTheDocument();
    expect(screen.getByText('manual.pdf')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Delete' }));
    await user.click(screen.getAllByRole('button', { name: 'Delete' }).find(
      (button) => button.classList.contains('btn-confirm')
    ));

    await waitFor(() => {
      expect(operationalDocumentApi.delete).toHaveBeenCalledWith(10);
      expect(screen.queryByText('manual.pdf')).not.toBeInTheDocument();
      expect(screen.getByText(/Operational document "manual.pdf" deleted/)).toBeInTheDocument();
    });
  });

  it('displays backend error when delete fails', async () => {
    const user = userEvent.setup();
    operationalDocumentApi.list.mockResolvedValue(pageResponse([
      {
        id: 10,
        originalFileName: 'manual.pdf',
        documentType: 'MANUAL',
        ownerType: 'ASSET',
        ownerId: null,
        uploadedAt: '2026-06-01T10:00:00',
      },
    ]));
    operationalDocumentApi.delete.mockRejectedValue({ status: 403, message: 'Forbidden' });

    render(
      <MemoryRouter>
        <AssetsPage />
      </MemoryRouter>
    );

    await selectAsset(user, 5);
    await waitFor(() => {
      expect(screen.getByText('manual.pdf')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Delete' }));
    await user.click(screen.getAllByRole('button', { name: 'Delete' }).find(
      (button) => button.classList.contains('btn-confirm')
    ));

    await waitFor(() => {
      expect(screen.getByText('You do not have permission to delete this document.')).toBeInTheDocument();
    });
    expect(screen.getByText('manual.pdf')).toBeInTheDocument();
  });
});
