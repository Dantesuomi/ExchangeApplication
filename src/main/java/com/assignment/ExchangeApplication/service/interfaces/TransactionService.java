package com.assignment.ExchangeApplication.service.interfaces;

import com.assignment.ExchangeApplication.model.Transaction;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.model.dto.TransactionRequest;
import com.assignment.ExchangeApplication.model.dto.TransferRequest;
import com.assignment.ExchangeApplication.model.dto.TransferResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface TransactionService {
    AccountResponseDto depositAccount (Authentication authentication, TransactionRequest request);
    AccountResponseDto withdrawAccount (Authentication authentication, TransactionRequest request);
    TransferResult transferBetweenAccounts (Authentication authentication, TransferRequest transferRequest);
    Page<Transaction> getTransactionsForAccount(Authentication authentication, UUID accountId, Pageable pageable);
}
