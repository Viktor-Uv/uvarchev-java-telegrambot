-- schema.sql

CREATE TABLE IF NOT EXISTS users
(
    id             BIGINT     NOT NULL,
    user_is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS subscriptions
(
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    subs_service VARCHAR(255) DEFAULT NULL,
    sub_is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS parameters
(
    id              BIGINT NOT NULL AUTO_INCREMENT,
    subscription_id BIGINT NOT NULL,
    param_name      VARCHAR(255) DEFAULT NULL,
    param_value     VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (subscription_id) REFERENCES subscriptions (id)
);
