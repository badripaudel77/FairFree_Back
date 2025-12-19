-- Insert users on app startup
-- User, password: password123
INSERT INTO users (full_name, email, password, verified, last_active)
VALUES ('Normal User', 'user@gmail.com',
        '$2a$10$OCQIe4RgYAL4/qchrAutwudNw5gTXXrnk0dOtNazIzmB2hukBv4gy', true, NOW());

-- Admin
INSERT INTO users (full_name, email, password, verified, last_active)
VALUES ('Admin User', 'admin@gmail.com', '$2a$10$OCQIe4RgYAL4/qchrAutwudNw5gTXXrnk0dOtNazIzmB2hukBv4gy',
        true, NOW());