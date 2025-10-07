-- Create the site-wide discount configuration table (singleton row).
CREATE TABLE IF NOT EXISTS discount_config (
    id UUID PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    percent_menu NUMERIC(5,2) NOT NULL DEFAULT 0,
    percent_buffet NUMERIC(5,2) NOT NULL DEFAULT 0,
    starts_at TIMESTAMPTZ NULL,
    ends_at   TIMESTAMPTZ NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- Seed a single default row with your hard-coded singleton ID from DiscountService.
INSERT INTO discount_config (id, enabled, percent_menu, percent_buffet, starts_at, ends_at, updated_at)
VALUES ('11111111-2222-3333-4444-555555555555', FALSE, 0, 0, NULL, NULL, NOW())
    ON CONFLICT (id) DO NOTHING;
