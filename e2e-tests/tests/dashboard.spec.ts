import { test, expect } from '@playwright/test';
import { mockLogin } from './auth-helper';

test.describe('Dashboard', () => {
  let transactions: any[] = [];

  test.beforeEach(async ({ page }) => {
    transactions = [
      { id: 1, description: 'Salário', amount: 5000, date: '2026-05-01T10:00:00', type: 'ENTRADA', category: 'SALARIO', confidence: 1.0, recurring: true },
      { id: 2, description: 'Aluguel', amount: 1500, date: '2026-05-02T10:00:00', type: 'SAIDA', category: 'MORADIA', confidence: 1.0, recurring: true },
    ];

    await page.route(/\/api\/transactions/, async route => {
      const method = route.request().method();
      const url = route.request().url();

      if (method === 'GET' && url.match(/\/api\/transactions$/)) {
        await route.fulfill({ json: transactions });
      } 
      else if (method === 'POST' && url.match(/\/api\/transactions$/)) {
        const body = route.request().postDataJSON();
        const newItem = { 
          id: Math.floor(Math.random() * 10000),
          category: 'OUTROS', 
          confidence: 0.95,
          date: new Date().toISOString(),
          type: 'SAIDA',
          ...body 
        };
        transactions.push(newItem);
        await route.fulfill({ json: newItem });
      } 
      else if (method === 'PUT' && url.match(/\/api\/transactions\/\d+$/)) {
        const id = parseInt(url.split('/').pop() || '0');
        const body = route.request().postDataJSON();
        transactions = transactions.map(t => t.id === id ? { ...t, ...body } : t);
        await route.fulfill({ json: body });
      } 
      else if (method === 'DELETE' && url.match(/\/api\/transactions\/\d+$/)) {
        const id = parseInt(url.split('/').pop() || '0');
        transactions = transactions.filter(t => t.id !== id);
        await route.fulfill({ status: 204 });
      } 
      else {
        await route.continue();
      }
    });

    await mockLogin(page);
  });

  test('should display summary cards correctly', async ({ page }) => {
    const incomeCard = page.locator('.metric-card').filter({ hasText: /Receitas|Incomes/i });
    await expect(incomeCard.locator('.metric-value')).toContainText('5000,00');
  });

  test('should add a manual transaction', async ({ page }) => {
    await page.getByPlaceholder(/Uber, Mercado, Netflix/i).fill('Supermercado');
    await page.getByPlaceholder('0.00').fill('250');
    await page.getByRole('button', { name: /Adicionar Gasto/i }).click();

    const row = page.locator('tr').filter({ hasText: 'Supermercado' });
    await expect(row).toBeVisible();
    await expect(row).toContainText('250,00');
  });

  test('should open edit modal and save changes', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: 'Aluguel' });
    await row.locator('button[title="Editar"]').click();

    await page.locator('.modal-content').getByPlaceholder('0.00').fill('1600');
    await page.locator('.modal-content').getByRole('button', { name: /Salvar/i }).click();

    const updatedRow = page.locator('tr').filter({ hasText: 'Aluguel' });
    await expect(updatedRow).toContainText('1600,00');
  });

  test('should interact with AI Assistant', async ({ page }) => {
    await page.route('**/api/insights/ask', async route => {
      await route.fulfill({ json: { answer: 'Você gastou mais em **Moradia** este mês.' } });
    });

    const aiInput = page.getByPlaceholder(/Onde posso economizar mais?/i);
    await aiInput.fill('Qual meu maior gasto?');
    await page.locator('.ai-send-btn').click();

    await expect(page.locator('.ai-response')).toContainText('Você gastou mais em Moradia este mês');
  });
});
