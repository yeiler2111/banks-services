package com.nexusbank.account.domain.port.in;

import com.nexusbank.account.domain.model.Account;

import java.util.List;
import java.util.UUID;

/** Puerto de ENTRADA de solo lectura (consultas). Separarlo de los comandos facilita aplicar CQRS después. */
public interface GetAccountQuery {

    /** Devuelve la cuenta solo si pertenece a requesterId; si no, AccountNotFoundException. */
    Account getById(UUID accountId, String requesterId);

    List<Account> getByUserId(String userId);
}
