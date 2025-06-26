# Spring Bootホットリロード使用ガイド

## 開発環境でのホットリロード

### 起動方法

**ターミナル1** (サーバー起動):
```bash
# 開発プロファイルで起動（DevTools有効）
./gradlew bootRun --args='--spring.profiles.active=dev'
# または
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

**ターミナル2** (継続コンパイル):
```bash
./gradlew -t compileKotlin
```

### 動作確認
- ファイル編集 → 自動コンパイル → DevToolsが自動再起動
- `http://localhost:8080/api/healthcheck` で変更確認

## 本番環境での起動

### 起動方法
```bash
# 本番プロファイルで起動（DevTools無効）
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

### 特徴
- ✅ DevToolsは完全に無効
- ✅ パフォーマンス最適化
- ✅ セキュリティ強化
- ✅ メモリ使用量最小化

## 環境変数

| 変数名 | 説明 | 開発環境 | 本番環境 |
|--------|------|----------|----------|
| `SPRING_PROFILES_ACTIVE` | プロファイル指定 | `dev` | `prod` |
| `DATABASE_URL` | DB接続URL | localhost | 本番DB |
| `DB_USERNAME` | DBユーザー名 | kanban_user | 本番ユーザー |
| `DB_PASSWORD` | DBパスワード | kanban_password | 本番パスワード |

## トラブルシューティング

### ホットリロードが動作しない場合
1. 開発プロファイルで起動しているか確認
2. 継続コンパイルが動作しているか確認
3. IDEの自動コンパイル設定を確認

### 本番環境での注意
- DevToolsは自動的に無効化される
- ホットリロード機能は利用不可
- 変更にはアプリケーション再起動が必要