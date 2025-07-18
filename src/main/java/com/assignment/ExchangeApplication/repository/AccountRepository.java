package com.assignment.ExchangeApplication.repository;

import com.assignment.ExchangeApplication.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findByIban(String iban);

}
