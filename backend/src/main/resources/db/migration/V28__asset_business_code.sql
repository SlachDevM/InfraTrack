-- V2.4.0 Sprint M4-BE1: stable business code for assets (mobile QR/barcode lookup)

ALTER TABLE assets
    ADD COLUMN asset_code VARCHAR(50);

UPDATE assets
SET asset_code = 'AST-' || upper(substr(md5(random()::text || clock_timestamp()::text || id::text), 1, 8))
WHERE asset_code IS NULL;

ALTER TABLE assets
    ALTER COLUMN asset_code SET NOT NULL;

CREATE UNIQUE INDEX idx_assets_asset_code ON assets (asset_code);
