package com.assignment.ExchangeApplication.enums;

public enum CurrencyCode {

    USD("USD"), // US Dollar
    EUR("EUR"), // Euro
    GBP("GBP"), // British Pound
    UAH("UAH"), // Ukrainian Hryvna
    JPY("JPY"), // Japanese Yen
    CAD("CAD"); // Canadian Dollar

    private final String name;

    CurrencyCode(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
