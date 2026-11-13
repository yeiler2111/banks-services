package com.nexusbank.account.application.service;

import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.AccountNumber;
import com.nexusbank.account.domain.port.in.OpenAccountUseCase;
import com.nexusbank.account.domain.port.out.AccountNumberGeneratorPort;
import com.nexusbank.account.domain.port.out.AccountRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: abrir una cuenta. Orquesta; las reglas de apertura viven en Account.open(). */
@Service
public class OpenAccountService implements OpenAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountNumberGeneratorPort accountNumberGenerator;

    public OpenAccountService(AccountRepositoryPort accountRepository,
                              AccountNumberGeneratorPort accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    @Override
    @Transactional
    public Account open(OpenAccountCommand command) {
        AccountNumber number = accountNumberGenerator.next();
        Account account = Account.open(number, command.userId(), command.type());
        return accountRepository.save(account);
    }
}
