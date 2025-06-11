package com.assignment.ExchangeApplication.repository;

import com.assignment.ExchangeApplication.model.Transaction;
import org.iban4j.Iban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {


}
