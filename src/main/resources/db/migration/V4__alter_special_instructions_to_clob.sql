-- V4__alter_special_instructions_to_clob.sql

-- Turn the TEXT column into a real large‐object CLOB (OID)
-- Note: this will *lose* existing content unless you write a PL/pgSQL function
-- to copy each TEXT value into a large‐object and set the column to that LO‐OID.
-- For simplicity, you could drop and re‐add, if you don't mind losing data:

ALTER TABLE orders
DROP COLUMN special_instructions;

ALTER TABLE orders
    ADD COLUMN special_instructions OID;

ALTER TABLE buffet_order
DROP COLUMN special_instructions;

ALTER TABLE buffet_order
    ADD COLUMN special_instructions OID;

ALTER TABLE reservation
DROP COLUMN special_requests;

ALTER TABLE reservation
    ADD COLUMN special_requests OID;