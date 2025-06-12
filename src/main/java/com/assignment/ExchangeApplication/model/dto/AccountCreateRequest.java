package com.assignment.ExchangeApplication.model.dto;


import com.assignment.ExchangeApplication.enums.CurrencyCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Currency;

@Data
@AllArgsConstructor
public class AccountCreateRequest {
    @NotNull
    @NotEmpty
    private CurrencyCode currency;
}
