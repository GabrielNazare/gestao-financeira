package com.gestaofinanceira.service;

import com.gestaofinanceira.model.Category;
import com.gestaofinanceira.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialInsightsServiceTest {

    @Mock
    private GeminiApiService geminiApiService;

    @InjectMocks
    private FinancialInsightsService financialInsightsService;

    @Test
    @DisplayName("Deve gerar conselhos financeiros baseados no histórico")
    void shouldGenerateAdviceSuccessfully() {

        Transaction t1 = new Transaction("Uber", new BigDecimal("30.0"));
        t1.setCategory(Category.TRANSPORTE);
        t1.setDate(LocalDateTime.now());

        List<Transaction> history = List.of(t1);
        String mockAdvice = "Seu gasto com transporte está moderado.";

        when(geminiApiService.generateContent(anyString())).thenReturn(mockAdvice);

        String advice = financialInsightsService.getPersonalizedAdvice("Como estão meus gastos?", history);

        assertTrue(advice.contains(mockAdvice));
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro amigável quando a API falha")
    void shouldReturnFriendlyErrorOnApiFailure() {

        when(geminiApiService.generateContent(anyString())).thenThrow(new RuntimeException("API Down"));

        String advice = financialInsightsService.getPersonalizedAdvice("Teste", List.of());

        assertTrue(advice.contains("Desculpe, tive um problema"));
    }
}
