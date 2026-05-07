package com.gestaofinanceira.dto;

import com.gestaofinanceira.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
    @NotBlank(message = "A descrição é obrigatória")
    String description,
    
    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    BigDecimal amount,
    
    LocalDateTime date,
    
    TransactionType type,
    
    boolean recurring
) {}
