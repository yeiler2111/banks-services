package com.nexusbank.account.domain.port.out;

import com.nexusbank.account.domain.model.AccountNumber;

/**
 * Puerto de SALIDA: obtiene el siguiente número de cuenta disponible.
 *
 * <p>Garantizar la unicidad es responsabilidad de la implementación
 * (por ejemplo, una secuencia de PostgreSQL), no del caso de uso.
 */
public interface AccountNumberGeneratorPort {

    AccountNumber next();
}
