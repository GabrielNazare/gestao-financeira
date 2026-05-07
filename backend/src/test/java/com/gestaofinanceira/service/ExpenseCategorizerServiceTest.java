package com.gestaofinanceira.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaofinanceira.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseCategorizerServiceTest {

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private GeminiApiService geminiApiService;

    private ExpenseCategorizerService categorizerService;

    @BeforeEach
    void setUp() {
        categorizerService = spy(new ExpenseCategorizerService(geminiApiService, objectMapper));
    }

    @Nested
    @DisplayName("Cenários de Sucesso - Classificação correta")
    class SuccessScenarios {

        @Test
        @DisplayName("Deve classificar 'Posto Ipiranga' como TRANSPORTE")
        void shouldClassifyGasStationAsTransporte() throws Exception {
            String aiResponse = """
                    {"categoria": "Transporte", "confianca": 0.99}
                    """;
            doReturn(aiResponse).when(geminiApiService).generateContent(anyString());

            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense("Posto Ipiranga");

            assertEquals(Category.TRANSPORTE, result.category());
            assertEquals(0, BigDecimal.valueOf(0.99).compareTo(result.confidence()));
        }

        @Test
        @DisplayName("Deve classificar 'Cinema Kinoplex' como LAZER")
        void shouldClassifyCinemaAsLazer() throws Exception {
            String aiResponse = """
                    {"categoria": "Lazer", "confianca": 0.95}
                    """;
            doReturn(aiResponse).when(geminiApiService).generateContent(anyString());

            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense("Cinema Kinoplex");

            assertEquals(Category.LAZER, result.category());
            assertEquals(0, BigDecimal.valueOf(0.95).compareTo(result.confidence()));
        }

        @Test
        @DisplayName("Deve classificar 'iFood Restaurante' como ALIMENTACAO")
        void shouldClassifyFoodDeliveryAsAlimentacao() throws Exception {
            String aiResponse = """
                    {"categoria": "Alimentação", "confianca": 0.97}
                    """;
            doReturn(aiResponse).when(geminiApiService).generateContent(anyString());

            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense("iFood Restaurante");

            assertEquals(Category.ALIMENTACAO, result.category());
            assertEquals(0, BigDecimal.valueOf(0.97).compareTo(result.confidence()));
        }

        @Test
        @DisplayName("Deve lidar com resposta da IA envolta em code block markdown")
        void shouldHandleMarkdownCodeBlockResponse() throws Exception {
            String aiResponse = """
                    ```json
                    {"categoria": "Transporte", "confianca": 0.98}
                    ```
                    """;
            doReturn(aiResponse).when(geminiApiService).generateContent(anyString());

            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense("Uber corrida");

            assertEquals(Category.TRANSPORTE, result.category());
        }
    }

    @Nested
    @DisplayName("Cenários de Fallback - Resiliência do sistema")
    class FallbackScenarios {

        @Test
        @DisplayName("Deve retornar OUTROS quando a API do Google falha com exceção")
        void shouldFallbackWhenApiThrowsException() throws Exception {
            doThrow(new RuntimeException("Falha de conexão com o Gemini"))
                    .when(geminiApiService).generateContent(anyString());

            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense("Posto Ipiranga");

            assertEquals(Category.OUTROS, result.category());
            assertEquals(BigDecimal.ZERO, result.confidence());
        }

        @Test
        @DisplayName("Deve retornar OUTROS quando a IA retorna JSON inválido")
        void shouldFallbackOnInvalidJson() throws Exception {
            doReturn("Isso não é um JSON válido")
                    .when(geminiApiService).generateContent(anyString());

            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense("Cinema");

            assertEquals(Category.OUTROS, result.category());
            assertEquals(BigDecimal.ZERO, result.confidence());
        }

        @Test
        @DisplayName("Deve retornar OUTROS quando a descrição é nula")
        void shouldReturnOutrosForNullDescription() {
            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense(null);

            assertEquals(Category.OUTROS, result.category());
            assertEquals(BigDecimal.ZERO, result.confidence());
        }

        @Test
        @DisplayName("Deve retornar OUTROS quando a descrição é vazia")
        void shouldReturnOutrosForEmptyDescription() {
            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense("   ");

            assertEquals(Category.OUTROS, result.category());
            assertEquals(BigDecimal.ZERO, result.confidence());
        }

        @Test
        @DisplayName("Deve retornar OUTROS quando a IA retorna categoria desconhecida")
        void shouldFallbackOnUnknownCategory() throws Exception {
            String aiResponse = """
                    {"categoria": "CategoriaInventada", "confianca": 0.50}
                    """;
            doReturn(aiResponse).when(geminiApiService).generateContent(anyString());

            ExpenseCategorizerService.ClassificationResult result =
                    categorizerService.categorizeExpense("Algo estranho");

            assertEquals(Category.OUTROS, result.category());
        }
    }

    @Nested
    @DisplayName("Verificação do Prompt")
    class PromptTests {

        @Test
        @DisplayName("O prompt deve conter a descrição do gasto e exemplos de treinamento")
        void promptShouldContainDescriptionAndExamples() {
            String prompt = categorizerService.buildPrompt("Uber Eats");

            assertAll(
                    () -> assertTrue(prompt.contains("Uber Eats")),
                    () -> assertTrue(prompt.contains("Posto Ipiranga")),
                    () -> assertTrue(prompt.contains("Cinema Kinoplex")),
                    () -> assertTrue(prompt.contains("JSON")),
                    () -> assertTrue(prompt.contains("categoria")),
                    () -> assertTrue(prompt.contains("confianca"))
            );
        }
    }
}

