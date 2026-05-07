package com.gestaofinanceira.service;

import com.gestaofinanceira.dto.TransactionRequest;
import com.gestaofinanceira.dto.TransactionResponse;
import com.gestaofinanceira.exception.ResourceNotFoundException;
import com.gestaofinanceira.exception.UnauthorizedException;
import com.gestaofinanceira.model.Transaction;
import com.gestaofinanceira.model.User;
import com.gestaofinanceira.repository.TransactionRepository;
import com.gestaofinanceira.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final ExpenseCategorizerService categorizerService;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              ExpenseCategorizerService categorizerService,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categorizerService = categorizerService;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, String userEmail) {
        log.info("Criando transação: '{}'", request.description());

        User user = userRepository.findById(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userEmail));

        Transaction transaction = new Transaction(request.description(), request.amount());
        
        if (request.date() != null) {
            transaction.setDate(request.date());
        }
        if (request.type() != null) {
            transaction.setType(request.type());
        }
        transaction.setRecurring(request.recurring());
        transaction.setUser(user);

        ExpenseCategorizerService.ClassificationResult result =
                categorizerService.categorizeExpense(transaction.getDescription());

        transaction.setCategory(result.category());
        transaction.setConfidence(result.confidence());

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transação salva com sucesso: {}", saved);

        return TransactionResponse.fromEntity(saved);
    }

    public List<TransactionResponse> getAllTransactions(String userEmail) {
        return getAllTransactionsEntities(userEmail).stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<Transaction> getAllTransactionsEntities(String userEmail) {
        return transactionRepository.findByUserEmail(userEmail);
    }

    public TransactionResponse getTransactionById(Long id, String userEmail) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com id: " + id));
        
        if (transaction.getUser() != null && !transaction.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Você não tem permissão para acessar esta transação");
        }
        
        return TransactionResponse.fromEntity(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, String userEmail) {
        log.info("Atualizando transação {}: '{}'", id, request.description());
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com id: " + id));

        if (transaction.getUser() != null && !transaction.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Você não tem permissão para alterar esta transação");
        }

        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());
        if (request.date() != null) {
            transaction.setDate(request.date());
        }
        if (request.type() != null) {
            transaction.setType(request.type());
        }
        transaction.setRecurring(request.recurring());

        ExpenseCategorizerService.ClassificationResult result =
                categorizerService.categorizeExpense(transaction.getDescription());

        transaction.setCategory(result.category());
        transaction.setConfidence(result.confidence());

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transação atualizada com sucesso: {}", saved);
        return TransactionResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteTransaction(Long id, String userEmail) {
        log.info("Deletando transação com id: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com id: " + id));

        if (transaction.getUser() != null && !transaction.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Você não tem permissão para excluir esta transação");
        }
        
        transactionRepository.delete(transaction);
        log.info("Transação deletada com sucesso.");
    }
}

