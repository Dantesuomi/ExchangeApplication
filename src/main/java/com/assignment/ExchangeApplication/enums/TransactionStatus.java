package com.assignment.ExchangeApplication.enums;

public enum TransactionStatus {

    ALLOWED("ALLOWED"),
    PROHIBITED("PROHIBITED");

    private final String name;

    TransactionStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
