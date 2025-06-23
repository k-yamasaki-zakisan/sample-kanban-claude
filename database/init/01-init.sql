-- カンバンアプリケーション用のデータベース初期化スクリプト

-- データベースとユーザーは既にdocker-composeで作成されているため、
-- ここではテーブル作成とサンプルデータのみを準備

-- Spring Boot JPA が自動でテーブルを作成するため、
-- 手動でのテーブル作成は不要
-- このファイルはサンプルデータ投入用として使用

-- サンプルタスクデータ
INSERT INTO tasks (title, description, status, created_at, updated_at) VALUES
('プロジェクト計画書作成', 'Q1のプロジェクト計画書を作成する', 'TODO', NOW(), NOW()),
('データベース設計', 'カンバンアプリケーション用のデータベース設計を行う', 'IN_PROGRESS', NOW(), NOW()),
('フロントエンド実装', 'React.jsでカンバンボードのUIを実装する', 'IN_PROGRESS', NOW(), NOW()),
('API仕様書作成', 'REST APIの仕様書をSwaggerで作成する', 'TODO', NOW(), NOW()),
('ユニットテスト作成', 'バックエンドのユニットテストを作成する', 'TODO', NOW(), NOW()),
('ロゴデザイン', 'アプリケーションのロゴデザインを作成する', 'DONE', NOW(), NOW()),
('データベース構築', 'PostgreSQLでデータベースを構築する', 'DONE', NOW(), NOW());