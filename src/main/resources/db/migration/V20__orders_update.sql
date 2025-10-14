-- Backfill existing rows first
UPDATE orders SET
                  items_subtotal_before_discount = COALESCE(items_subtotal_before_discount, 0),
                  discount_percent               = COALESCE(discount_percent, 0),
                  discount_amount                = COALESCE(discount_amount, 0),
                  items_subtotal_after_discount  = COALESCE(items_subtotal_after_discount, total_price),
                  vat_amount                     = COALESCE(vat_amount, 0),
                  delivery_fee                   = COALESCE(delivery_fee, 0);

UPDATE buffet_order SET
                        items_subtotal_before_discount = COALESCE(items_subtotal_before_discount, 0),
                        discount_percent               = COALESCE(discount_percent, 0),
                        discount_amount                = COALESCE(discount_amount, 0),
                        items_subtotal_after_discount  = COALESCE(items_subtotal_after_discount, total_price),
                        vat_amount                     = COALESCE(vat_amount, 0),
                        delivery_fee                   = COALESCE(delivery_fee, 0);

-- Then enforce NOT NULL (optional)
ALTER TABLE orders
    ALTER COLUMN items_subtotal_before_discount SET NOT NULL,
ALTER COLUMN discount_percent               SET NOT NULL,
  ALTER COLUMN discount_amount                SET NOT NULL,
  ALTER COLUMN items_subtotal_after_discount  SET NOT NULL,
  ALTER COLUMN vat_amount                     SET NOT NULL,
  ALTER COLUMN delivery_fee                   SET NOT NULL;

ALTER TABLE buffet_order
    ALTER COLUMN items_subtotal_before_discount SET NOT NULL,
ALTER COLUMN discount_percent               SET NOT NULL,
  ALTER COLUMN discount_amount                SET NOT NULL,
  ALTER COLUMN items_subtotal_after_discount  SET NOT NULL,
  ALTER COLUMN vat_amount                     SET NOT NULL,
  ALTER COLUMN delivery_fee                   SET NOT NULL;
