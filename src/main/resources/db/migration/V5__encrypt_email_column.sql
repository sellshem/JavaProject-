-- Increase full_name column size to accommodate encrypted base64 data
-- The maximum length of a name after AES/GCM encryption is roughly 1.5x original plus IV (12 bytes)
-- For a fullName max 255 chars, we need at least 512 varchar.
ALTER TABLE users ALTER COLUMN full_name TYPE VARCHAR(512);

-- Note: email remains plaintext for queryability. No changes to email column.
