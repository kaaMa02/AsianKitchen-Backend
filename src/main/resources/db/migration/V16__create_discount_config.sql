CREATE TABLE discount_config (
                                 id               UUID PRIMARY KEY,
                                 enabled          BOOLEAN NOT NULL DEFAULT FALSE,
                                 percent_menu     NUMERIC(5,2) NOT NULL DEFAULT 0.00,
                                 percent_buffet   NUMERIC(5,2) NOT NULL DEFAULT 0.00,
                                 starts_at        TIMESTAMPTZ NULL,
                                 ends_at          TIMESTAMPTZ NULL,
                                 updated_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- seed a single row (disabled by default)
INSERT INTO discount_config (id, enabled, percent_menu, percent_buffet)
VALUES ('11111111-2222-3333-4444-555555555555', FALSE, 0.00, 0.00);
