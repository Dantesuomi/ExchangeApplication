package com.assignment.ExchangeApplication.model.dto;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.model.Account;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountResponseDto {

    private UUID id;
    private CurrencyCode currency;
    private BigDecimal balance;
    private UUID clientId;
    private String clientName;
    private String iban;

    public AccountResponseDto(Account account) {
        this.id = account.getId();
        this.currency = account.getCurrency();
        this.balance = account.getBalance();
        this.clientId = account.getClient().getId();
        this.clientName = account.getClient().getName();
        this.iban = account.getIban();
    }
}
