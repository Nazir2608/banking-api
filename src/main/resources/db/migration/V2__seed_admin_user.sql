INSERT INTO users (id, first_name, last_name, email, password, phone, role, is_active)
VALUES (UUID(), 'Super', 'Admin', 'admin@springbank.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewFX.HRCbm7IJnKy',
        '+10000000000', 'ADMIN', 1);
