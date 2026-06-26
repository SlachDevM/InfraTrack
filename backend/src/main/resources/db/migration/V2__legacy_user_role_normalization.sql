-- One-time normalization for databases created before V1 role enum values were finalised.
-- Safe on fresh databases: updates zero rows.

UPDATE users SET role = 'ADMINISTRATOR' WHERE role = 'ADMIN';
UPDATE users SET role = 'FIELD_EMPLOYEE' WHERE role = 'EMPLOYEE';

UPDATE users SET enabled = TRUE WHERE enabled IS NULL;
