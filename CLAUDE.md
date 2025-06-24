# Sample Kanban Board - プロジェクト概要

## プロジェクト概要

Kotlin + Spring Boot をバックエンド、React + TypeScript をフロントエンドとした、フルスタックのカンバンボードアプリケーションです。PostgreSQL をデータベースとして使用し、Docker Compose で開発環境を構築しています。

## アーキテクチャ

### システム構成
- **フロントエンド**: React 18 + TypeScript + CSS3
- **バックエンド**: Kotlin + Spring Boot 3.1.5 + Spring Data JPA
- **データベース**: PostgreSQL 17
- **開発環境**: Docker Compose
- **ビルドツール**: Gradle (Kotlin DSL), npm

### アプリケーション構造
```
sample-kanban/
├── backend/                    # Kotlin + Spring Boot API
│   ├── src/main/kotlin/com/example/kanban/
│   │   ├── KanbanApplication.kt       # メインアプリケーションクラス
│   │   ├── controller/
│   │   │   └── TaskController.kt      # REST APIコントローラー
│   │   ├── service/
│   │   │   └── TaskService.kt         # ビジネスロジック層
│   │   ├── repository/
│   │   │   └── TaskRepository.kt      # データアクセス層
│   │   ├── model/
│   │   │   └── Task.kt               # エンティティクラス
│   │   └── dto/
│   │       └── TaskDto.kt            # データ転送オブジェクト
│   ├── src/main/resources/
│   │   ├── application.yml           # 開発環境設定
│   │   ├── application-test.yml      # テスト環境設定
│   │   └── application-prod.yml      # 本番環境設定
│   ├── build.gradle.kts              # Gradleビルド設定
│   └── settings.gradle.kts
├── frontend/                   # React + TypeScript SPA
│   ├── src/
│   │   ├── components/
│   │   │   ├── KanbanBoard.tsx       # メインのカンバンボード
│   │   │   ├── TaskCard.tsx          # タスクカードコンポーネント
│   │   │   └── TaskForm.tsx          # タスク作成・編集フォーム
│   │   ├── services/
│   │   │   └── api.ts                # API通信ライブラリ
│   │   ├── types/
│   │   │   └── Task.ts               # TypeScript型定義
│   │   ├── App.tsx                   # ルートコンポーネント
│   │   └── index.tsx                 # エントリーポイント
│   ├── package.json                  # npm依存関係
│   └── tsconfig.json                 # TypeScript設定
├── database/
│   └── init/
│       └── 01-init.sql              # データベース初期化スクリプト
├── docker-compose.yml               # Docker Compose設定
├── README.md                        # プロジェクト説明
└── CLAUDE.md                        # Claude用プロジェクト概要（このファイル）
```

## データモデル

### Task エンティティ
```kotlin
data class Task(
    val id: Long = 0,
    val title: String,                    // タスクタイトル（必須）
    val description: String? = null,      // タスク詳細（任意）
    val status: TaskStatus = TaskStatus.TODO,  // ステータス
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class TaskStatus {
    TODO,        // 未着手
    IN_PROGRESS, // 進行中
    DONE         // 完了
}
```

### DTO (Data Transfer Objects)
```kotlin
// タスク作成用
data class TaskCreateDto(
    val title: String,
    val description: String? = null
)

// タスク更新用
data class TaskUpdateDto(
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null
)

// API レスポンス用
data class TaskResponseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

## API エンドポイント

### REST API 仕様
- **ベースURL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **CORS**: フロントエンド（localhost:3000）からのアクセスを許可

#### タスク管理エンドポイント

| メソッド | エンドポイント | 説明 | リクエストボディ | レスポンス |
|---------|-------------|------|----------------|-----------|
| GET | `/tasks` | 全タスク取得 | なし | `TaskResponseDto[]` |
| GET | `/tasks/{id}` | 特定タスク取得 | なし | `TaskResponseDto` |
| POST | `/tasks` | タスク作成 | `TaskCreateDto` | `TaskResponseDto` |
| PUT | `/tasks/{id}` | タスク更新 | `TaskUpdateDto` | `TaskResponseDto` |
| DELETE | `/tasks/{id}` | タスク削除 | なし | なし（204） |
| GET | `/tasks/status/{status}` | ステータス別取得 | なし | `TaskResponseDto[]` |

#### レスポンス例
```json
// GET /api/tasks
[
  {
    "id": 1,
    "title": "プロジェクト計画書作成",
    "description": "Q1のプロジェクト計画書を作成する",
    "status": "TODO",
    "createdAt": "2025-06-24T01:57:05.990696",
    "updatedAt": "2025-06-24T01:57:36.501278"
  }
]
```

## フロントエンド構成

### コンポーネント設計
- **KanbanBoard**: メインコンポーネント、3つのカラム（TODO/IN_PROGRESS/DONE）を表示
- **TaskCard**: 個別タスクの表示、ステータス変更、編集・削除ボタン
- **TaskForm**: タスクの作成・編集用モーダルフォーム

### 状態管理
- React Hooks (useState, useEffect) を使用
- API通信は axios ライブラリを使用
- エラーハンドリングとローディング状態の管理

### スタイリング
- CSS3 を使用（CSS Modules ではなく通常のCSS）
- レスポンシブデザイン対応
- カンバンボード風のUI/UX

## 開発環境

### 必要な環境
- **Java**: 17以上
- **Node.js**: 22 (メジャーバージョン固定)
- **Docker**: 最新版
- **npm**: Node.js付属版

### Docker Services
```yaml
# docker-compose.yml で定義されているサービス
services:
  postgres:      # PostgreSQL 17
    ports: ["5432:5432"]
    
  pgadmin:       # pgAdmin（データベース管理）
    ports: ["5050:80"]
    
  pgweb:         # pgweb（軽量DB管理ツール）
    ports: ["8081:8081"]
```

### 起動手順
```bash
# 1. データベース起動
docker compose up -d

# 2. バックエンド起動
cd backend
./gradlew bootRun

# 3. フロントエンド起動（別ターミナル）
cd frontend
npm start
```

### アクセスURL
- **メインアプリケーション**: http://localhost:3000
- **API**: http://localhost:8080/api/tasks
- **pgAdmin**: http://localhost:5050 (admin@example.com / admin)
- **pgweb**: http://localhost:8081

## 機能説明

### 実装済み機能
1. **タスク管理**
   - タスクの作成（タイトル・説明文）
   - タスクの編集（タイトル・説明文の修正）
   - タスクの削除（確認ダイアログ付き）
   - タスクのステータス変更（セレクトボックス）

2. **カンバンボード**
   - 3カラム表示（TODO / In Progress / Done）
   - タスク数のカウント表示
   - 作成日時順での表示
   - リアルタイムでの状態反映

3. **ユーザーインターフェース**
   - モーダルフォームでのタスク作成・編集
   - レスポンシブデザイン
   - エラーメッセージ表示
   - ローディング状態表示

### データベース設計
- **テーブル**: `tasks`
- **主キー**: `id` (bigserial)
- **インデックス**: 作成日時でのソート用
- **制約**: ステータスはENUM型で制限

## 技術的特徴

### バックエンド（Kotlin + Spring Boot）
- **Spring Data JPA**: データベースアクセス
- **Hibernate**: ORM機能
- **Jackson**: JSON シリアライゼーション
- **HikariCP**: コネクションプール
- **CORS設定**: フロントエンドとの連携

### フロントエンド（React + TypeScript）
- **TypeScript**: 型安全性の確保
- **React Hooks**: 関数コンポーネントでの状態管理
- **Axios**: HTTP クライアント
- **CSS3**: モダンなスタイリング

### インフラ・DevOps
- **Docker Compose**: 開発環境の統一
- **PostgreSQL**: 本格的なRDBMS
- **環境別設定**: dev/test/prod の設定分離

## 拡張予定・改善点

### 今後の拡張可能性
1. **ドラッグ&ドロップ**: react-beautiful-dnd の導入
2. **認証機能**: Spring Security + JWT
3. **リアルタイム更新**: WebSocket対応
4. **タスクの優先度**: 優先度フィールドの追加
5. **担当者機能**: ユーザー管理機能
6. **期日管理**: 締切日の設定・通知
7. **ファイル添付**: タスクへの添付ファイル機能

### パフォーマンス改善
1. **フロントエンド**: React.memo、useCallback の活用
2. **バックエンド**: N+1問題の対策、キャッシュ導入
3. **データベース**: インデックス最適化

## トラブルシューティング

### よくある問題と解決方法

1. **PostgreSQL接続エラー**
   ```bash
   # Dockerコンテナの状態確認
   docker ps
   
   # ログ確認
   docker logs kanban-postgres
   ```

2. **フロントエンドでAPI接続エラー**
   - バックエンドの起動確認: http://localhost:8080/api/tasks
   - CORS設定の確認
   - ブラウザのネットワークタブでエラー詳細確認

3. **Gradleビルドエラー**
   ```bash
   # Gradle Wrapper の再生成
   gradle wrapper
   
   # クリーンビルド
   ./gradlew clean build
   ```

## Git リポジトリ情報

- **リポジトリ**: https://github.com/k-yamasaki-zakisan/sample-kanban-claude
- **ブランチ戦略**: main ブランチでの開発
- **コミット形式**: 機能単位でのコミット

## ライセンス・その他

このプロジェクトは学習・デモ目的で作成されており、商用利用については適切なライセンスの検討が必要です。

---

## Claude への指示

このプロジェクトについて作業する際は、以下の点を考慮してください：

1. **コード規約**: 既存のコードスタイルを維持
2. **型安全性**: TypeScript/Kotlinの型を適切に使用
3. **エラーハンドリング**: 適切な例外処理とユーザーフィードバック
4. **テスト**: 新機能追加時は適切なテストの実装
5. **ドキュメント**: 変更時はこのCLAUDE.mdの更新も検討
6. **コミット前のCI実行**: 必ずローカルでCI/CDを実行してからコミットする

### コミット前のローカルCI実行ルール

**重要**: コードの変更を行った際は、必ずコミット前に以下のローカルCIチェックを実行してください：

#### フロントエンド関連の変更時
```bash
# フロントエンドディレクトリで実行
cd frontend

# 依存関係のインストール（初回のみ）
npm ci

# ESLintチェック
npm run lint

# テスト実行
npm test -- --coverage --watchAll=false
```

#### バックエンド関連の変更時
```bash
# バックエンドディレクトリで実行
cd backend

# 実行権限設定（初回のみ）
chmod +x gradlew

# コンパイルチェック
./gradlew compileKotlin compileTestKotlin --warning-mode all

# テスト実行
./gradlew test --info
```

#### 全体チェック
フロントエンドとバックエンドの両方に変更がある場合は、両方のチェックを実行してください。

#### CI失敗時の対応
- ESLintエラー: コードスタイルの修正または適切なESLint設定の調整
- テスト失敗: テストケースの修正またはコードの不具合修正
- コンパイルエラー: 型エラーや構文エラーの修正

すべてのローカルCIが成功してからコミット・プッシュを行うことで、GitHub Actionsでの失敗を防ぎ、開発効率を向上させます。

プロジェクトの構造や設計思想を理解した上で、一貫性のある開発を行ってください。