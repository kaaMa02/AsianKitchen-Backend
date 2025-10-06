-- add payment_method to orders
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(30);

-- add payment_method to buffet_order
ALTER TABLE buffet_order
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(30);

-- optional: sensible backfill for historical rows
-- Assume Stripe card orders have a payment_intent_id
UPDATE orders
SET payment_method = 'CARD'
WHERE payment_method IS NULL
  AND payment_intent_id IS NOT NULL;

UPDATE buffet_order
SET payment_method = 'CARD'
WHERE payment_method IS NULL
  AND payment_intent_id IS NOT NULL;
