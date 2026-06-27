-- Normalize existing user emails to canonical lowercase form for case-insensitive identity matching.

UPDATE users
SET email = LOWER(TRIM(email))
WHERE email <> LOWER(TRIM(email));
