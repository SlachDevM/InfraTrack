import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import IssuesPage from '../../pages/IssuesPage';
import issueApi from '../../services/issueApi';
import inspectionApi from '../../services/inspectionApi';
import { DEFAULT_PAGE, MAX_PAGE_SIZE } from '../../utils/pagination';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 20, role: 'FIELD_EMPLOYEE' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/issueApi', () => ({
  default: {
    list: vi.fn(),
    record: vi.fn(),
    updateCapa: vi.fn(),
  },
}));

vi.mock('../../services/inspectionApi', () => ({
  default: {
    list: vi.fn(),
    listEligibleForIssueRecording: vi.fn(),
  },
}));

vi.mock('../../services/apiClient', () => ({
  default: {
    setToken: vi.fn(),
  },
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

const eligibleInspection = {
  id: 10,
  assetName: 'Central Playground',
  status: 'COMPLETED',
  issueIdentified: true,
  completedByUserId: 20,
  businessTriggerType: 'CUSTOMER_REQUEST',
  observations: 'Swing chain broken',
  completedAt: '2026-06-01T08:00:00',
};

describe('IssuesPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    issueApi.list.mockResolvedValue(pageResponse([]));
    inspectionApi.listEligibleForIssueRecording.mockResolvedValue(pageResponse([]));
  });

  it('renders issues from mocked API', async () => {
    issueApi.list.mockResolvedValue(pageResponse([
      {
        id: 1,
        assetName: 'Central Playground',
        inspectionId: 10,
        description: 'Broken swing',
        severity: 'HIGH',
        recordedAt: '2026-06-01T09:00:00',
      },
    ]));

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Central Playground')).toBeInTheDocument();
    expect(screen.getByText('Broken swing')).toBeInTheDocument();
    expect(screen.getByText('#10')).toBeInTheDocument();
  });

  it('shows eligible inspection in selector from backend response', async () => {
    inspectionApi.listEligibleForIssueRecording.mockResolvedValue(
      pageResponse([eligibleInspection])
    );

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByRole('option', {
      name: '#10 — Central Playground (Customer Request)',
    })).toBeInTheDocument();
    expect(inspectionApi.listEligibleForIssueRecording).toHaveBeenCalledWith(DEFAULT_PAGE, MAX_PAGE_SIZE);
    expect(inspectionApi.list).not.toHaveBeenCalled();
  });

  it('shows empty selector message when no eligible inspections exist', async () => {
    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText(/no completed inspections with identified issues/i)).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Select inspection' })).toBeInTheDocument();
  });

  it('records issue after selecting eligible inspection', async () => {
    const user = userEvent.setup();
    inspectionApi.listEligibleForIssueRecording.mockResolvedValue(
      pageResponse([eligibleInspection])
    );
    issueApi.record.mockResolvedValue({ id: 1 });

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    await user.selectOptions(
      await screen.findByLabelText('Completed Inspection'),
      '10'
    );
    await user.type(screen.getByLabelText('Description'), 'Broken swing chain');
    await user.type(screen.getByLabelText('Lessons Learned'), 'Update installation procedure');
    await user.click(screen.getByRole('button', { name: 'Record Issue' }));

    await waitFor(() => {
      expect(issueApi.record).toHaveBeenCalledWith(expect.objectContaining({
        inspectionId: 10,
        description: 'Broken swing chain',
        severity: 'MEDIUM',
        lessonsLearned: 'Update installation procedure',
      }));
    });
  });

  it('displays CAPA fields on the record issue form', async () => {
    inspectionApi.listEligibleForIssueRecording.mockResolvedValue(
      pageResponse([eligibleInspection])
    );

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByLabelText('Root Cause')).toBeInTheDocument();
    expect(screen.getByLabelText('Corrective Action')).toBeInTheDocument();
    expect(screen.getByLabelText('Preventive Action')).toBeInTheDocument();
    expect(screen.getByLabelText('Lessons Learned')).toBeInTheDocument();
  });

  it('renders lessons learned from API response in issue list', async () => {
    issueApi.list.mockResolvedValue(pageResponse([
      {
        id: 1,
        assetName: 'Central Playground',
        inspectionId: 10,
        description: 'Broken swing',
        severity: 'HIGH',
        rootCause: 'Component fatigue',
        lessonsLearned: 'Review supplier quality',
        recordedAt: '2026-06-01T09:00:00',
      },
    ]));

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Review supplier quality')).toBeInTheDocument();
    expect(screen.getByText('Component fatigue')).toBeInTheDocument();
  });

  it('displays API error message when loading fails', async () => {
    issueApi.list.mockRejectedValue({
      status: 500,
      message: 'Internal server error',
    });

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Internal server error')).toBeInTheDocument();
    });
  });

  it('displays backend validation error when issue recording fails', async () => {
    const user = userEvent.setup();
    inspectionApi.listEligibleForIssueRecording.mockResolvedValue(
      pageResponse([eligibleInspection])
    );
    issueApi.record.mockRejectedValue({
      status: 409,
      message: 'An issue has already been recorded for this inspection',
    });

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    await user.selectOptions(
      await screen.findByLabelText('Completed Inspection'),
      '10'
    );
    await user.type(screen.getByLabelText('Description'), 'Duplicate attempt');
    await user.click(screen.getByRole('button', { name: 'Record Issue' }));

    await waitFor(() => {
      expect(screen.getByText('An issue has already been recorded for this inspection')).toBeInTheDocument();
    });
  });

  it('loads the first page on initial render', async () => {
    issueApi.list.mockResolvedValue(pageResponse([
      {
        id: 1,
        assetName: 'First Page Asset',
        inspectionId: 10,
        description: 'First page issue',
        severity: 'HIGH',
        recordedAt: '2026-06-01T09:00:00',
      },
    ], 0, 2));

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('First page issue')).toBeInTheDocument();
    expect(issueApi.list).toHaveBeenCalledWith(0);
  });

  it('loads the next page when Next is clicked', async () => {
    const user = userEvent.setup();
    issueApi.list.mockImplementation((page = 0) => {
      if (page === 0) {
        return Promise.resolve(pageResponse([
          {
            id: 1,
            assetName: 'First Page Asset',
            inspectionId: 10,
            description: 'First page issue',
            severity: 'HIGH',
            recordedAt: '2026-06-01T09:00:00',
          },
        ], 0, 2));
      }
      return Promise.resolve(pageResponse([
        {
          id: 2,
          assetName: 'Second Page Asset',
          inspectionId: 11,
          description: 'Second page issue',
          severity: 'MEDIUM',
          recordedAt: '2026-06-02T09:00:00',
        },
      ], 1, 2));
    });

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('First page issue')).toBeInTheDocument();
    await user.click(screen.getByTestId('pagination-next'));

    await waitFor(() => {
      expect(issueApi.list).toHaveBeenLastCalledWith(1);
      expect(screen.getByText('Second page issue')).toBeInTheDocument();
    });
  });
});
