package com.nexusbank.account.domain.model;

import com.nexusbank.account.domain.exception.InvalidAccountNumberException;

import java.util.regex.Pattern;

/**
 * Value Object: número visible de la cuenta (el que ve el cliente, distinto del id técnico UUID).
 * Invariante: exactamente 10 dígitos.
 */
public record AccountNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("\\d{10}");

    public AccountNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidAccountNumberException(value);
        }
    }

    public static AccountNumber of(String value) {
        return new AccountNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
