# Sample Kanban Board

Kotlin + Spring Boot バックエンドと React.js フロントエンドを使用したカンバンボードアプリケーション

## プロジェクト構成

```
sample-kanban/
├── backend/          # Kotlin + Spring Boot API
│   ├── src/
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── frontend/         # React.js アプリケーション
│   ├── src/
│   ├── public/
│   └── package.json
└── README.md
```

## 機能

- タスクの作成、編集、削除
- タスクのステータス管理（Todo、In Progress、Done）
- カンバンボード形式のUI
- REST API経由でのデータ通信

## 起動方法

### 1. データベース起動（Docker）

```bash
# PostgreSQL、pgAdmin、pgwebを起動
docker-compose up -d

# データベース管理ツール
# - pgAdmin: http://localhost:5050 (admin@example.com / admin)
# - pgweb: http://localhost:8081
```

### 2. バックエンド（Spring Boot）

```bash
cd backend
./gradlew bootRun
```

サーバーは http://localhost:8080 で起動

### 3. フロントエンド（React）

```bash
cd frontend
npm start
```

アプリケーションは http://localhost:3000 で起動

## API エンドポイント

- `GET /api/tasks` - 全タスク取得
- `GET /api/tasks/{id}` - タスク詳細取得
- `POST /api/tasks` - タスク作成
- `PUT /api/tasks/{id}` - タスク更新
- `DELETE /api/tasks/{id}` - タスク削除
- `GET /api/tasks/status/{status}` - ステータス別タスク取得

## 使用技術

### バックエンド
- Kotlin
- Spring Boot 3.1.5
- Spring Data JPA
- PostgreSQL 17

### フロントエンド
- React 18
- TypeScript
- Axios（HTTP クライアント）
- CSS3

## 開発環境

- Java 17以上
- Node.js 16以上
- npm または yarn