-- schema.sql

CREATE TABLE IF NOT EXISTS users
(
    id                BIGINT NOT NULL,
    user_role         VARCHAR(255) DEFAULT 'GUEST',
    articles_received BIGINT       DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS subscriptions
(
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    is_active    TINYINT(1)  NOT NULL,
    last_read_id DATETIME(6) NOT NULL,
    subs_service ENUM (
        'ARSTECHNICA',
        'EUROPEAN_SPACEFLIGHT',
        'NASA',
        'NASASPACEFLIGHT',
        'SPACENEWS',
        'SPACEPOLICYONLINE',
        'SPACE_SCOUT'
        ) DEFAULT NULL,
    user_id      BIGINT      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
