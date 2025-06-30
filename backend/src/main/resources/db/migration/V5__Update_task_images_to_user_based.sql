-- task_imagesテーブルをユーザーベースに変更

-- 既存のtask_imagesテーブルのデータを削除（古いデータのため）
DELETE FROM task_images;

-- 外部キー制約を削除
ALTER TABLE task_images 
DROP CONSTRAINT IF EXISTS fk_task_images_task_id;

-- task_idカラムを削除
ALTER TABLE task_images 
DROP COLUMN IF EXISTS task_id;

-- user_idカラムを追加（NOT NULL制約は後で追加）
ALTER TABLE task_images 
ADD COLUMN user_id BIGINT;

-- 一時的な画像（タスクに未紐づけ）を識別するためのフラグを追加
ALTER TABLE task_images 
ADD COLUMN is_temporary BOOLEAN DEFAULT TRUE;

-- 画像の使用状況を管理するための追加フィールド
ALTER TABLE task_images 
ADD COLUMN description_content TEXT;

-- user_idにNOT NULL制約を追加
ALTER TABLE task_images 
ALTER COLUMN user_id SET NOT NULL;

-- 外部キー制約を追加
ALTER TABLE task_images 
ADD CONSTRAINT fk_task_images_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- インデックスを追加
CREATE INDEX idx_task_images_user_id ON task_images(user_id);
CREATE INDEX idx_task_images_temporary ON task_images(is_temporary);
CREATE INDEX idx_task_images_user_temporary ON task_images(user_id, is_temporary);