-- V4__Create_task_images_table.sql
CREATE TABLE task_images (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    minio_bucket VARCHAR(100) NOT NULL DEFAULT 'kanban-images',
    minio_object_key VARCHAR(500) NOT NULL,
    upload_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_images_task_id FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Create index for better query performance
CREATE INDEX idx_task_images_task_id ON task_images(task_id);
CREATE INDEX idx_task_images_created_at ON task_images(created_at);