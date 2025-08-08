ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payment_intent_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_status     VARCHAR(30);

ALTER TABLE buffet_order
    ADD COLUMN IF NOT EXISTS payment_intent_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_status     VARCHAR(30);