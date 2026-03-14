CREATE TABLE transactions (
    id             CHAR(36)                      NOT NULL DEFAULT (UUID()),
    account_id     CHAR(36)                      NOT NULL,
    type           ENUM('DEPOSIT','WITHDRAWAL')  NOT NULL,
    amount         DECIMAL(19,4)                 NOT NULL,
    balance_before DECIMAL(19,4)                 NOT NULL,
    balance_after  DECIMAL(19,4)                 NOT NULL,
    description    VARCHAR(255)                           DEFAULT NULL,
    created_at     DATETIME                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);
CREATE INDEX idx_transactions_account_id ON transactions (account_id);
CREATE INDEX idx_transactions_created_at ON transactions (created_at);
