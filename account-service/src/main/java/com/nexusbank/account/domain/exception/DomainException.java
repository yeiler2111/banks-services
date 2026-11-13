package com.nexusbank.account.domain.exception;

/**
 * Raíz de todas las violaciones de reglas de negocio del dominio de cuentas.
 *
 * <p>Es unchecked (RuntimeException) para no "contaminar" las firmas de los métodos
 * del dominio con throws. La capa web la traducirá a una respuesta HTTP 422.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
