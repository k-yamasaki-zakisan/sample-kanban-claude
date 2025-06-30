-- MinIOカラム名をS3カラム名に変更
ALTER TABLE task_images RENAME COLUMN minio_bucket TO s3_bucket;
ALTER TABLE task_images RENAME COLUMN minio_object_key TO s3_object_key;