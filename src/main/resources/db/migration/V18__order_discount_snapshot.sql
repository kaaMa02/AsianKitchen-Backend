-- V018__order_discount_snapshot.sql
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS items_subtotal_before_discount NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS discount_percent NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS items_subtotal_after_discount NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS vat_amount NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(10,2);

ALTER TABLE buffet_order
    ADD COLUMN IF NOT EXISTS items_subtotal_before_discount NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS discount_percent NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS items_subtotal_after_discount NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS vat_amount NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(10,2);
