CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- test user (admin admin)
INSERT INTO users (username, password, role)
VALUES ('admin', '$2a$10$R6u9cS.yY3fZ4U7aFTa9V.lRldz5h7cl3btXcSyWrnIgOhGfiVBDu', 'ADMIN');
