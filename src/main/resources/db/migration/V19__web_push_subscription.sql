-- V19__web_push_subscriptions.sql
CREATE TABLE IF NOT EXISTS web_push_subscriptions (
                                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint VARCHAR(1000) NOT NULL,
    p256dh VARCHAR(200) NOT NULL,
    auth VARCHAR(200) NOT NULL,
    tag VARCHAR(50),
    user_agent VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_webpush_endpoint UNIQUE (endpoint)
    );
