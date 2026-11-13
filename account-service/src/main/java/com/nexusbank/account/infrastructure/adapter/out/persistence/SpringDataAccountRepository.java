package com.nexusbank.account.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de Spring Data: Spring genera la implementación en tiempo de ejecución.
 *
 * <p>NO es el puerto del dominio. Es una herramienta interna del adaptador:
 * trabaja con AccountJpaEntity y el núcleo nunca la ve.
 */
interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {

    /** Query derivada del nombre: SELECT ... FROM accounts WHERE user_id = ? */
    List<AccountJpaEntity> findByUserId(String userId);
}
