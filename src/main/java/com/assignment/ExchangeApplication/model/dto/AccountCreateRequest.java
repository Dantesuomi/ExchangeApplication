package com.assignment.ExchangeApplication.model.dto;


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
    private Currency currency;
}
