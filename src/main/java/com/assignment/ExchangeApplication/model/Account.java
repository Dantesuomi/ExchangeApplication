package com.assignment.ExchangeApplication.model;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;
    @Enumerated(EnumType.STRING)
    private CurrencyCode currency;
    private BigDecimal balance;
//
//    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
//    private List<Transaction> transactions;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private String iban;

}
