-- Execute only against a dedicated, explicitly selected database.
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(11),
    sex VARCHAR(1),
    id_number VARCHAR(18),
    status TINYINT NOT NULL DEFAULT 1,
    bootstrap_admin BOOLEAN NOT NULL DEFAULT FALSE,
    create_time DATETIME(6) NOT NULL,
    update_time DATETIME(6) NOT NULL,
    create_user BIGINT NOT NULL,
    update_user BIGINT NOT NULL,
    CONSTRAINT uk_employee_username UNIQUE (username),
    CONSTRAINT ck_employee_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type TINYINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME(6) NOT NULL,
    update_time DATETIME(6) NOT NULL,
    create_user BIGINT NOT NULL,
    update_user BIGINT NOT NULL,
    CONSTRAINT uk_category_name UNIQUE (name),
    CONSTRAINT ck_category_type CHECK (type IN (1, 2)),
    CONSTRAINT ck_category_sort CHECK (sort >= 0),
    CONSTRAINT ck_category_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
