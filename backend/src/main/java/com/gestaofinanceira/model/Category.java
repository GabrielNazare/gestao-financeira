package com.gestaofinanceira.model;

public enum Category {

    ALIMENTACAO("Alimentação"),
    TRANSPORTE("Transporte"),
    LAZER("Lazer"),
    OUTROS("Outros");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromText(String text) {
        if (text == null || text.isBlank()) {
            return OUTROS;
        }

        String normalized = text.trim().toLowerCase();

        if (normalized.contains("alimenta")) {
            return ALIMENTACAO;
        } else if (normalized.contains("transporte")) {
            return TRANSPORTE;
        } else if (normalized.contains("lazer")) {
            return LAZER;
        }

        return OUTROS;
    }
}

