import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
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

describe('NotificationPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    notificationApi.list.mockResolvedValue([]);
    notificationApi.markAsRead.mockResolvedValue({});
  });

  it('renders notifications from notificationApi', async () => {
    notificationApi.list.mockResolvedValue([
      {
        id: 1,
        title: 'Inspection Assigned',
        message: 'You have a new inspection.',
        createdAt: '2026-06-01T09:00:00',
        isRead: false,
        targetRoute: null,
      },
    ]);

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
    notificationApi.list.mockResolvedValue([
      {
        id: 2,
        title: 'Work Order Assigned',
        message: 'Work order #100 assigned.',
        createdAt: '2026-06-01T10:00:00',
        isRead: false,
        targetRoute: '/work-orders',
      },
    ]);

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
    notificationApi.list.mockResolvedValue([
      {
        id: 3,
        title: 'General Notice',
        message: 'System maintenance tonight.',
        createdAt: '2026-06-01T11:00:00',
        isRead: true,
        targetRoute: null,
      },
    ]);

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
});
