package com.nexusbank.account.application.service;

import com.nexusbank.account.domain.exception.AccountNotFoundException;
import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.port.in.GetAccountQuery;
import com.nexusbank.account.domain.port.out.AccountRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Consultas de cuentas. readOnly = true permite a Hibernate/BD optimizar las lecturas. */
@Service
@Transactional(readOnly = true)
public class GetAccountService implements GetAccountQuery {

    private final AccountRepositoryPort accountRepository;

    public GetAccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account getById(UUID accountId, String requesterId) {
        return accountRepository.findById(accountId)
                .filter(account -> account.isOwnedBy(requesterId))   // ajena -> 404, igual que inexistente
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    public List<Account> getByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }
}
