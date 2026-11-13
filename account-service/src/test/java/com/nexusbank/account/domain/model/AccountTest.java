package com.nexusbank.account.domain.model;

import com.nexusbank.account.domain.exception.AccountLockedException;
import com.nexusbank.account.domain.exception.InsufficientFundsException;
import com.nexusbank.account.domain.exception.InvalidAccountNumberException;
import com.nexusbank.account.domain.exception.InvalidMoneyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests unitarios puros del agregado Account: sin Spring, sin base de datos. */
class AccountTest {

    private static final AccountNumber NUMBER = AccountNumber.of("1234567890");
    private static final String USER_ID = "ff64aaa2-ed5c-464a-9fdc-89ccce9cbedd";

    private Account accountWithBalance(String balance) {
        Account account = Account.open(NUMBER, USER_ID, AccountType.STANDARD);
        account.credit(Money.of(balance));
        return account;
    }

    @Nested
    @DisplayName("Apertura")
    class Opening {

        @Test
        void una_cuenta_nueva_empieza_activa_con_saldo_cero_y_sin_version() {
            Account account = Account.open(NUMBER, USER_ID, AccountType.PREMIUM);

            assertThat(account.getId()).isNotNull();
            assertThat(account.getBalance()).isEqualTo(Money.ZERO);
            assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(account.getType()).isEqualTo(AccountType.PREMIUM);
            assertThat(account.getVersion()).isNull();
        }

        @Test
        void exige_userId() {
            assertThatThrownBy(() -> Account.open(NUMBER, " ", AccountType.STANDARD))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Crédito")
    class Credit {

        @Test
        void suma_al_saldo() {
            Account account = accountWithBalance("100.00");
            account.credit(Money.of("50.25"));
            assertThat(account.getBalance()).isEqualTo(Money.of("150.25"));
        }

        @Test
        void rechaza_monto_cero() {
            Account account = accountWithBalance("100");
            assertThatThrownBy(() -> account.credit(Money.ZERO))
                    .isInstanceOf(InvalidMoneyException.class);
        }
    }

    @Nested
    @DisplayName("Débito")
    class Debit {

        @Test
        void resta_del_saldo() {
            Account account = accountWithBalance("100.00");
            account.debit(Money.of("30.00"));
            assertThat(account.getBalance()).isEqualTo(Money.of("70.00"));
        }

        @Test
        void permite_dejar_el_saldo_exactamente_en_cero() {
            Account account = accountWithBalance("100.00");
            account.debit(Money.of("100.00"));
            assertThat(account.getBalance().isZero()).isTrue();
        }

        @Test
        void sin_saldo_suficiente_lanza_excepcion_y_NO_modifica_el_saldo() {
            Account account = accountWithBalance("100.00");

            assertThatThrownBy(() -> account.debit(Money.of("100.01")))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("1234567890");

            assertThat(account.getBalance()).isEqualTo(Money.of("100.00"));
        }

        @Test
        void rechaza_monto_cero() {
            Account account = accountWithBalance("100");
            assertThatThrownBy(() -> account.debit(Money.ZERO))
                    .isInstanceOf(InvalidMoneyException.class);
        }
    }

    @Nested
    @DisplayName("Bloqueo")
    class Locking {

        @Test
        void una_cuenta_bloqueada_no_admite_debitos_ni_creditos() {
            Account account = accountWithBalance("100");
            account.lock();

            assertThatThrownBy(() -> account.debit(Money.of("10")))
                    .isInstanceOf(AccountLockedException.class);
            assertThatThrownBy(() -> account.credit(Money.of("10")))
                    .isInstanceOf(AccountLockedException.class);
            assertThat(account.getBalance()).isEqualTo(Money.of("100"));
        }

        @Test
        void al_desbloquear_vuelve_a_operar() {
            Account account = accountWithBalance("100");
            account.lock();
            account.unlock();
            account.debit(Money.of("10"));
            assertThat(account.getBalance()).isEqualTo(Money.of("90"));
        }
    }

    @Nested
    @DisplayName("Identidad y número de cuenta")
    class Identity {

        @Test
        void dos_cuentas_son_iguales_si_tienen_el_mismo_id_aunque_cambie_el_saldo() {
            UUID id = UUID.randomUUID();
            Account a = Account.restore(id, NUMBER, USER_ID, AccountType.STANDARD,
                    Money.of("10"), AccountStatus.ACTIVE, 0L);
            Account b = Account.restore(id, NUMBER, USER_ID, AccountType.STANDARD,
                    Money.of("999"), AccountStatus.LOCKED, 3L);

            assertThat(a).isEqualTo(b);
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "12345678901", "12345abcde", ""})
        void rechaza_numeros_de_cuenta_mal_formados(String value) {
            assertThatThrownBy(() -> AccountNumber.of(value))
                    .isInstanceOf(InvalidAccountNumberException.class);
        }
    }
}
