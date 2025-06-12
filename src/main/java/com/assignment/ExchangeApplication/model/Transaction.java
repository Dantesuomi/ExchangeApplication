package com.assignment.ExchangeApplication.model;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.enums.TransactionOperation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    private LocalDateTime timestamp;

    private String description;

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Enumerated(EnumType.STRING)
    private CurrencyCode sourceCurrencyCode;

    @Enumerated(EnumType.STRING)
    private TransactionOperation transactionOperation;

    @Enumerated(EnumType.STRING)
    private CurrencyCode destinationCurrencyCode;

    @NotNull
    private BigDecimal sourceAmountDebited;
    @NotNull
    private BigDecimal destinationAmountCredited;

}
