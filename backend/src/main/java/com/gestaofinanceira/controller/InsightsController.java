package com.gestaofinanceira.controller;

import com.gestaofinanceira.model.Transaction;
import com.gestaofinanceira.service.FinancialInsightsService;
import com.gestaofinanceira.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final FinancialInsightsService insightsService;
    private final TransactionService transactionService;

    public InsightsController(FinancialInsightsService insightsService, TransactionService transactionService) {
        this.insightsService = insightsService;
        this.transactionService = transactionService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAssistant(@RequestBody Map<String, String> request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pergunta não pode ser vazia"));
        }

        List<Transaction> transactions = transactionService.getAllTransactionsEntities(jwt.getClaimAsString("email"));
        String answer = insightsService.getPersonalizedAdvice(question, transactions);

        return ResponseEntity.ok(Map.of("answer", answer));
    }
}

