package com.assignment.ExchangeApplication.enums;

public enum TransactionOperation {

    TRANSFER("TRANSFER"),
    DEPOSIT("DEPOSIT"),
    WITHDRAWAL("WITHDRAWAL");

    private final String name;

    TransactionOperation(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
