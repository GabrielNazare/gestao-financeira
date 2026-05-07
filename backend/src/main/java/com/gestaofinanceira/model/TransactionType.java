package com.gestaofinanceira.model;

public enum TransactionType {
    ENTRADA("Entrada"),
    SAIDA("Saída");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TransactionType fromText(String text) {
        if (text == null || text.isBlank()) {
            return SAIDA;
        }
        String normalized = text.trim().toLowerCase();
        if (normalized.contains("entrada") || normalized.contains("receita") || normalized.contains("salario") || normalized.contains("deposito")) {
            return ENTRADA;
        }
        return SAIDA;
    }
}

