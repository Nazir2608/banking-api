CREATE TABLE transfers (
    id               CHAR(36)                                NOT NULL DEFAULT (UUID()),
    reference_number VARCHAR(30)                             NOT NULL,
    from_account_id  CHAR(36)                                NOT NULL,
    to_account_id    CHAR(36)                                NOT NULL,
    amount           DECIMAL(19,4)                           NOT NULL,
    status           ENUM('PENDING','COMPLETED','FAILED')    NOT NULL DEFAULT 'PENDING',
    description      VARCHAR(255)                                     DEFAULT NULL,
    created_at       DATETIME                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_transfers_ref  UNIQUE (reference_number),
    CONSTRAINT fk_transfers_from FOREIGN KEY (from_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transfers_to   FOREIGN KEY (to_account_id)   REFERENCES accounts (id)
);
CREATE INDEX idx_transfers_from    ON transfers (from_account_id);
CREATE INDEX idx_transfers_to      ON transfers (to_account_id);
CREATE INDEX idx_transfers_ref     ON transfers (reference_number);
