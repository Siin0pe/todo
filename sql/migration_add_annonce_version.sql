-- Ajoute la colonne de version optimistic locking si absente.
ALTER TABLE annonce
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
