package com.gestaofinanceira.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaofinanceira.model.Category;
import com.gestaofinanceira.model.Transaction;
import com.gestaofinanceira.model.TransactionType;
import com.gestaofinanceira.model.User;
import com.gestaofinanceira.repository.TransactionRepository;
import com.gestaofinanceira.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementProcessorServiceTest {

    @Mock
    private GeminiApiService geminiApiService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StatementProcessorService statementProcessorService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("user@test.com");
    }

    @Test
    @DisplayName("Deve processar extrato e salvar transações extraídas pela IA")
    void shouldProcessStatementSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "extrato.pdf", "application/pdf", "conteudo".getBytes());
        
        String aiResponse = """
            [
                {"date": "2024-05-01", "description": "Supermercado", "amount": 150.0, "type": "SAIDA", "recurring": false, "category": "Alimentação", "confidence": 0.98},
                {"date": "2024-05-02", "description": "Salário", "amount": 5000.0, "type": "ENTRADA", "recurring": true, "category": "Outros", "confidence": 1.0}
            ]
            """;

        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(geminiApiService.generateContent(anyString(), anyString(), anyString())).thenReturn(aiResponse);
        when(transactionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Transaction> result = statementProcessorService.processStatement(file, "user@test.com");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Supermercado", result.get(0).getDescription());
        assertEquals(Category.ALIMENTACAO, result.get(0).getCategory());
        assertEquals(TransactionType.SAIDA, result.get(0).getType());
        assertEquals("Salário", result.get(1).getDescription());
        assertEquals(TransactionType.ENTRADA, result.get(1).getType());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a IA retorna JSON inválido")
    void shouldThrowExceptionWhenAiReturnsInvalidJson() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "extrato.pdf", "application/pdf", "conteudo".getBytes());
        String aiResponse = "Erro: Não consegui ler o arquivo";

        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(geminiApiService.generateContent(anyString(), anyString(), anyString())).thenReturn(aiResponse);

        assertThrows(RuntimeException.class, () -> 
            statementProcessorService.processStatement(file, "user@test.com")
        );
    }
}
