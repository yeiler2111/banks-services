package com.nexusbank.account.infrastructure.adapter.out.persistence;

import com.nexusbank.account.domain.model.AccountStatus;
import com.nexusbank.account.domain.model.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Representación de la tabla "accounts" para JPA/Hibernate.
 *
 * <p>Es un detalle de infraestructura: NO contiene reglas de negocio (esas viven en Account).
 * Solo describe cómo se guardan los datos. Se convierte desde/hacia Account con AccountPersistenceMapper.
 */
@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 10)
    private String accountNumber;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)   // guarda "PREMIUM", no el ordinal (0, 1): reordenar el enum no corrompe datos
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    /**
     * Concurrencia optimista. Hibernate genera:
     *   UPDATE accounts SET ..., version = 4 WHERE id = ? AND version = 3
     * Si otra transacción ya cambió la fila (version ya no es 3), actualiza 0 filas
     * y lanza OptimisticLockException en lugar de pisar el saldo.
     */
    @Version
    private Long version;

    /** Constructor vacío: JPA lo exige para instanciar la entidad al leer de la BD. */
    protected AccountJpaEntity() {
    }

    public AccountJpaEntity(UUID id, String accountNumber, String userId, AccountType type,
                            BigDecimal balance, AccountStatus status, Long version) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.type = type;
        this.balance = balance;
        this.status = status;
        this.version = version;
    }

    public UUID getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getUserId() { return userId; }
    public AccountType getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Long getVersion() { return version; }
}
