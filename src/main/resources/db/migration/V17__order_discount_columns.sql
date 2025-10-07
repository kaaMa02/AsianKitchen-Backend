ALTER TABLE customer_order
    ADD COLUMN discount_percent_applied NUMERIC(5,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN discount_amount          NUMERIC(10,2) NOT NULL DEFAULT 0.00;

ALTER TABLE buffet_order
    ADD COLUMN discount_percent_applied NUMERIC(5,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN discount_amount          NUMERIC(10,2) NOT NULL DEFAULT 0.00;
