package com.nexusbank.account.domain.model;

import com.nexusbank.account.domain.exception.InvalidMoneyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests unitarios puros: sin Spring, sin base de datos. */
class MoneyTest {

    @Nested
    @DisplayName("Creación")
    class Creation {

        @Test
        void normaliza_a_dos_decimales() {
            assertThat(Money.of("100").amount()).isEqualByComparingTo("100.00");
            assertThat(Money.of("100").amount().scale()).isEqualTo(2);
        }

        @Test
        void usa_redondeo_bancario_half_even() {
            // HALF_EVEN redondea al vecino PAR cuando está justo a la mitad
            assertThat(Money.of("10.125")).isEqualTo(Money.of("10.12"));
            assertThat(Money.of("10.135")).isEqualTo(Money.of("10.14"));
        }

        @Test
        void permite_cero() {
            assertThat(Money.of("0").isZero()).isTrue();
        }

        @Test
        void rechaza_negativos() {
            assertThatThrownBy(() -> Money.of("-0.01"))
                    .isInstanceOf(InvalidMoneyException.class);
        }

        @Test
        void rechaza_nulo() {
            assertThatThrownBy(() -> Money.of((BigDecimal) null))
                    .isInstanceOf(InvalidMoneyException.class);
        }

        @Test
        void rechaza_texto_no_numerico() {
            assertThatThrownBy(() -> Money.of("abc"))
                    .isInstanceOf(InvalidMoneyException.class);
        }
    }

    @Nested
    @DisplayName("Operaciones")
    class Operations {

        @Test
        void suma_sin_errores_de_punto_flotante() {
            // Con double: 0.1 + 0.2 = 0.30000000000000004
            assertThat(Money.of("0.10").add(Money.of("0.20"))).isEqualTo(Money.of("0.30"));
        }

        @Test
        void resta() {
            assertThat(Money.of("100.00").subtract(Money.of("30.50"))).isEqualTo(Money.of("69.50"));
        }

        @Test
        void resta_que_quedaria_negativa_lanza_excepcion() {
            assertThatThrownBy(() -> Money.of("10").subtract(Money.of("10.01")))
                    .isInstanceOf(InvalidMoneyException.class);
        }

        @Test
        void es_inmutable() {
            Money original = Money.of("50");
            original.add(Money.of("25"));
            assertThat(original).isEqualTo(Money.of("50"));
        }

        @Test
        void compara_montos() {
            assertThat(Money.of("10").isGreaterThanOrEqual(Money.of("10.00"))).isTrue();
            assertThat(Money.of("9.99").isGreaterThanOrEqual(Money.of("10"))).isFalse();
        }
    }
}
