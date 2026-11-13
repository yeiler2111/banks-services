package com.nexusbank.account.domain.exception;

/** Se lanza cuando se intenta operar sobre una cuenta bloqueada. */
public class AccountLockedException extends DomainException {

    public AccountLockedException(String accountNumber) {
        super("La cuenta " + accountNumber + " está bloqueada y no admite operaciones");
    }
}
