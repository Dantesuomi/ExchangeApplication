package com.assignment.ExchangeApplication.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.iban4j.Iban;

import java.math.BigDecimal;

public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    private Iban sourceAccountNumber;

    @NotBlank(message = "Destination account number is required")
    private Iban destinationAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private BigDecimal amount;

    private String description;
}
