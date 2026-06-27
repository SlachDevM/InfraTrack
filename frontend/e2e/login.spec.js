import { test, expect } from '@playwright/test';

test('login page loads with form visible', async ({ page }) => {
  await page.goto('/login');

  await expect(page.getByRole('heading', { name: 'InfraTrack' })).toBeVisible();
  await expect(page.getByPlaceholder('you@example.com')).toBeVisible();
  await expect(page.getByPlaceholder('••••••••')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
});
