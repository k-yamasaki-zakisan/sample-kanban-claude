-- Create tasks table with all required statuses including IN_REVIEW
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO tasks (title, description, status, created_at, updated_at) VALUES
('プロジェクト計画書作成', 'Q1のプロジェクト計画書を作成する', 'TODO', NOW(), NOW()),
('データベース設計', 'カンバンアプリケーション用のデータベース設計を行う', 'IN_PROGRESS', NOW(), NOW()),
('フロントエンド実装', 'React.jsでカンバンボードのUIを実装する', 'IN_PROGRESS', NOW(), NOW()),
('API仕様書作成', 'REST APIの仕様書をSwaggerで作成する', 'TODO', NOW(), NOW()),
('ユニットテスト作成', 'バックエンドのユニットテストを作成する', 'TODO', NOW(), NOW()),
('ロゴデザイン', 'アプリケーションのロゴデザインを作成する', 'DONE', NOW(), NOW()),
('データベース構築', 'PostgreSQLでデータベースを構築する', 'DONE', NOW(), NOW());