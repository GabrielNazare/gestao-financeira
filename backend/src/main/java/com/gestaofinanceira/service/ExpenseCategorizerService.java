package com.gestaofinanceira.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaofinanceira.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExpenseCategorizerService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseCategorizerService.class);

    private final GeminiApiService geminiApiService;
    private final ObjectMapper objectMapper;

    public record ClassificationResult(Category category, BigDecimal confidence) {
    }

    public ExpenseCategorizerService(GeminiApiService geminiApiService, ObjectMapper objectMapper) {
        this.geminiApiService = geminiApiService;
        this.objectMapper = objectMapper;
    }

    public ClassificationResult categorizeExpense(String expenseDescription) {
        if (expenseDescription == null || expenseDescription.isBlank()) {
            log.warn("Descrição de gasto vazia recebida. Retornando categoria OUTROS.");
            return new ClassificationResult(Category.OUTROS, BigDecimal.ZERO);
        }

        try {
            String prompt = buildPrompt(expenseDescription);
            String aiResponse = geminiApiService.generateContent(prompt);
            return parseResponse(aiResponse);
        } catch (Exception e) {
            log.error("Erro ao classificar gasto via REST: '{}'. Ativando fallback.", expenseDescription, e);
            return fallback();
        }
    }

    String buildPrompt(String expenseDescription) {
        return """
                Você é um assistente financeiro de backend.

                Regra de Formato: Você deve responder estritamente no formato JSON, contendo as chaves "categoria" e "confianca". Não adicione nenhum texto adicional. Use ponto como separador decimal.

                As categorias válidas são: "Alimentação", "Transporte", "Lazer" ou "Outros".

                Exemplos de Treinamento:
                Entrada: 'Posto Ipiranga' -> Saída: {"categoria": "Transporte", "confianca": 0.99}
                Entrada: 'Cinema Kinoplex' -> Saída: {"categoria": "Lazer", "confianca": 0.95}
                Entrada: 'iFood Restaurante' -> Saída: {"categoria": "Alimentação", "confianca": 0.97}
                Entrada: 'Uber corrida' -> Saída: {"categoria": "Transporte", "confianca": 0.98}
                Entrada: 'Supermercado Extra' -> Saída: {"categoria": "Alimentação", "confianca": 0.96}

                Agora, classifique a entrada a seguir: '%s'
                """.formatted(expenseDescription);
    }

    ClassificationResult parseResponse(String aiResponse) {
        try {
            String cleaned = aiResponse.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }

            JsonNode json = objectMapper.readTree(cleaned);

            String categoriaText = json.has("categoria") ? json.get("categoria").asText() : "";
            double confiancaValue = json.has("confianca") ? json.get("confianca").asDouble() : 0.0;

            Category category = Category.fromText(categoriaText);
            BigDecimal confidence = BigDecimal.valueOf(confiancaValue);

            log.info("Classificação: {} (confiança: {})", category.getDisplayName(), confidence);
            return new ClassificationResult(category, confidence);

        } catch (Exception e) {
            log.warn("Falha ao fazer parsing da resposta da IA: '{}'. Ativando fallback.", aiResponse, e);
            return fallback();
        }
    }

    ClassificationResult fallback() {
        log.info("Fallback ativado: retornando categoria OUTROS com confiança 0.");
        return new ClassificationResult(Category.OUTROS, BigDecimal.ZERO);
    }
}

