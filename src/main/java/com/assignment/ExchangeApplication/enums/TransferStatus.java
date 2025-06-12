package com.assignment.ExchangeApplication.enums;

public enum TransferStatus {
    FAILED("FAILED"),
    SUCCESSFUL("SUCCESSFUL");

    private final String name;

    TransferStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
