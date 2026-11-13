package com.nexusbank.account.application.service;

import com.nexusbank.account.domain.exception.AccountNotFoundException;
import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.Money;
import com.nexusbank.account.domain.port.in.UpdateBalanceUseCase;
import com.nexusbank.account.domain.port.out.AccountRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Casos de uso: depositar y retirar.
 *
 * <p>Receta: cargar (solo si es del solicitante) -> delegar en el dominio -> guardar.
 * Si el dominio lanza una excepción (saldo insuficiente, cuenta bloqueada),
 * NO se llega a guardar y @Transactional hace rollback.
 */
@Service
public class UpdateBalanceService implements UpdateBalanceUseCase {

    private final AccountRepositoryPort accountRepository;

    public UpdateBalanceService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Account deposit(UUID accountId, String requesterId, Money amount) {
        Account account = loadOwnedBy(accountId, requesterId);
        account.credit(amount);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account withdraw(UUID accountId, String requesterId, Money amount) {
        Account account = loadOwnedBy(accountId, requesterId);
        account.debit(amount);
        return accountRepository.save(account);
    }

    /** Ajena o inexistente -> mismo 404: no revelamos que la cuenta existe. */
    private Account loadOwnedBy(UUID accountId, String requesterId) {
        return accountRepository.findById(accountId)
                .filter(account -> account.isOwnedBy(requesterId))
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
