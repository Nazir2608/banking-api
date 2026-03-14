CREATE TABLE loans (
    id            CHAR(36)                                                 NOT NULL DEFAULT (UUID()),
    user_id       CHAR(36)                                                 NOT NULL,
    account_id    CHAR(36)                                                 NOT NULL,
    amount        DECIMAL(19,4)                                            NOT NULL,
    interest_rate DECIMAL(5,2)                                             NOT NULL,
    term_months   INT                                                      NOT NULL,
    emi_amount    DECIMAL(19,4)                                            NOT NULL,
    outstanding   DECIMAL(19,4)                                            NOT NULL,
    total_repaid  DECIMAL(19,4)                                            NOT NULL DEFAULT 0.0000,
    status        ENUM('PENDING','APPROVED','REJECTED','ACTIVE','CLOSED')  NOT NULL DEFAULT 'PENDING',
    purpose       VARCHAR(500)                                                      DEFAULT NULL,
    approved_by   VARCHAR(255)                                                      DEFAULT NULL,
    approved_at   DATETIME                                                          DEFAULT NULL,
    created_at    DATETIME                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_loans_user    FOREIGN KEY (user_id)    REFERENCES users    (id),
    CONSTRAINT fk_loans_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);
CREATE INDEX idx_loans_user_id ON loans (user_id);
CREATE INDEX idx_loans_status  ON loans (status);
