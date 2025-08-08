CREATE TABLE IF NOT EXISTS contact_messages (
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    message    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
    );
