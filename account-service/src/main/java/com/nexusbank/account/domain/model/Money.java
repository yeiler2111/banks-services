package com.nexusbank.account.domain.model;

import com.nexusbank.account.domain.exception.InvalidMoneyException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object que representa una cantidad de dinero.
 *
 * <p>Invariantes (se cumplen SIEMPRE que exista una instancia):
 * <ul>
 *   <li>nunca es nulo,</li>
 *   <li>nunca es negativo,</li>
 *   <li>tiene exactamente 2 decimales (redondeo bancario HALF_EVEN).</li>
 * </ul>
 *
 * <p>Es inmutable: las operaciones devuelven un Money nuevo.
 * Al ser un {@code record}, dos Money con el mismo monto son iguales (igualdad por valor).
 */
public record Money(BigDecimal amount) {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    /** Constructor compacto: valida y normaliza antes de asignar el campo. */
    public Money {
        if (amount == null) {
            throw new InvalidMoneyException("El monto no puede ser nulo");
        }
        amount = amount.setScale(SCALE, ROUNDING);
        if (amount.signum() < 0) {
            throw new InvalidMoneyException("El monto no puede ser negativo: " + amount);
        }
    }

    /** Fábrica desde texto ("1500.50"). Se evita double para no arrastrar errores de punto flotante. */
    public static Money of(String amount) {
        try {
            return new Money(new BigDecimal(amount));
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidMoneyException("Monto con formato inválido: " + amount);
        }
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /** Lanza InvalidMoneyException si el resultado fuese negativo (lo impide el constructor). */
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
