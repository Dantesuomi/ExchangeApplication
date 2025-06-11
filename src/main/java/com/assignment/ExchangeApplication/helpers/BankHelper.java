package com.assignment.ExchangeApplication.helpers;

import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class BankHelper {
    public static final String bankCode = "HABA";

    public static String generateIban() {
        Iban iban = new Iban.Builder()
                .countryCode(CountryCode.LV)
                .bankCode(bankCode)
                .buildRandom();
        return iban.toString();
    }
}
