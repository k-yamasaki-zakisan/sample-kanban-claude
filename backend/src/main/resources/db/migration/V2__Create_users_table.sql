CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster email lookups during authentication
CREATE INDEX idx_users_email ON users(email);

-- Insert a default test user for testing (password: "password123")
-- Password hash generated with BCrypt for "password123"
INSERT INTO users (name, email, password) VALUES 
('テストユーザー', 'test@example.com', '$2a$10$O8gZYWmrLfWpKd6YgVXRAOYMLhE3rOuW8WzLgJhKbwKjk9Vh6Gm4G');