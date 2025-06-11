package com.assignment.ExchangeApplication.model;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.enums.TransactionOperation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    BigDecimal balance;

    private LocalDateTime timestamp;

    private String description;

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    private CurrencyCode sourceCurrencyCode;

    private CurrencyCode destinationCurrencyCode;

    private TransactionOperation transactionOperation;

    @NotNull
    BigDecimal sourceAmountDebited;
    @NotNull
    BigDecimal destinationAmountCredited;


}
