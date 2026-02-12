-- Schema PostgreSQL pour le projet Todo API

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(64) NOT NULL,
    CONSTRAINT uk_category_label UNIQUE (label)
);

CREATE TABLE IF NOT EXISTS annonce (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(64) NOT NULL,
    description VARCHAR(256) NOT NULL,
    adress VARCHAR(64) NOT NULL,
    mail VARCHAR(64) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    author_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_annonce_author FOREIGN KEY (author_id) REFERENCES app_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_annonce_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE RESTRICT,
    CONSTRAINT ck_annonce_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS idx_annonce_author_id ON annonce (author_id);
CREATE INDEX IF NOT EXISTS idx_annonce_category_id ON annonce (category_id);
CREATE INDEX IF NOT EXISTS idx_annonce_status ON annonce (status);
