package com.nexusbank.account.infrastructure.adapter.in.web.dto;

import com.nexusbank.account.domain.model.AccountType;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de POST /api/v1/accounts. Ejemplo: { "type": "STANDARD" } */
public record OpenAccountRequest(
        @NotNull(message = "type es obligatorio (STANDARD o PREMIUM)")
        AccountType type) {
}
