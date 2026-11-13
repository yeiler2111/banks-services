package com.nexusbank.account.infrastructure.adapter.in.web.dto;

import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.AccountStatus;
import com.nexusbank.account.domain.model.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lo que la API devuelve de una cuenta. Contrato público: no expone detalles internos
 * (como version) y no cambia si el dominio se reorganiza por dentro.
 */
public record AccountResponse(
        UUID id,
        String accountNumber,
        String userId,
        AccountType type,
        BigDecimal balance,
        AccountStatus status) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber().value(),
                account.getUserId(),
                account.getType(),
                account.getBalance().amount(),
                account.getStatus());
    }
}
