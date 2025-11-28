CREATE TABLE IF NOT EXISTS roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS users
(
    id          SERIAL PRIMARY KEY,
    full_name   VARCHAR(100),
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    verified    BOOLEAN,
    last_active TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    CONSTRAINT unique_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id)
);
