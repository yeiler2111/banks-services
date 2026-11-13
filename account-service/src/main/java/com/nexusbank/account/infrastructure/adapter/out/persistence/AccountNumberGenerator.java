package com.nexusbank.account.infrastructure.adapter.out.persistence;

import com.nexusbank.account.domain.model.AccountNumber;
import com.nexusbank.account.domain.port.out.AccountNumberGeneratorPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * ADAPTADOR DE SALIDA: genera números de cuenta con una secuencia de PostgreSQL.
 *
 * <p>La secuencia es atómica: aunque dos peticiones lleguen a la vez, nunca reciben
 * el mismo valor. Además, la columna account_number es UNIQUE como segunda garantía.
 * Formato: 10 dígitos con ceros a la izquierda (1 -> "0000000001").
 */
@Component
public class AccountNumberGenerator implements AccountNumberGeneratorPort {

    private final JdbcClient jdbcClient;

    public AccountNumberGenerator(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public AccountNumber next() {
        Long value = jdbcClient.sql("SELECT nextval('account_number_seq')")
                .query(Long.class)
                .single();
        return AccountNumber.of(String.format("%010d", value));
    }
}
