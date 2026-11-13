package com.nexusbank.account.domain.exception;

/** Se lanza cuando un número de cuenta no cumple el formato esperado. */
public class InvalidAccountNumberException extends DomainException {

    public InvalidAccountNumberException(String value) {
        super("Número de cuenta inválido: '" + value + "'. Debe tener exactamente 10 dígitos");
    }
}
