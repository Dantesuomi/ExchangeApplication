package com.assignment.ExchangeApplication.exceptions;

public class FailedAccountUpdateException extends RuntimeException {
    public FailedAccountUpdateException(String message) {
        super(message);
    }
}
