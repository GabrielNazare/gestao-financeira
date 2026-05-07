import { test, expect } from '@playwright/test';

test.describe('Landing Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should display the main title and features', async ({ page }) => {
    const title = page.locator('h1.hero-title');
    await expect(title).toContainText('Gestão Financeira');
    
    const loginBtn = page.getByRole('button', { name: /Entrar/i });
    await expect(loginBtn).toBeVisible();
  });

  test('should toggle language from PT to EN', async ({ page }) => {
    const langBtn = page.locator('button[title="Switch Language"]');
    
    await expect(page.locator('h1.hero-title')).toContainText('Gestão Financeira Inteligente');
    
    await langBtn.click();
    
    await expect(page.locator('h1.hero-title')).toContainText('Smart Financial Management');
    
    await langBtn.click();
    await expect(page.locator('h1.hero-title')).toContainText('Gestão Financeira Inteligente');
  });

  test('should show register form when clicking register link', async ({ page }) => {
    const registerLink = page.getByRole('button', { name: /Cadastre-se/i });
    await registerLink.click();
    
    const registerBtn = page.getByRole('button', { name: /Criar Conta/i });
    await expect(registerBtn).toBeVisible();
    
    const nameInput = page.getByPlaceholder(/Seu nome/i);
    await expect(nameInput).toBeVisible();
  });
});
