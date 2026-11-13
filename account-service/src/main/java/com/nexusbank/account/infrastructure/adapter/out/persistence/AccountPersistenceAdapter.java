package com.nexusbank.account.infrastructure.adapter.out.persistence;

import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.port.out.AccountRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ADAPTADOR DE SALIDA: implementa el puerto AccountRepositoryPort usando JPA.
 *
 * <p>Es un traductor: recibe Account (dominio) -> lo convierte a AccountJpaEntity ->
 * lo persiste con Spring Data -> devuelve el resultado convertido otra vez a Account.
 * Spring lo detecta por @Component y lo inyecta en los servicios que piden el puerto.
 */
@Component
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository repository;
    private final AccountPersistenceMapper mapper;

    public AccountPersistenceAdapter(SpringDataAccountRepository repository, AccountPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * saveAndFlush ejecuta el INSERT/UPDATE ya (no al final de la transacción), así:
     * - la cuenta devuelta trae la version actualizada,
     * - un conflicto de concurrencia (OptimisticLockException) aparece aquí y no más tarde.
     */
    @Override
    public Account save(Account account) {
        AccountJpaEntity saved = repository.saveAndFlush(mapper.toEntity(account));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Account> findByUserId(String userId) {
        return repository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
