package com.assignment.ExchangeApplication.service.interfaces;

import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.model.dto.TransactionRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {
    AccountResponseDto depositAccount (Authentication authentication, TransactionRequest request);
    AccountResponseDto withdrawAccount (Authentication authentication, TransactionRequest request);
}
