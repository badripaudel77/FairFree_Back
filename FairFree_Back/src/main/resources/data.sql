-- Insert roles on startup
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;;

-- Insert users on app startup
-- User, password: password123
INSERT INTO users (full_name, email, password, verified, last_active)
VALUES ('Normal User', 'user@gmail.com', '$2a$10$OCQIe4RgYAL4/qchrAutwudNw5gTXXrnk0dOtNazIzmB2hukBv4gy', true, NOW())
ON CONFLICT (email) DO NOTHING;

-- Admin
INSERT INTO users (full_name, email, password, verified, last_active)
VALUES ('Admin User', 'admin@gmail.com', '$2a$10$OCQIe4RgYAL4/qchrAutwudNw5gTXXrnk0dOtNazIzmB2hukBv4gy', true, NOW())
ON CONFLICT (email) DO NOTHING;

-- Link Username and Roles

-- Get user IDs (assuming IDs are 1 and 2)
-- Get role IDs (assuming ROLE_USER = 1, ROLE_ADMIN = 2)
-- Normal user -> ROLE_USER
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1) ON CONFLICT (user_id, role_id) DO NOTHING;;

-- Admin user -> ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2) ON CONFLICT (user_id, role_id) DO NOTHING;

-- INSERT INTO categories(id, name) VALUES (1, 'CLOTHING') ON CONFLICT (id, name) DO NOTHING;