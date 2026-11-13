package com.nexusbank.account.domain.exception;

/** Se lanza cuando un débito dejaría el saldo de la cuenta en negativo. */
public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException(String accountNumber, String balance, String requested) {
        super("Saldo insuficiente en la cuenta " + accountNumber
                + ": disponible " + balance + ", solicitado " + requested);
    }
}
