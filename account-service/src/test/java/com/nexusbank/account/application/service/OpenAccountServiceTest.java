package com.nexusbank.account.application.service;

import com.nexusbank.account.domain.model.Account;
import com.nexusbank.account.domain.model.AccountNumber;
import com.nexusbank.account.domain.model.AccountStatus;
import com.nexusbank.account.domain.model.AccountType;
import com.nexusbank.account.domain.model.Money;
import com.nexusbank.account.domain.port.in.OpenAccountUseCase.OpenAccountCommand;
import com.nexusbank.account.domain.port.out.AccountNumberGeneratorPort;
import com.nexusbank.account.domain.port.out.AccountRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAccountServiceTest {

    @Mock
    private AccountRepositoryPort accountRepository;
    @Mock
    private AccountNumberGeneratorPort accountNumberGenerator;

    private OpenAccountService service;

    @BeforeEach
    void setUp() {
        service = new OpenAccountService(accountRepository, accountNumberGenerator);
    }

    @Test
    void abre_la_cuenta_con_el_numero_generado_y_la_guarda() {
        when(accountNumberGenerator.next()).thenReturn(AccountNumber.of("0000000001"));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.open(new OpenAccountCommand("user-1", AccountType.PREMIUM));

        // ArgumentCaptor: "atrapa" lo que se pasó a save() para inspeccionarlo
        ArgumentCaptor<Account> saved = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(saved.capture());

        Account account = saved.getValue();
        assertThat(account.getAccountNumber()).isEqualTo(AccountNumber.of("0000000001"));
        assertThat(account.getUserId()).isEqualTo("user-1");
        assertThat(account.getType()).isEqualTo(AccountType.PREMIUM);
        assertThat(account.getBalance()).isEqualTo(Money.ZERO);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }
}
