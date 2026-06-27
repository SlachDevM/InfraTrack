import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import NotificationPage from '../../pages/NotificationPage';
import notificationApi from '../../services/notificationApi';

const mockNavigate = vi.fn();
const mockDecrementUnread = vi.fn();
const mockRefreshUnreadCount = vi.fn();

vi.mock('../../services/notificationApi', () => ({
  default: {
    list: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
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
    auth: { token: 'test-token', user: { id: 1, name: 'Coordinator' } },
    logout: vi.fn(),
  }),
}));

vi.mock('../../context/NotificationContext', () => ({
  useNotifications: () => ({
    decrementUnread: mockDecrementUnread,
    clearUnread: vi.fn(),
    refreshUnreadCount: mockRefreshUnreadCount,
  }),
}));

function pageResponse(content, number = 0, totalPages = 1) {
  return { content, number, totalPages };
}

describe('NotificationPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    notificationApi.list.mockResolvedValue(pageResponse([]));
    notificationApi.markAsRead.mockResolvedValue({});
  });

  it('renders notifications from notificationApi', async () => {
    notificationApi.list.mockResolvedValue(pageResponse([
      {
        id: 1,
        title: 'Inspection Assigned',
        message: 'You have a new inspection.',
        createdAt: '2026-06-01T09:00:00',
        isRead: false,
        targetRoute: null,
      },
    ]));

    render(
      <MemoryRouter>
        <NotificationPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Inspection Assigned')).toBeInTheDocument();
    expect(screen.getByText('You have a new inspection.')).toBeInTheDocument();
  });

  it('marks unread notification as read and navigates when targetRoute is present', async () => {
    const user = userEvent.setup();
    notificationApi.list.mockResolvedValue(pageResponse([
      {
        id: 2,
        title: 'Work Order Assigned',
        message: 'Work order #100 assigned.',
        createdAt: '2026-06-01T10:00:00',
        isRead: false,
        targetRoute: '/work-orders',
      },
    ]));

    render(
      <MemoryRouter>
        <NotificationPage />
      </MemoryRouter>
    );

    const item = await screen.findByText('Work Order Assigned');
    await user.click(item);

    await waitFor(() => {
      expect(notificationApi.markAsRead).toHaveBeenCalledWith(2);
      expect(mockNavigate).toHaveBeenCalledWith('/work-orders');
    });
  });

  it('does not crash when notification has no targetRoute', async () => {
    const user = userEvent.setup();
    notificationApi.list.mockResolvedValue(pageResponse([
      {
        id: 3,
        title: 'General Notice',
        message: 'System maintenance tonight.',
        createdAt: '2026-06-01T11:00:00',
        isRead: true,
        targetRoute: null,
      },
    ]));

    render(
      <MemoryRouter>
        <NotificationPage />
      </MemoryRouter>
    );

    await user.click(await screen.findByText('General Notice'));

    expect(notificationApi.markAsRead).not.toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalled();
    expect(screen.getByText('System maintenance tonight.')).toBeInTheDocument();
  });

  it('loads the next page when Next is clicked', async () => {
    const user = userEvent.setup();
    notificationApi.list
      .mockResolvedValueOnce(pageResponse([{ id: 1, title: 'Page 1', message: 'First', createdAt: '2026-06-01T09:00:00', isRead: true }], 0, 2))
      .mockResolvedValueOnce(pageResponse([{ id: 2, title: 'Page 2', message: 'Second', createdAt: '2026-06-01T10:00:00', isRead: true }], 1, 2));

    render(
      <MemoryRouter>
        <NotificationPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Page 1')).toBeInTheDocument();
    await user.click(screen.getByTestId('pagination-next'));

    await waitFor(() => {
      expect(notificationApi.list).toHaveBeenLastCalledWith(1);
      expect(screen.getByText('Page 2')).toBeInTheDocument();
    });
  });
});
