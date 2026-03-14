CREATE TABLE users (
    id          CHAR(36)                  NOT NULL DEFAULT (UUID()),
    first_name  VARCHAR(100)              NOT NULL,
    last_name   VARCHAR(100)              NOT NULL,
    email       VARCHAR(255)              NOT NULL,
    password    VARCHAR(255)              NOT NULL,
    phone       VARCHAR(20)                        DEFAULT NULL,
    role        ENUM('CUSTOMER','ADMIN')  NOT NULL DEFAULT 'CUSTOMER',
    is_active   TINYINT(1)                NOT NULL DEFAULT 1,
    created_at  DATETIME                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME                  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone)
);
CREATE INDEX idx_users_role   ON users (role);
CREATE INDEX idx_users_active ON users (is_active);
