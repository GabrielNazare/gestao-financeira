package com.gestaofinanceira.service;

import com.gestaofinanceira.dto.TransactionRequest;
import com.gestaofinanceira.dto.TransactionResponse;
import com.gestaofinanceira.exception.ResourceNotFoundException;
import com.gestaofinanceira.exception.UnauthorizedException;
import com.gestaofinanceira.model.Category;
import com.gestaofinanceira.model.Transaction;
import com.gestaofinanceira.model.TransactionType;
import com.gestaofinanceira.model.User;
import com.gestaofinanceira.repository.TransactionRepository;
import com.gestaofinanceira.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExpenseCategorizerService categorizerService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail(userEmail);
        testUser.setName("Test User");
    }

    @Nested
    @DisplayName("Criação de Transações")
    class CreationTests {

        @Test
        @DisplayName("Deve criar transação com sucesso e categorizar automaticamente")
        void shouldCreateTransactionSuccessfully() {
            // Arrange
            TransactionRequest request = new TransactionRequest(
                    "Almoço no Shopping",
                    new BigDecimal("50.00"),
                    null,
                    TransactionType.SAIDA,
                    false
            );

            when(userRepository.findById(userEmail)).thenReturn(Optional.of(testUser));
            
            ExpenseCategorizerService.ClassificationResult classification = 
                    new ExpenseCategorizerService.ClassificationResult(Category.ALIMENTACAO, new BigDecimal("0.95"));
            when(categorizerService.categorizeExpense(anyString())).thenReturn(classification);

            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            // Act
            TransactionResponse response = transactionService.createTransaction(request, userEmail);

            // Assert
            assertNotNull(response);
            assertEquals("Almoço no Shopping", response.description());
            assertEquals(Category.ALIMENTACAO, response.category());
            assertEquals(new BigDecimal("0.95"), response.confidence());
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não existe")
        void shouldThrowExceptionWhenUserNotFound() {
            TransactionRequest request = new TransactionRequest("Teste", BigDecimal.TEN, null, null, false);
            when(userRepository.findById(anyString())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> 
                transactionService.createTransaction(request, "ghost@example.com")
            );
        }
    }

    @Nested
    @DisplayName("Segurança e Permissões")
    class SecurityTests {

        @Test
        @DisplayName("Não deve permitir acessar transação de outro usuário")
        void shouldNotAllowAccessingOtherUserTransaction() {
            // Arrange
            Long transactionId = 1L;
            User otherUser = new User();
            otherUser.setEmail("other@example.com");

            Transaction transaction = new Transaction("Gasto Alheio", BigDecimal.TEN);
            transaction.setUser(otherUser);

            when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

            // Act & Assert
            assertThrows(UnauthorizedException.class, () -> 
                transactionService.getTransactionById(transactionId, userEmail)
            );
        }

        @Test
        @DisplayName("Não deve permitir deletar transação de outro usuário")
        void shouldNotAllowDeletingOtherUserTransaction() {
            Long transactionId = 1L;
            User otherUser = new User();
            otherUser.setEmail("other@example.com");

            Transaction transaction = new Transaction("Gasto Alheio", BigDecimal.TEN);
            transaction.setUser(otherUser);

            when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

            assertThrows(UnauthorizedException.class, () -> 
                transactionService.deleteTransaction(transactionId, userEmail)
            );
            verify(transactionRepository, never()).delete(any());
        }
    }
}
