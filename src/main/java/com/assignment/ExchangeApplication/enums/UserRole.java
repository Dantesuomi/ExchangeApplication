package com.assignment.ExchangeApplication.enums;

public enum UserRole {

    USER("ROLE_USER");

    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
