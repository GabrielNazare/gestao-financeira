# Gestão Financeira — Backend com IA

Sistema de gestão financeira inteligente construído com **Java 17** e **Spring Boot 3**, integrado ao **Google Cloud Vertex AI (Gemini Pro)** para classificação automática de gastos por categoria.

---

## Stack Tecnológica

| Tecnologia | Finalidade |
|---|---|
| **Java 17** | Linguagem principal |
| **Spring Boot 3** | Framework backend (Web + Data JPA) |
| **PostgreSQL 16** | Banco de dados relacional para persistência |
| **Google Cloud Vertex AI** | IA generativa (Gemini Pro) para categorização de gastos |
| **Docker Compose** | Provisionamento do banco de dados local |
| **JUnit 5 + Mockito** | Testes unitários com mocking da API de IA |
| **Testcontainers** | Testes de integração com banco PostgreSQL efêmero |
| **Maven** | Gerenciamento de dependências e build |
| **Playwright** | Automação de testes End-to-End (E2E) |

---

## Como a IA Funciona

O sistema utiliza um prompt **few-shot** com formato JSON estruturado para garantir respostas consistentes e parseáveis:

```
Entrada: "Uber Eats"
Saída:   {"categoria": "Alimentação", "confianca": 0.97}
```

### Categorias Suportadas
- 🍔 **Alimentação** — Restaurantes, supermercados, delivery
- 🚗 **Transporte** — Uber, postos de gasolina, pedágios
- 🎬 **Lazer** — Cinema, shows, parques
- ❓ **Outros** — Fallback de segurança

### Mecanismo de Fallback
Se a API do Vertex AI falhar (timeout, erro de rede, resposta inválida), o sistema automaticamente classifica o gasto como **"Outros"** com confiança **0**, garantindo que a aplicação nunca quebre.

---

## Como Executar

### Pré-requisitos
- **Java 17+** instalado
- **Maven 3.8+** instalado
- **Docker** e **Docker Compose** instalados
- **Conta Google Cloud** com Vertex AI habilitado

### 1. Configurar Credenciais do Google Cloud

```bash
# Opção A: Autenticação via gcloud CLI
gcloud auth application-default login

# Opção B: Variável de ambiente com Service Account
export GOOGLE_APPLICATION_CREDENTIALS="/caminho/para/sua-chave.json"
```

### 2. Subir o Banco de Dados

```bash
docker-compose up -d
```

### 3. Configurar Variáveis de Ambiente (opcional)

```bash
export GCP_PROJECT_ID=seu-projeto-gcp
export GCP_LOCATION=us-central1
export GCP_MODEL_NAME=gemini-pro
```

### 4. Rodar a Aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

---

## API REST

### Criar Transação (com classificação automática)

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"description": "Uber Eats", "amount": 45.90}'
```

**Resposta:**
```json
{
  "id": 1,
  "description": "Uber Eats",
  "amount": 45.90,
  "date": "2024-06-15T20:30:00",
  "category": "ALIMENTACAO",
  "confidence": 0.97
}
```

### Listar Transações

```bash
curl http://localhost:8080/api/transactions
```

### Buscar por ID

```bash
curl http://localhost:8080/api/transactions/1
```

---

## Qualidade e Testes (QA Focus)

Este projeto foi desenvolvido com foco rigoroso em qualidade de software, seguindo princípios de **Clean Architecture** e **Test-Driven Development (TDD)** onde aplicável.


### O que testamos?
- **Testes Unitários (JUnit 5 + Mockito):**
    - Lógica de negócio no `TransactionService`.
    - Resiliência e Fallback no `ExpenseCategorizerService` (QA para IA).
    - Mocks de APIs externas para garantir independência.
- **Testes de Integração (Testcontainers + Docker):**
    - Persistência real no PostgreSQL.
    - Validação de queries complexas e relacionamentos JPA.
- **Testes de IA:**
    - Validação de prompts (Few-shot prompting).
    - Testes de precisão de extração de dados de PDFs.
- **Testes End-to-End (Playwright):**
    - Automação de fluxos críticos de usuário na interface.
    - Mocking de APIs para simulação de estados complexos (Stateful Mocking).

### Como rodar os testes
```bash
mvn test
```

---

---

---

## Estrutura do Projeto
```
gestaoFinanceira/
├── docker-compose.yml
├── backend/
│   └── src/test/java/...    # Implementação dos testes automatizados
├── frontend/                # Interface React com validações de UI
└── e2e-tests/               # Suíte de testes End-to-End com Playwright
```
