package com.gestaofinanceira.repository;

import com.gestaofinanceira.model.Category;
import com.gestaofinanceira.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserEmail(String email);

    List<Transaction> findByCategory(Category category);

    List<Transaction> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findByCategoryAndDateBetween(Category category, LocalDateTime startDate, LocalDateTime endDate);
}

