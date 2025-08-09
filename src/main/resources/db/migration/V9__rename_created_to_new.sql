-- V9__rename_created_to_new.sql
UPDATE orders SET status = 'NEW' WHERE status = 'CREATED';