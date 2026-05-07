package com.gestaofinanceira.repository;

import com.gestaofinanceira.model.Category;
import com.gestaofinanceira.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("gestao_financeira_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve salvar e recuperar uma transação do banco de dados")
    void shouldSaveAndRetrieveTransaction() {
        Transaction transaction = new Transaction(
                "Supermercado Extra",
                new BigDecimal("150.75"),
                Category.ALIMENTACAO,
                new BigDecimal("0.96")
        );

        Transaction saved = transactionRepository.save(transaction);

        assertNotNull(saved.getId());
        assertEquals("Supermercado Extra", saved.getDescription());
        assertEquals(Category.ALIMENTACAO, saved.getCategory());
    }

    @Test
    @DisplayName("Deve buscar transações por categoria")
    void shouldFindTransactionsByCategory() {
        transactionRepository.save(new Transaction("Uber", new BigDecimal("25.00"), Category.TRANSPORTE, new BigDecimal("0.98")));
        transactionRepository.save(new Transaction("Posto Shell", new BigDecimal("200.00"), Category.TRANSPORTE, new BigDecimal("0.99")));
        transactionRepository.save(new Transaction("iFood", new BigDecimal("45.90"), Category.ALIMENTACAO, new BigDecimal("0.97")));

        List<Transaction> transporteTransactions = transactionRepository.findByCategory(Category.TRANSPORTE);

        assertEquals(2, transporteTransactions.size());
        assertTrue(transporteTransactions.stream()
                .allMatch(t -> t.getCategory() == Category.TRANSPORTE));
    }

    @Test
    @DisplayName("Deve buscar transações por período de datas")
    void shouldFindTransactionsByDateRange() {
        Transaction t1 = new Transaction("Cinema", new BigDecimal("50.00"), Category.LAZER, new BigDecimal("0.95"));
        t1.setDate(LocalDateTime.of(2024, 6, 1, 10, 0));

        Transaction t2 = new Transaction("Restaurante", new BigDecimal("80.00"), Category.ALIMENTACAO, new BigDecimal("0.96"));
        t2.setDate(LocalDateTime.of(2024, 6, 15, 20, 0));

        Transaction t3 = new Transaction("Uber", new BigDecimal("30.00"), Category.TRANSPORTE, new BigDecimal("0.98"));
        t3.setDate(LocalDateTime.of(2024, 7, 1, 8, 0));

        transactionRepository.saveAll(List.of(t1, t2, t3));

        List<Transaction> juneTransactions = transactionRepository.findByDateBetween(
                LocalDateTime.of(2024, 6, 1, 0, 0),
                LocalDateTime.of(2024, 6, 30, 23, 59)
        );

        assertEquals(2, juneTransactions.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há transações para a categoria")
    void shouldReturnEmptyListForCategoryWithNoTransactions() {
        List<Transaction> result = transactionRepository.findByCategory(Category.LAZER);

        assertTrue(result.isEmpty());
    }
}

