package com.gestaofinanceira.service;

import com.gestaofinanceira.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialInsightsService {
    private static final Logger log = LoggerFactory.getLogger(FinancialInsightsService.class);

    private final GeminiApiService geminiApiService;

    public FinancialInsightsService(GeminiApiService geminiApiService) {
        this.geminiApiService = geminiApiService;
    }

    public String getPersonalizedAdvice(String userQuestion, List<Transaction> history) {
        try {
            String prompt = buildPrompt(userQuestion, history);
            return geminiApiService.generateContent(prompt);
        } catch (Exception e) {
            log.error("Erro ao gerar insights via API REST", e);
            return "Desculpe, tive um problema ao tentar analisar seus dados. Verifique a conexão com a API do Google AI Studio.";
        }
    }

    private String buildPrompt(String userQuestion, List<Transaction> history) {
        String historySummary = history.stream()
                .map(t -> String.format("- %s: R$ %.2f (%s em %s)",
                        t.getDescription(), t.getAmount(), t.getCategory().getDisplayName(), t.getDate().toLocalDate()))
                .collect(Collectors.joining("\n"));

        if (historySummary.isBlank()) {
            historySummary = "O usuário ainda não tem gastos registrados.";
        }

        return """
                Você é um assistente financeiro inteligente que ajuda o usuário a entender seus gastos e a economizar dinheiro.
                Você deve ser direto, prestativo e amigável. Formate a resposta usando Markdown.

                Aqui está o histórico de transações do usuário:
                %s

                Com base nisso, responda à seguinte pergunta do usuário de forma clara e objetiva:
                "%s"
                """.formatted(historySummary, userQuestion);
    }
}

