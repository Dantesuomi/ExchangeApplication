package com.assignment.ExchangeApplication.model.dao;

import com.assignment.ExchangeApplication.enums.TransferType;
import com.assignment.ExchangeApplication.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TransactionResponse extends Transaction {

    private TransferType transferType;

    public TransactionResponse(Transaction transaction, TransferType transferType){
        super(transaction);
        this.transferType = transferType;
    }
}
