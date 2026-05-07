package com.gestaofinanceira.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{transaction.description.required}")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "{transaction.amount.required}")
    @Positive(message = "{transaction.amount.positive}")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "varchar(20) default 'SAIDA'")
    private TransactionType type = TransactionType.SAIDA;

    @Column(name = "is_recurring", columnDefinition = "boolean default false")
    private boolean isRecurring = false;

    @Column(precision = 4, scale = 2)
    private BigDecimal confidence;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email")
    private User user;

    public Transaction() {
        this.date = LocalDateTime.now();
        this.category = Category.OUTROS;
        this.type = TransactionType.SAIDA;
        this.isRecurring = false;
    }

    public Transaction(String description, BigDecimal amount) {
        this();
        this.description = description;
        this.amount = amount;
    }

    public Transaction(String description, BigDecimal amount, Category category, BigDecimal confidence) {
        this(description, amount);
        this.category = category;
        this.confidence = confidence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", category=" + category +
                ", type=" + type +
                ", isRecurring=" + isRecurring +
                ", confidence=" + confidence +
                '}';
    }
}

