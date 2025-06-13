package com.assignment.ExchangeApplication.enums;

public enum TransferType {
    RECEIVED("RECEIVED"),
    SENT("SENT");

    private final String name;

    TransferType(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
