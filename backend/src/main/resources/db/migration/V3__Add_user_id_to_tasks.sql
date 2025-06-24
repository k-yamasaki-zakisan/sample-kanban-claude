-- Add user_id column to tasks table for user-specific task management
ALTER TABLE tasks ADD COLUMN user_id BIGINT;

-- Add foreign key constraint to users table
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Update existing tasks to belong to the test user (id=1)
-- This assumes the test user exists from V2 migration
UPDATE tasks SET user_id = 1 WHERE user_id IS NULL;

-- Make user_id NOT NULL after setting default values
ALTER TABLE tasks ALTER COLUMN user_id SET NOT NULL;

-- Create index for better query performance on user_id
CREATE INDEX idx_tasks_user_id ON tasks(user_id);

-- Create composite index for user_id and created_at for efficient ordering
CREATE INDEX idx_tasks_user_id_created_at ON tasks(user_id, created_at DESC);