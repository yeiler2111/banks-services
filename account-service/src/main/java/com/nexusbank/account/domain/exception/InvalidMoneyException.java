package com.nexusbank.account.domain.exception;

/** Se lanza cuando se intenta crear o calcular un monto de dinero inválido. */
public class InvalidMoneyException extends DomainException {

    public InvalidMoneyException(String message) {
        super(message);
    }
}
