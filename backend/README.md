# Kanban Board Backend

Kotlin + Spring Boot によるRESTful APIサーバー

## 技術スタック

- **言語**: Kotlin 2.1.21
- **フレームワーク**: Spring Boot 3.1.5
- **Java**: 21 (LTS)
- **データベース**: PostgreSQL 17
- **ORM**: Spring Data JPA (Hibernate)
- **マイグレーション**: Flyway
- **セキュリティ**: Spring Security + JWT
- **バリデーション**: Bean Validation
- **ビルドツール**: Gradle (Kotlin DSL)
- **テスト**: JUnit 5 + Mockito

## プロジェクト構成

```
backend/
├── src/main/kotlin/com/example/kanban/
│   ├── KanbanApplication.kt              # メインアプリケーション
│   ├── config/
│   │   ├── SecurityConfig.kt             # Spring Security設定
│   │   └── JwtAuthenticationFilter.kt    # JWT認証フィルター
│   ├── controller/
│   │   ├── AuthController.kt             # 認証エンドポイント
│   │   └── TaskController.kt             # タスク管理エンドポイント
│   ├── service/
│   │   ├── UserService.kt                # ユーザービジネスロジック
│   │   ├── TaskService.kt                # タスクビジネスロジック
│   │   └── JwtService.kt                 # JWT処理
│   ├── repository/
│   │   ├── UserRepository.kt             # ユーザーデータアクセス
│   │   └── TaskRepository.kt             # タスクデータアクセス
│   ├── model/
│   │   ├── User.kt                       # ユーザーエンティティ
│   │   └── Task.kt                       # タスクエンティティ
│   ├── dto/
│   │   ├── UserDto.kt                    # ユーザーDTO群
│   │   └── TaskDto.kt                    # タスクDTO群
│   └── exception/
│       └── GlobalExceptionHandler.kt     # グローバル例外ハンドラー
├── src/main/resources/
│   ├── application.yml                   # 開発環境設定
│   ├── application-test.yml              # テスト環境設定
│   ├── application-prod.yml              # 本番環境設定
│   └── db/migration/                     # Flywayマイグレーション
│       ├── V1__Create_tasks_table.sql
│       ├── V2__Create_users_table.sql
│       └── V3__Add_user_id_to_tasks.sql
└── src/test/kotlin/                      # テストコード
    └── com/example/kanban/
        ├── controller/                   # コントローラーテスト
        ├── service/                      # サービステスト
        └── exception/                    # 例外ハンドラーテスト
```

## 事前準備

### 必要な環境

- **Java 21**: OpenJDK 21以上
- **Docker & Docker Compose**: PostgreSQLコンテナ用
- **Git**: ソースコード管理

### データベース起動

```bash
# プロジェクトルートで実行
docker compose up -d

# データベース接続確認
docker compose ps
```

起動するサービス：
- **PostgreSQL**: `localhost:5432`
- **pgAdmin**: `localhost:5050` (admin@example.com / admin)
- **pgweb**: `localhost:8081`

## 起動方法

### 1. 開発サーバー起動

```bash
# backendディレクトリに移動
cd backend

# 実行権限付与（初回のみ）
chmod +x gradlew

# アプリケーション起動
./gradlew bootRun
```

### 2. 本番環境起動

```bash
# JARファイルビルド
./gradlew build

# JARファイル実行
java -jar build/libs/sample-kanban-backend-0.0.1-SNAPSHOT.jar
```

### 3. プロファイル指定起動

```bash
# テスト環境で起動
./gradlew bootRun --args='--spring.profiles.active=test'

# 本番環境で起動
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## API仕様

### ベースURL
```
http://localhost:8080/api
```

### 認証エンドポイント

| メソッド | エンドポイント | 説明 | 認証 |
|---------|-------------|------|-----|
| POST | `/auth/register` | ユーザー登録 | 不要 |
| POST | `/auth/login` | ログイン | 不要 |

### タスク管理エンドポイント

| メソッド | エンドポイント | 説明 | 認証 |
|---------|-------------|------|-----|
| GET | `/tasks` | 全タスク取得 | 必要 |
| GET | `/tasks/{id}` | 特定タスク取得 | 必要 |
| POST | `/tasks` | タスク作成 | 必要 |
| PUT | `/tasks/{id}` | タスク更新 | 必要 |
| DELETE | `/tasks/{id}` | タスク削除 | 必要 |
| GET | `/tasks/status/{status}` | ステータス別取得 | 必要 |

### 認証ヘッダー

```bash
Authorization: Bearer <JWT_TOKEN>
```

### レスポンス例

```json
# POST /api/auth/login
{
  "user": {
    "id": 1,
    "name": "山田太郎",
    "email": "yamada@example.com",
    "lastLogin": "2025-06-25T08:30:00",
    "createdAt": "2025-06-20T10:00:00",
    "updatedAt": "2025-06-25T08:30:00"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

# GET /api/tasks
[
  {
    "id": 1,
    "title": "プロジェクト計画書作成",
    "description": "Q1のプロジェクト計画書を作成する",
    "status": "TODO",
    "userId": 1,
    "createdAt": "2025-06-24T01:57:05.990696",
    "updatedAt": "2025-06-24T01:57:36.501278"
  }
]
```

## 開発ツール

### テスト実行

```bash
# 全テスト実行
./gradlew test

# テストレポート確認
open build/reports/tests/test/index.html
```

### コードチェック

```bash
# コンパイルチェック
./gradlew compileKotlin compileTestKotlin

# 警告表示
./gradlew build --warning-mode all
```

### データベース管理

```bash
# Flywayマイグレーション手動実行
./gradlew flywayMigrate

# マイグレーション情報確認
./gradlew flywayInfo

# データベースクリーン
./gradlew flywayClean
```

## 設定ファイル

### application.yml (開発環境)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kanban_db
    username: kanban_user
    password: kanban_password
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: none
  flyway:
    enabled: true
    baseline-on-migrate: true

server:
  port: 8080

logging:
  level:
    org.hibernate.SQL: DEBUG
```

### 環境変数

| 変数名 | 説明 | デフォルト値 |
|--------|------|-------------|
| `ENVIRONMENT` | 実行環境 | `local` |
| `DB_URL` | データベースURL | `jdbc:postgresql://localhost:5432/kanban_db` |
| `DB_USERNAME` | DBユーザー名 | `kanban_user` |
| `DB_PASSWORD` | DBパスワード | `kanban_password` |
| `JWT_SECRET` | JWT署名キー | 自動生成 |

## セキュリティ

### パスワード要件
- 6文字以上
- 英字と数字の両方を含む

### JWT認証
- トークン有効期限: 24時間
- HS256アルゴリズム使用
- 環境別強度設定（local: 10, cloud: 12）

### CORS設定
- フロントエンド（localhost:3000）からのアクセス許可
- 本番環境では適切なオリジン設定が必要

## トラブルシューティング

### よくある問題

#### 1. データベース接続エラー
```bash
# PostgreSQLコンテナ状態確認
docker compose ps

# コンテナログ確認
docker compose logs postgres

# コンテナ再起動
docker compose restart postgres
```

#### 2. ポート8080使用中エラー
```bash
# プロセス確認・停止
lsof -ti:8080 | xargs kill -9

# 別ポートで起動
./gradlew bootRun --args='--server.port=8081'
```

#### 3. Flywayマイグレーションエラー
```bash
# マイグレーション状態確認
./gradlew flywayInfo

# ベースライン設定
./gradlew flywayBaseline

# 修復実行
./gradlew flywayRepair
```

#### 4. JWTトークンエラー
- ログイン画面でトークン再取得
- ローカルストレージクリア
- サーバー再起動

## CI/CD

### GitHub Actions
- プルリクエスト時に自動テスト実行
- Kotlin/Java コンパイルチェック
- テストカバレッジレポート生成

### ローカルCI実行
```bash
# フルCI実行
./gradlew clean build test

# 警告込みチェック
./gradlew build --warning-mode all
```

## ライセンス

このプロジェクトは学習・デモ目的で作成されています。

## 開発者向け情報

### アーキテクチャパターン
- **レイヤードアーキテクチャ**: Controller → Service → Repository
- **DTOパターン**: エンティティとAPIの分離
- **依存性注入**: Spring DIコンテナ活用
