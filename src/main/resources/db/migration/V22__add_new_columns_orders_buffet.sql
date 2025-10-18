-- Customer orders
ALTER TABLE orders
    ADD COLUMN asap BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN requested_at TIMESTAMP NULL,
  ADD COLUMN min_prep_minutes INT NOT NULL DEFAULT 45,
  ADD COLUMN admin_extra_minutes INT NOT NULL DEFAULT 0,
  ADD COLUMN committed_ready_at TIMESTAMP NULL,
  ADD COLUMN auto_cancel_at TIMESTAMP NULL,
  ADD COLUMN seen_at TIMESTAMP NULL,
  ADD COLUMN escalated_at TIMESTAMP NULL;

-- Buffet orders
ALTER TABLE buffet_order
    ADD COLUMN asap BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN requested_at TIMESTAMP NULL,
  ADD COLUMN min_prep_minutes INT NOT NULL DEFAULT 45,
  ADD COLUMN admin_extra_minutes INT NOT NULL DEFAULT 0,
  ADD COLUMN committed_ready_at TIMESTAMP NULL,
  ADD COLUMN auto_cancel_at TIMESTAMP NULL,
  ADD COLUMN seen_at TIMESTAMP NULL,
  ADD COLUMN escalated_at TIMESTAMP NULL;

-- Reservations (no ASAP/scheduling; they already have reservation_date_time)
ALTER TABLE reservation
    ADD COLUMN auto_cancel_at TIMESTAMP NULL,
  ADD COLUMN seen_at TIMESTAMP NULL,
  ADD COLUMN escalated_at TIMESTAMP NULL;

-- Backfill “committed_ready_at” for existing orders (best effort)
UPDATE orders
SET committed_ready_at =
        CASE
            WHEN asap IS TRUE THEN (created_at + (min_prep_minutes || ' minutes')::interval)
            ELSE requested_at
            END
WHERE committed_ready_at IS NULL;

UPDATE buffet_order
SET committed_ready_at =
        CASE
            WHEN asap IS TRUE THEN (created_at + (min_prep_minutes || ' minutes')::interval)
            ELSE requested_at
            END
WHERE committed_ready_at IS NULL;
