-- =====================================================================
-- V1: tabla de cuentas
-- Flyway ejecuta este script UNA sola vez y lo registra en flyway_schema_history.
-- Nunca se modifica un script ya aplicado: los cambios van en V2, V3...
-- =====================================================================

CREATE TABLE accounts (
    id             UUID          PRIMARY KEY,                  -- id técnico (lo genera el dominio)
    account_number VARCHAR(10)   NOT NULL UNIQUE,              -- número visible para el cliente
    user_id        VARCHAR(64)   NOT NULL,                     -- "sub" del JWT de Keycloak
    type           VARCHAR(20)   NOT NULL,
    balance        NUMERIC(19,2) NOT NULL,                     -- NUNCA float/double para dinero
    status         VARCHAR(20)   NOT NULL,
    version        BIGINT        NOT NULL,                     -- concurrencia optimista (@Version)

    -- Última línea de defensa: aunque un bug saltara el dominio, la BD rechaza datos inválidos
    CONSTRAINT chk_accounts_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_accounts_type   CHECK (type   IN ('STANDARD', 'PREMIUM')),
    CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE', 'LOCKED'))
);

-- Consulta frecuente: "mis cuentas" (por usuario)
CREATE INDEX idx_accounts_user_id ON accounts (user_id);

-- Fuente de números de cuenta únicos (la usa el adaptador AccountNumberGenerator)
CREATE SEQUENCE account_number_seq START WITH 1 INCREMENT BY 1;
