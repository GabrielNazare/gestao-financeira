package com.gestaofinanceira.controller;

import com.gestaofinanceira.dto.TransactionRequest;
import com.gestaofinanceira.dto.TransactionResponse;
import com.gestaofinanceira.service.TransactionService;
import com.gestaofinanceira.service.StatementProcessorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final StatementProcessorService statementProcessorService;

    public TransactionController(TransactionService transactionService, StatementProcessorService statementProcessorService) {
        this.transactionService = transactionService;
        this.statementProcessorService = statementProcessorService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<TransactionResponse>> uploadStatement(@RequestParam("file") MultipartFile file, 
                                                                    @AuthenticationPrincipal Jwt jwt) throws Exception {
        String userEmail = jwt.getClaimAsString("email");
        var imported = statementProcessorService.processStatement(file, userEmail);
        return ResponseEntity.ok(imported.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request, 
                                                                 @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(request, userEmail));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(@AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        return ResponseEntity.ok(transactionService.getAllTransactions(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id, 
                                                                 @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        return ResponseEntity.ok(transactionService.getTransactionById(id, userEmail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, 
                                                                 @Valid @RequestBody TransactionRequest request, 
                                                                 @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        return ResponseEntity.ok(transactionService.updateTransaction(id, request, userEmail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, 
                                                  @AuthenticationPrincipal Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        transactionService.deleteTransaction(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}

