-- Make long URL fields TEXT
ALTER TABLE restaurant_info
ALTER COLUMN google_maps_url TYPE TEXT,
  ALTER COLUMN instagram_url   TYPE TEXT;

-- Optional refinements for other fields
ALTER TABLE restaurant_info
ALTER COLUMN email TYPE VARCHAR(320); -- standard max for emails
