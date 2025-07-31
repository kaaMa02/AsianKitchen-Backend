-- V3__alter_decimal_columns.sql
-- change every DOUBLE / FLOAT column to DECIMAL(10,2) so it matches your BigDecimal fields

ALTER TABLE menu_item
ALTER COLUMN price TYPE numeric(10,2)
    USING price::numeric(10,2);

ALTER TABLE buffet_item
ALTER COLUMN price TYPE numeric(10,2)
    USING price::numeric(10,2);

ALTER TABLE orders
ALTER COLUMN total_price TYPE numeric(10,2)
    USING total_price::numeric(10,2);

ALTER TABLE buffet_order
ALTER COLUMN total_price TYPE numeric(10,2)
    USING total_price::numeric(10,2);
