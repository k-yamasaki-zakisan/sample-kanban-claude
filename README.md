# Sample Kanban Board

Kotlin + Spring Boot バックエンドと React + TypeScript フロントエンドを使用したフルスタック カンバンボードアプリケーション

## 主要機能

### 🔐 認証・認可システム
- ユーザー登録・ログイン機能
- JWT トークンベース認証
- 自動ログアウト（トークン期限切れ時）
- セッション管理

### 📋 タスク管理
- タスクの作成、編集、削除
- 4段階のステータス管理（TODO / In Progress / In Review / Done）
- ドラッグ&ドロップによるタスク移動
- タスク説明文の自動テキスト切り詰め（長文対応）

### 🎯 ユーザーインターフェース
- URL ベースのページルーティング（React Router）
- レスポンシブデザイン（モバイル対応）
- ハンバーガーメニューナビゲーション
- リアルタイム通知システム
- ダッシュボード（タスク統計・チャート）
- ユーザープロフィール管理

### 🔧 開発・運用機能
- カスタム JPQL クエリ実装
- 包括的エラーハンドリング
- TypeScript による型安全性
- ESLint によるコード品質管理
- 自動テスト（Jest）

## 🚀 クイックスタート

### 前提条件
- **Java**: 21 (LTS)
- **Node.js**: 22.x
- **Docker**: 最新版
- **npm**: Node.js 付属版

### 1. リポジトリクローン
```bash
git clone https://github.com/k-yamasaki-zakisan/sample-kanban-claude.git
cd sample-kanban-claude
```

### 2. データベース起動（Docker）
```bash
# PostgreSQL、pgAdmin、pgweb を起動
docker compose up -d

# 起動確認
docker compose ps
```

### 3. バックエンド起動（Spring Boot）
```bash
cd backend

# 実行権限設定（初回のみ）
chmod +x gradlew

# アプリケーション起動
./gradlew bootRun
```
バックエンド API: http://localhost:8080

### 4. フロントエンド起動（React）
```bash
cd frontend

# 依存関係インストール（初回のみ）
npm ci

# アプリケーション起動
npm start
```
フロントエンド: http://localhost:3000

### 5. アクセス先
- **メインアプリケーション**: http://localhost:3000
- **API エンドポイント**: http://localhost:8080/api
- **pgAdmin**: http://localhost:5050 (admin@example.com / admin)
- **pgweb**: http://localhost:8081

## 📚 API ドキュメント

### 認証エンドポイント
| メソッド | エンドポイント | 説明 | 認証 |
|---------|-------------|------|------|
| POST | `/api/auth/register` | ユーザー登録 | 不要 |
| POST | `/api/auth/login` | ログイン | 不要 |

### タスク管理エンドポイント
| メソッド | エンドポイント | 説明 | 認証 |
|---------|-------------|------|------|
| GET | `/api/tasks` | 全タスク取得 | 必要 |
| GET | `/api/tasks/{id}` | 特定タスク取得 | 必要 |
| POST | `/api/tasks` | タスク作成 | 必要 |
| PUT | `/api/tasks/{id}` | タスク更新 | 必要 |
| DELETE | `/api/tasks/{id}` | タスク削除 | 必要 |
| GET | `/api/tasks/status/{status}` | ステータス別取得 | 必要 |

**認証**: Bearer トークンをヘッダーに含める
```
Authorization: Bearer <JWT_TOKEN>
```

## 🛠 技術スタック

### バックエンド
- **言語**: Kotlin 2.1.21
- **フレームワーク**: Spring Boot 3.1.5
- **データアクセス**: カスタム JPQL + EntityManager
- **データベース**: PostgreSQL 17
- **認証**: JWT (JSON Web Token)
- **ビルドツール**: Gradle (Kotlin DSL)
- **Java**: 21 (LTS)

### フロントエンド  
- **言語**: TypeScript
- **フレームワーク**: React 19
- **ルーティング**: React Router 6
- **状態管理**: React Hooks + Context API
- **HTTP クライアント**: Axios (認証インターセプター付き)
- **UI/UX**: CSS3 + レスポンシブデザイン
- **ドラッグ&ドロップ**: @dnd-kit/core
- **ビルドツール**: npm

### インフラ・開発
- **コンテナ**: Docker Compose
- **データベース管理**: pgAdmin, pgweb
- **テスト**: Jest (フロントエンド), JUnit (バックエンド)  
- **コード品質**: ESLint, TypeScript strict mode
- **バージョン管理**: Git

## 🏗 アーキテクチャ

### システム構成図
```
┌─────────────────┐    HTTP/REST    ┌─────────────────┐
│   React SPA     │◄─────────────► │  Spring Boot    │
│  (Port: 3000)   │    (JWT Auth)   │  (Port: 8080)   │
└─────────────────┘                 └─────────────────┘
        │                                    │
        │                                    │ JPQL
        │                                    ▼
        │                           ┌─────────────────┐
        └─── Routing ───────────────│  PostgreSQL     │
             Navigation              │  (Port: 5432)   │
                                    └─────────────────┘
```

### フロントエンド構成
- **コンポーネント設計**: 機能別モジュール化
- **ルーティング**: URL ベースのページ遷移
- **認証管理**: JWT トークンによる状態管理
- **エラーハンドリング**: グローバルインターセプター
- **通知システム**: Context API ベースの通知

### バックエンド構成
- **レイヤー構造**: Controller → Service → Repository
- **カスタムリポジトリ**: JPQL による直接クエリ制御
- **セキュリティ**: JWT ベースの認証・認可
- **エラーハンドリング**: 統一された例外処理

## 🧪 開発・テスト

### フロントエンド
```bash
cd frontend

# テスト実行
npm test

# カバレッジ付きテスト
npm test -- --coverage --watchAll=false

# Lint チェック
npm run lint

# 型チェック
npx tsc --noEmit
```

### バックエンド
```bash
cd backend

# テスト実行
./gradlew test --info

# コンパイルチェック
./gradlew compileKotlin compileTestKotlin --warning-mode all

# ビルド
./gradlew build
```

## 📈 今後の拡張予定

### 機能拡張
- [ ] タスクの優先度設定
- [ ] タスクへのファイル添付機能
- [ ] タスクのコメント・履歴機能
- [ ] チーム機能・権限管理
- [ ] 通知機能（メール、リアルタイム）
- [ ] カレンダービュー
- [ ] レポート・分析機能

### 技術改善
- [ ] WebSocket によるリアルタイム更新
- [ ] Redis によるセッション管理
- [ ] Docker によるフル環境コンテナ化
- [ ] CI/CD パイプライン（GitHub Actions）
- [ ] パフォーマンス監視・ログ分析
- [ ] API ドキュメント自動生成（OpenAPI）

## 🤝 コントリビューション

1. このリポジトリをフォーク
2. 機能ブランチを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をコミット (`git commit -m 'Add amazing feature'`)
4. ブランチにプッシュ (`git push origin feature/amazing-feature`)
5. プルリクエストを作成

## 📝 ライセンス

このプロジェクトは学習・デモ目的で作成されています。

## 👨‍💻 開発者

- **Repository**: [sample-kanban-claude](https://github.com/k-yamasaki-zakisan/sample-kanban-claude)
- **開発**: Claude Code + Human Developer