package com.nexusbank.account.application.service;

import com.nexusbank.account.domain.exception.AccountLockedException;
import com.nexusbank.account.domain.exception.AccountNotFoundException;
import com.nexusbank.account.domain.exception.InsufficientFundsException;
import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.AccountNumber;
import com.nexusbank.account.domain.model.AccountStatus;
import com.nexusbank.account.domain.model.AccountType;
import com.nexusbank.account.domain.model.Money;
import com.nexusbank.account.domain.port.out.AccountRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Test unitario del caso de uso: el repositorio es un mock (sin BD, sin Spring). */
@ExtendWith(MockitoExtension.class)
class UpdateBalanceServiceTest {

    @Mock
    private AccountRepositoryPort accountRepository;

    private UpdateBalanceService service;

    private static final String OWNER = "user-1";
    private static final String STRANGER = "user-2";

    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new UpdateBalanceService(accountRepository); // inyección por constructor
    }

    private Account existingAccount(String balance, AccountStatus status) {
        return Account.restore(accountId, AccountNumber.of("1234567890"), OWNER,
                AccountType.STANDARD, Money.of(balance), status, 0L);
    }

    @Test
    void deposit_carga_acredita_y_guarda() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount("100", AccountStatus.ACTIVE)));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = service.deposit(accountId, OWNER, Money.of("25.50"));

        assertThat(result.getBalance()).isEqualTo(Money.of("125.50"));
        verify(accountRepository).save(result);
    }

    @Test
    void withdraw_carga_debita_y_guarda() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount("100", AccountStatus.ACTIVE)));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = service.withdraw(accountId, OWNER, Money.of("40"));

        assertThat(result.getBalance()).isEqualTo(Money.of("60"));
    }

    @Test
    void withdraw_sin_saldo_propaga_la_excepcion_del_dominio_y_NO_guarda() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount("10", AccountStatus.ACTIVE)));

        assertThatThrownBy(() -> service.withdraw(accountId, OWNER, Money.of("10.01")))
                .isInstanceOf(InsufficientFundsException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_en_cuenta_bloqueada_NO_guarda() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount("10", AccountStatus.LOCKED)));

        assertThatThrownBy(() -> service.deposit(accountId, OWNER, Money.of("5")))
                .isInstanceOf(AccountLockedException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void cuenta_ajena_se_trata_como_inexistente_y_NO_guarda() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount("1000", AccountStatus.ACTIVE)));

        assertThatThrownBy(() -> service.withdraw(accountId, STRANGER, Money.of("500")))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void cuenta_inexistente_lanza_AccountNotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deposit(accountId, OWNER, Money.of("5")))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(accountId.toString());

        verify(accountRepository, never()).save(any());
    }
}
