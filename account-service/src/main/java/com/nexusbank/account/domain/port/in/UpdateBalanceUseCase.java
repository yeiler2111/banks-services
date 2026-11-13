package com.nexusbank.account.domain.port.in;

import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.Money;

import java.util.UUID;

/**
 * Puerto de ENTRADA: mover saldo de una cuenta.
 *
 * <p>requesterId = quién hace la petición ("sub" del JWT). Solo el titular puede operar su cuenta;
 * si no lo es, se responde como si la cuenta no existiera (no se revela su existencia).
 */
public interface UpdateBalanceUseCase {

    Account deposit(UUID accountId, String requesterId, Money amount);

    Account withdraw(UUID accountId, String requesterId, Money amount);
}
