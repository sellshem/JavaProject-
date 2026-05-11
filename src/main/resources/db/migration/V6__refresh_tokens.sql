-- Refresh tokens table for JWT refresh token rotation
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for fast lookup by token
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Index for cleanup expired tokens
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- Index for quick user token deletion
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
