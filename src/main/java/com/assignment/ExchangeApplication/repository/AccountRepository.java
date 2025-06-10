package com.assignment.ExchangeApplication.repository;

import com.assignment.ExchangeApplication.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
