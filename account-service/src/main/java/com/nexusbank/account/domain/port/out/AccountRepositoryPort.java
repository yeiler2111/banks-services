package com.nexusbank.account.domain.port.out;

import com.nexusbank.account.domain.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de SALIDA: lo que la aplicación necesita para persistir cuentas.
 *
 * <p>Habla el idioma del dominio (Account), no el de la BD (entidades JPA, SQL).
 * La infraestructura lo implementará con un adaptador JPA; los tests, con un mock.
 */
public interface AccountRepositoryPort {

    /** Guarda la cuenta (nueva o modificada) y devuelve el estado persistido (p. ej. con versión). */
    Account save(Account account);

    Optional<Account> findById(UUID id);

    List<Account> findByUserId(String userId);
}
