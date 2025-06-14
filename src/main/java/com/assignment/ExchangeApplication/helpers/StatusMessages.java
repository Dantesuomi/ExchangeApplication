package com.assignment.ExchangeApplication.helpers;

public class StatusMessages {
    public static final String UNAUTHORIZED_ACCOUNT_ERROR = "You are not authorized to transfer funds from this account";
    public static final String INVALID_CURRENCY_ERROR = "The currency of funds in the transfer operation must match the receiver's account currency";
    public static final String INSUFFICIENT_BALANCE_ERROR = "Insufficient balance";
    public static final String TRANSFER_SUCCEEDED = "Transfer Performed Successfully";
    public static final String EXCHANGE_UNAVAILABLE_ERROR = "Exchange rates API unavailable";
    public static final String RETRIEVE_EXCHANGE_RATE_ERROR = "Unable to retrieve exchange rates";
    public static final String SOURCE_ACCOUNT_NOT_FOUND_ERROR = "Source account not found";
    public static final String DESTINATION_ACCOUNT_NOT_FOUND_ERROR = "Destination account not found";
    public static final String ACCOUNT_NOT_FOUND_ERROR = "Account not found";
    public static final String IDENTICAL_SOURCE_AND_DESTINATION_ACCOUNT_ERROR = "Source and destination account are identical";
    public static final String TRANSFER_ERROR = "Failed to perform transfer, transaction has been rolled back";
}
