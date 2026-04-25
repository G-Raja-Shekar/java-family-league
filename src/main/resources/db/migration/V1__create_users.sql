CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    avatar_url  VARCHAR(1024),
    role        VARCHAR(50)  NOT NULL DEFAULT 'ROLE_USER',
    created_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_users_email ON users (email) WHERE deleted_at IS NULL;
