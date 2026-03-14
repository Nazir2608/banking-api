CREATE TABLE accounts (
    id             CHAR(36)                           NOT NULL DEFAULT (UUID()),
    account_number VARCHAR(20)                        NOT NULL,
    account_type   ENUM('SAVINGS','CURRENT')          NOT NULL DEFAULT 'SAVINGS',
    status         ENUM('ACTIVE','FROZEN','CLOSED')   NOT NULL DEFAULT 'ACTIVE',
    balance        DECIMAL(19,4)                      NOT NULL DEFAULT 0.0000,
    user_id        CHAR(36)                           NOT NULL,
    created_at     DATETIME                           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_accounts_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_user   FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_accounts_status  ON accounts (status);
