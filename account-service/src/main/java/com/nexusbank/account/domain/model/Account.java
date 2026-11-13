package com.nexusbank.account.domain.model;

import com.nexusbank.account.domain.exception.AccountLockedException;
import com.nexusbank.account.domain.exception.InsufficientFundsException;
import com.nexusbank.account.domain.exception.InvalidMoneyException;

import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raíz: la cuenta bancaria.
 *
 * <p>Es la ÚNICA puerta para modificar el saldo. No hay setters: el estado solo cambia
 * a través de métodos con significado de negocio ({@link #credit}, {@link #debit},
 * {@link #lock}, {@link #unlock}), que validan las reglas antes de tocar nada.
 *
 * <p>Identidad: dos cuentas son la misma si tienen el mismo {@code id} (no por su saldo).
 */
public class Account {

    private final UUID id;
    private final AccountNumber accountNumber;
    private final String userId;          // "sub" del JWT emitido por Keycloak
    private final AccountType type;
    private Money balance;
    private AccountStatus status;
    private final Long version;           // Concurrencia optimista: el dominio solo la transporta

    private Account(UUID id, AccountNumber accountNumber, String userId, AccountType type,
                    Money balance, AccountStatus status, Long version) {
        this.id = Objects.requireNonNull(id, "id");
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
        this.userId = requireText(userId);
        this.type = Objects.requireNonNull(type, "type");
        this.balance = Objects.requireNonNull(balance, "balance");
        this.status = Objects.requireNonNull(status, "status");
        this.version = version; // null = aún no persistida
    }

    // ------------------------------------------------------------------ Fábricas

    /** Abre una cuenta NUEVA: aplica las reglas de apertura (saldo 0, ACTIVE). */
    public static Account open(AccountNumber accountNumber, String userId, AccountType type) {
        return new Account(UUID.randomUUID(), accountNumber, userId, type,
                Money.ZERO, AccountStatus.ACTIVE, null);
    }

    /** Reconstruye una cuenta EXISTENTE (p. ej. leída de la BD). No aplica reglas de apertura. */
    public static Account restore(UUID id, AccountNumber accountNumber, String userId, AccountType type,
                                  Money balance, AccountStatus status, Long version) {
        return new Account(id, accountNumber, userId, type, balance, status, version);
    }

    // ------------------------------------------------------- Comportamiento de negocio

    /** Ingresa dinero en la cuenta. */
    public void credit(Money amount) {
        ensureActive();
        ensurePositive(amount);
        this.balance = this.balance.add(amount);
    }

    /** Retira dinero de la cuenta. Nunca deja el saldo en negativo. */
    public void debit(Money amount) {
        ensureActive();
        ensurePositive(amount);
        if (!balance.isGreaterThanOrEqual(amount)) {
            throw new InsufficientFundsException(accountNumber.value(), balance.toString(), amount.toString());
        }
        this.balance = this.balance.subtract(amount);
    }

    public void lock() {
        this.status = AccountStatus.LOCKED;
    }

    public void unlock() {
        this.status = AccountStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    /** ¿Esta cuenta pertenece al usuario indicado? (userId = "sub" del JWT) */
    public boolean isOwnedBy(String userId) {
        return this.userId.equals(userId);
    }

    // ------------------------------------------------------------ Reglas privadas

    private void ensureActive() {
        if (!isActive()) {
            throw new AccountLockedException(accountNumber.value());
        }
    }

    private static void ensurePositive(Money amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.isZero()) {
            throw new InvalidMoneyException("El monto de la operación debe ser mayor que cero");
        }
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("userId es obligatorio");
        }
        return value;
    }

    // ------------------------------------------------------------ Getters (sin setters)

    public UUID getId() { return id; }
    public AccountNumber getAccountNumber() { return accountNumber; }
    public String getUserId() { return userId; }
    public AccountType getType() { return type; }
    public Money getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Long getVersion() { return version; }

    // ---------------------------------------------------- Identidad de entidad (por id)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
