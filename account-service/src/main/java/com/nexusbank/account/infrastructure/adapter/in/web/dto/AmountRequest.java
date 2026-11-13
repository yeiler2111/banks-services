package com.nexusbank.account.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Cuerpo de deposit/withdraw. Ejemplo: { "amount": 150.50 }
 *
 * <p>Valida el FORMATO (400 si falla). Las reglas de NEGOCIO (saldo, bloqueo) las aplica el dominio (422).
 */
public record AmountRequest(
        @NotNull(message = "amount es obligatorio")
        @DecimalMin(value = "0.01", message = "amount debe ser mayor que cero")
        @Digits(integer = 17, fraction = 2, message = "amount admite como máximo 2 decimales")
        BigDecimal amount) {
}
