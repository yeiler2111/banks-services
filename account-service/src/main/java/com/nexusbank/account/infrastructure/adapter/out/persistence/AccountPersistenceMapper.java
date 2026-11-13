package com.nexusbank.account.infrastructure.adapter.out.persistence;

import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.AccountNumber;
import com.nexusbank.account.domain.model.Money;
import org.springframework.stereotype.Component;

/** Traduce entre el modelo de dominio (Account) y el modelo de persistencia (AccountJpaEntity). */
@Component
public class AccountPersistenceMapper {

    public AccountJpaEntity toEntity(Account account) {
        return new AccountJpaEntity(
                account.getId(),
                account.getAccountNumber().value(),
                account.getUserId(),
                account.getType(),
                account.getBalance().amount(),
                account.getStatus(),
                account.getVersion());
    }

    /** Usa restore(): la cuenta YA existe, no se aplican reglas de apertura. */
    public Account toDomain(AccountJpaEntity entity) {
        return Account.restore(
                entity.getId(),
                AccountNumber.of(entity.getAccountNumber()),
                entity.getUserId(),
                entity.getType(),
                Money.of(entity.getBalance()),
                entity.getStatus(),
                entity.getVersion());
    }
}
