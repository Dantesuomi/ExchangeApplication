package com.assignment.ExchangeApplication.enums;

public enum TransactionOperation {

    SEND("SEND"),
    RECEIVE("RECEIVE");

    private final String name;

    TransactionOperation(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
