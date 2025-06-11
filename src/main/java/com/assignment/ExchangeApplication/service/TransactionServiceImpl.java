package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.exceptions.NegativeAmountException;
import com.assignment.ExchangeApplication.exceptions.PermissionDeniedException;
import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.model.dto.TransactionRequest;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import com.assignment.ExchangeApplication.service.interfaces.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountService accountService;

    public TransactionServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public AccountResponseDto depositAccount(Authentication authentication, TransactionRequest request) {
        //TODO Extract Account validation into separate method
        Account account = accountService.getAccountByIban(request.getAccountIban());
        UUID accountOwner = account.getClient().getId();
        Client client = (Client) authentication.getPrincipal();
        UUID authorizedClientId = client.getId();
        if(!accountOwner.equals(authorizedClientId)){
            throw new PermissionDeniedException("User not authorized to perform this operation");
        }

        BigDecimal amountToDeposit = request.getAmount();
        BigDecimal currentAccountBalance = account.getBalance();
        BigDecimal newAccountBalance = currentAccountBalance.add(amountToDeposit);
        account.setBalance(newAccountBalance);
        accountService.updateAccount(account);

        return new AccountResponseDto(account);
    }

    @Override
    public AccountResponseDto withdrawAccount(Authentication authentication, TransactionRequest request) {
        Account account = accountService.getAccountByIban(request.getAccountIban());
        UUID accountOwner = account.getClient().getId();
        Client client = (Client) authentication.getPrincipal();
        UUID authorizedClientId = client.getId();
        if(!accountOwner.equals(authorizedClientId)){
            throw new PermissionDeniedException("User not authorized to perform this operation");
        }

        BigDecimal amountToWithdraw = request.getAmount();
        BigDecimal currentAccountBalance = account.getBalance();
        BigDecimal newAccountBalance = currentAccountBalance.subtract(amountToWithdraw);
        if(newAccountBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeAmountException("Balance cannot be less than zero");
        }
        account.setBalance(newAccountBalance);
        accountService.updateAccount(account);

        return new AccountResponseDto(account);
    }
}
