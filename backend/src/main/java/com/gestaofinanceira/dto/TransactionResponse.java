package com.gestaofinanceira.dto;

import com.gestaofinanceira.model.Category;
import com.gestaofinanceira.model.Transaction;
import com.gestaofinanceira.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    Long id,
    String description,
    BigDecimal amount,
    LocalDateTime date,
    Category category,
    TransactionType type,
    boolean recurring,
    BigDecimal confidence
) {
    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getDate(),
            transaction.getCategory(),
            transaction.getType(),
            transaction.isRecurring(),
            transaction.getConfidence()
        );
    }
}
