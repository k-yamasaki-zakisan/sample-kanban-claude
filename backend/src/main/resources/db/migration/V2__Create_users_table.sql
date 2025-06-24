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

-- Insert a default admin user for testing (password: "admin123")
-- Password hash generated with BCrypt for "admin123"
INSERT INTO users (name, email, password) VALUES 
('Admin User', 'admin@example.com', '$2a$10$rZ9p1xRd9EeA4mZ.bvdgFuGJ9qY2EjQ6X3gN7H5kL8mP4sT6vU8wW');