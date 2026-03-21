CREATE TABLE users
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(90)  NOT NULL,
    username        VARCHAR(60)  NOT NULL,
    email           VARCHAR(120) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER      NOT NULL DEFAULT 0,
    locked_until    TIMESTAMP    NULL,
    last_failed_at  TIMESTAMP    NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    deleted_at      TIMESTAMP    NULL,
    CONSTRAINT users_username_un UNIQUE (username),
    CONSTRAINT users_email_un UNIQUE (email)
);