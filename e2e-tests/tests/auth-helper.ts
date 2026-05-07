import { Page } from '@playwright/test';

export async function mockLogin(page: Page) {
  const fakeToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwibmFtZSI6IlRlc3QgVXNlciIsImlhdCI6MTUxNjIzOTAyMn0.fake-signature';
  
  await page.goto('/');
  await page.evaluate((token) => {
    localStorage.setItem('token', token);
  }, fakeToken);
  
  await page.reload();
}
