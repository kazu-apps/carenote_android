# CareNote - 家族でつながる介護記録 Android アプリ

服薬管理、健康記録、カレンダー、タスク、メモ・申し送りを搭載した
家族介護者向け Android ネイティブアプリ。

## エージェントチーム構成

> **task-driver v8 連携**: `/task-driver` スキル使用時は `~/.claude/skills/task-driver/SKILL.md` の手順に従う。

すべての開発タスクは4人のエージェントチームで実行する（リーダー1人 + ワーカー3人）。

### リーダーの絶対ルール（違反厳禁）

**禁止ツール:** `Write`, `Edit`, `Bash`（git commit 等の破壊的コマンド）
**許可ツール:** `Read`, `Glob`, `Grep`, `TeamCreate`, `Task`（`team_name` + `name` 必須）, `TaskCreate/Update/List`, `SendMessage`, `TaskOutput`

### ワーカー生成の正しい方法

**必須パラメータ**: `team_name` と `name` を必ず指定する。

~~~
Task(team_name: "my-team", name: "worker-a", subagent_type: "general-purpose", prompt: "...")
~~~

### ワーカーへの指示ルール
- 各ワーカーには担当ファイル/ディレクトリを明示的に指定する
- 同じファイルを複数ワーカーが同時編集しない
- 共有リソース（build.gradle.kts、libs.versions.toml等）は1人だけが担当する
- ワーカー同士は `SendMessage` で発見や依存情報を共有する
- 依存関係があるワーカーは、先行ワーカーの完了メッセージを待ってから開始する

### sub-agent-patterns 原則

1. **ツールアクセス制限**: Worker プロンプトの冒頭に許可/禁止ツールを明記
2. **重要指示先頭配置**: 制約事項・禁止事項はプロンプトの冒頭に配置
3. **コンテキスト衛生**: Worker に渡す情報は必要最小限
4. **2層深さ制限**: Leader → Worker の2層まで
5. **Bash approval spam 防止**: 実装 Worker は Bash 禁止、worker-quality のみ許可

---

## クイックリファレンス

~~~bash
# ビルド
./gradlew.bat assembleDebug

# ユニットテスト
./gradlew.bat testDebugUnitTest

# 特定テストクラス実行
./gradlew.bat testDebugUnitTest --tests "com.carenote.app.domain.common.ResultTest"

# UI テスト（要エミュレータ）
./gradlew.bat connectedDebugAndroidTest

# カバレッジ（80% LINE 閾値）
./gradlew.bat jacocoTestReport jacocoTestCoverageVerification

# スクリーンショットテスト（golden image 記録）
./gradlew.bat recordRoborazziDebug

# スクリーンショット回帰テスト（CI 用）
./gradlew.bat verifyRoborazziDebug

# 静的解析（CLI ツール。Gradle プラグインではない）
detekt --config detekt.yml --input app/src/main/java

# OSS 脆弱性スキャン（依存追加/更新時は必須）
./gradlew.bat :app:dependencies --write-locks && osv-scanner scan source -r .
~~~

## 技術スタック

| カテゴリ | 技術 |
|---------|------|
| 言語 | Kotlin 2.3.0 / JVM 17 |
| UI | Jetpack Compose + Material 3 (BOM 2026.01.01) |
| DI | Hilt 2.59.1 (KSP 2.3.5) |
| DB | Room 2.8.4 + SQLCipher 4.6.1 (`carenote_database` v25) |
| ナビゲーション | Navigation Compose 2.9.7 |
| 非同期 | Coroutines 1.10.2 + StateFlow |
| ログ | Timber 5.0.1 |
| Firebase | BOM 34.8.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics) |
| WorkManager | 2.10.1 (HiltWorker) |
| Paging | Paging 3.3.6 (Runtime + Compose) |
| 画像 | Coil 3.1.0 |
| Widget | Glance 1.1.1 |
| セキュリティ | Biometric 1.1.0 |
| Adaptive | Material3 Adaptive Navigation Suite |
| テスト | JUnit 4 + MockK 1.14.3 + Turbine 1.0.0 + Robolectric 4.16 + Roborazzi 1.58.0 |
| SDK | compileSdk 36, minSdk 26, targetSdk 36 |

## アーキテクチャ

### Clean Architecture（依存方向: ui → domain → data）

- **ui**: Jetpack Compose Screen + ViewModel (Hilt @Inject)。State は `StateFlow` で管理
- **domain**: Repository interfaces, domain models, `Result<T, DomainError>`
- **data**: Room DB, Firestore, Repository implementations, Mapper

DI モジュール・ナビゲーション・同期パターンの詳細は `docs/ARCHITECTURE.md` 参照。

### エラーハンドリング

- `domain/common/Result.kt` — 独自の `Result<T, E>` sealed class（kotlin.Result ではない）
- `domain/common/DomainError.kt` — 6 種の sealed class (Database, NotFound, Validation, Network, Unauthorized, Unknown)
- DomainError は **Throwable ではない**。Timber に渡す際は `Timber.w("msg: $error")` と文字列化

## パッケージ構成

`app/src/main/java/com/carenote/app/` 配下:
config/ | data/ (export, local, mapper, remote, repository, worker) | domain/ (common, model, repository, util) | ui/ (components, navigation, screens, theme, viewmodel, widget) | di/

詳細は `docs/ARCHITECTURE.md#パッケージ構成詳細` 参照。

## Firebase 統合

Auth, Firestore, FCM, Crashlytics, Storage, Analytics。`google-services.json` 未配置時は No-Op 実装でグレースフルデグラデーション。
詳細は `docs/ARCHITECTURE.md#firebase-統合` 参照。

## Worker パターン

SyncWorker (15分間隔), MedicationReminderWorker, TaskReminderWorker, CalendarEventReminderWorker。
詳細は `docs/ARCHITECTURE.md#worker-パターン` 参照。

## テーマ

詳細は `docs/ARCHITECTURE.md#テーマ` 参照。

## テスト

### 構成

| 種類 | フレームワーク | 場所 |
|------|-------------|------|
| Unit | JUnit 4 + MockK + Turbine + Coroutines Test | `app/src/test/` |
| UI/E2E | Hilt + Espresso + UIAutomator + Compose UI Test | `app/src/androidTest/` |
| Screenshot | Roborazzi 1.58.0 + ComposablePreviewScanner 0.8.1 | `app/src/test/snapshots/` |
| Benchmark | Macrobenchmark 1.4.1 | `benchmark/` |
| Baseline Profile | baselineprofile 1.5.0-alpha02 | `baselineprofile/` |
| Runner | `com.carenote.app.HiltTestRunner` | build.gradle.kts |
| カバレッジ | JaCoCo 0.8.12（LINE 80% 閾値） | `jacocoTestCoverageVerification` |

### Fake Repository パターン

`test/.../fakes/` に配置。`MutableStateFlow<List<T>>` でインメモリ状態管理。
全 Fake クラス一覧は `docs/ARCHITECTURE.md#fake-repository-一覧` 参照。

### E2E テスト

`androidTest/.../di/TestFirebaseModule.kt` で本番モジュールを Fake に置換。18 テストファイル。
テストファイル一覧は `docs/ARCHITECTURE.md#e2e-テスト一覧` 参照。

## コード規約

### ログ

**Timber 必須**。`println()`, `Log.d()`, `Log.e()` 等は禁止。

**PII ログ禁止**: UID, email, 個人名等をログに含めない。
~~~kotlin
// NG
Timber.d("User signed in: ${user.uid}")

// OK
Timber.d("User signed in successfully")
~~~

### i18n（多言語対応）

- デフォルト: 日本語 `res/values/strings.xml`
- 英語: `res/values-en/strings.xml`
- **新規文字列追加時は必ず両方のファイルを更新**

### 設定値

マジックナンバーは全て `config/AppConfig.kt` に集約。直接リテラルを使わない。

主要カテゴリ:
- `AppConfig.Auth` — 認証関連（パスワード長、メール長）
- `AppConfig.Sync` — 同期関連（タイムアウト、リトライ回数）
- `AppConfig.Notification` — 通知チャンネル ID
- `AppConfig.Biometric` — 生体認証（バックグラウンドタイムアウト）
- `AppConfig.Widget` — ウィジェット表示件数
- `AppConfig.Export` — エクスポート設定（CSV/PDF ファイルプレフィックス、PDF 寸法）
- `AppConfig.Photo` — 画像キャッシュ TTL/サイズ上限、圧縮品質
- `AppConfig.UI` — デバウンス時間、アニメーション、Badge 最大値、検索デバウンス等
- `AppConfig.Support` — 問い合わせメールアドレス
- `AppConfig.Member` — 招待リンク設定（DEEP_LINK_HOST, DEEP_LINK_PATH_PREFIX, トークン有効期限）
- `AppConfig.Billing` — プレミアム機能設定（SKU, 機能制限値）
- `AppConfig.Analytics` — 画面名定数 + イベント定数（40+ 種）

### OSS 依存管理

- **新規依存追加/バージョン更新時は必ず OSV-Scanner を実行**
- 手順: (1) `libs.versions.toml` 編集 → (2) `./gradlew.bat :app:dependencies --write-locks` → (3) `osv-scanner scan source -r .`
- 脆弱性が見つかった場合: バージョン変更 or `osv-scanner.toml` に理由付きで除外登録
- `gradle.lockfile` は git にコミットする（OSV-Scanner のスキャン対象）

### Detekt ルール（maxIssues=0）

| ルール | 閾値 |
|--------|------|
| LongMethod | 50 行 |
| LargeClass | 800 行 |
| MaxLineLength | 120 文字 |
| NestedBlockDepth | 4 |
| CyclomaticComplexMethod | 15 |

## よくある落とし穴

1. **Detekt は CLI ツール** — Gradle プラグインとして追加しないこと（MockK インストルメンテーションと競合）
2. **Room Entity 変更時** — Migration ファイル作成 + `DatabaseModule.kt` への登録が必須（v25 baseline。`room-testing` 依存追加 + MigrationTest 作成が必要）
3. **strings.xml は JP/EN ペア更新** — 片方だけ更新すると実行時に英語/日本語が混在
4. **DomainError は Throwable ではない** — `Timber.w(error, msg)` は使えない。`Timber.w("msg: $error")` と書く
5. **Result は独自実装** — `domain/common/Result.kt` の `Result<T, E>`。kotlin.Result ではない
6. **Windows 環境** — `./gradlew.bat` を使用。パス区切りは `\`
7. **ProGuard (release)** — 新ライブラリ追加時は `app/proguard-rules.pro` の keep ルール確認
8. **Zero Detekt tolerance** — maxIssues=0, all issues must be fixed
10. **PII ログ禁止** — UID, email, 個人名をログに含めない（L-2 セキュリティ要件）
20. **エクスポート PII 注意** — CSV/PDF エクスポートに患者情報を含む。キャッシュクリア、ログ PII 禁止ルール遵守
21. **Worker Bash approval spam** — 実装 Worker は Bash 禁止、worker-quality のみに許可
23. **OSS 依存追加時** — 必ず lockfile 再生成 + `osv-scanner scan source -r .` を実行。CI でもブロックされる

その他の落とし穴（#9, #11-19, #22）は `docs/ARCHITECTURE.md#よくある落とし穴詳細` 参照。
