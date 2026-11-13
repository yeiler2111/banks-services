package com.nexusbank.account.domain.exception;

import java.util.UUID;

/** Se lanza cuando la cuenta solicitada no existe. La capa web la traducirá a HTTP 404. */
public class AccountNotFoundException extends DomainException {

    public AccountNotFoundException(UUID accountId) {
        super("No existe la cuenta con id " + accountId);
    }
}
