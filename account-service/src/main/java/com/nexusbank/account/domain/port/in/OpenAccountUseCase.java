package com.nexusbank.account.domain.port.in;

import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.AccountType;

/** Puerto de ENTRADA: abrir una cuenta nueva para un usuario. */
public interface OpenAccountUseCase {

    Account open(OpenAccountCommand command);

    /** Datos de entrada del caso de uso. userId = "sub" del JWT de Keycloak. */
    record OpenAccountCommand(String userId, AccountType type) {
    }
}
