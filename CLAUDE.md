# CareNote - 家族でつながる介護記録 Android アプリ

服薬管理、健康記録、カレンダー、タスク、メモ・申し送りを搭載した
家族介護者向け Android ネイティブアプリ。

## エージェントチーム構成

> **task-driver v8 連携**: `/task-driver` スキル使用時は、本セクションのルールに加え `~/.claude/skills/task-driver/SKILL.md` の手順に従う。Plan モードでは TeamCreate 3 Worker 並列リサーチ、Run モードでは Explore リサーチ + TeamCreate 3 Worker 実装のハイブリッドモデルを採用。

すべての開発タスクは4人のエージェントチームで実行する。
リーダー1人 + ワーカー3人の構成。

### リーダーの絶対ルール（違反厳禁）

**リーダーは以下のツールを絶対に使わない:**
- `Write` — ファイル作成禁止
- `Edit` — ファイル編集禁止
- `Bash`（git commit 等の破壊的コマンド）— ワーカーに委託する

**リーダーが使えるツール:**
- `Read`, `Glob`, `Grep` — コードベース調査
- `TeamCreate` — チーム作成
- `Task`（`team_name` + `name` 必須）— ワーカー生成
- `TaskCreate`, `TaskUpdate`, `TaskList` — タスク管理
- `SendMessage` — ワーカーへの指示・軌道修正
- `TaskOutput` — ワーカー進捗確認

### ワーカー生成の正しい方法

**必須パラメータ**: `team_name` と `name` を必ず指定する。

```
// 正しい: チームメンバーとして生成
Task(
  team_name: "my-team",
  name: "worker-a",
  subagent_type: "general-purpose",
  prompt: "..."
)

// 禁止: team_name なしのバックグラウンドエージェント
Task(
  subagent_type: "general-purpose",
  run_in_background: true,  // ← これだとチームメンバーにならない
  prompt: "..."
)
```

**禁止パターン（過去の失敗）:**
- リーダーが「Worker A は簡単だから自分でやる」→ 禁止。全作業はワーカーに委託
- `Task` に `team_name` を渡さずバックグラウンド起動 → チーム連携不可。禁止
- ワーカー間で `SendMessage` を使わず独立実行 → 禁止。依存情報は必ず共有

### ワーカーへの指示ルール
- 各ワーカーには担当ファイル/ディレクトリを明示的に指定する
- 同じファイルを複数ワーカーが同時編集しない
- 共有リソース（build.gradle.kts、libs.versions.toml等）は1人だけが担当する
- ワーカー同士は `SendMessage` で発見や依存情報を共有する
- 依存関係があるワーカーは、先行ワーカーの完了メッセージを待ってから開始する

### sub-agent-patterns 原則

エージェント（Leader/Worker）のプロンプト設計で遵守すべき原則:

1. **ツールアクセス制限**: Worker プロンプトの**冒頭**に許可/禁止ツールを明記する。Worker が不要なツール（Write, Bash 等）を使うリスクを防ぐ
2. **重要指示先頭配置**: 制約事項・禁止事項はプロンプトの冒頭に配置する。末尾に置くとモデルが無視するリスクがある
3. **コンテキスト衛生**: Worker に渡す情報は必要最小限にする。巨大ファイルの全文コピーやプロジェクト全体の説明は避け、担当範囲に関連する情報のみ渡す
4. **2層深さ制限**: Leader → Worker の2層まで。Worker が Task/TeamCreate で更にサブエージェントを生成することは禁止
5. **Bash approval spam 防止**: 実装 Worker（worker-impl, worker-test）は Bash 禁止。ビルド/テスト実行は worker-quality のみが担当。これにより approval プロンプトの頻度を最小化

### テンプレート集：タスクに応じた3ワーカーの役割

リーダーは依頼内容に応じて、以下から最適なテンプレートを選択する。
テンプレートにない組み合わせが最適な場合は、自由にカスタマイズしてよい。

| テンプレート | ワーカーA | ワーカーB | ワーカーC |
|------------|----------|----------|----------|
| 🔨 新機能実装 | 実装者（ロジック+UI） | 基盤担当（DB/API/型定義） | 品質担当（テスト+ドキュメント） |
| 🐛 バグ調査 | 仮説①（最有力原因） | 仮説②（次点原因） | 仮説③（その他） |
| 📝 コードレビュー | セキュリティ観点 | 品質・パフォーマンス観点 | テストカバレッジ観点 |
| 🔄 リファクタリング | 構造改善 | テスト整備 | 移行作業 |
| 🏗️ レイヤー別実装 | フロントエンド | バックエンド | データ層 |
| 📦 リリース準備 | コード仕上げ | テスト強化 | ドキュメント |
| 🔍 技術調査 | 既存コード分析 | 外部リサーチ | プロトタイプ検証 |

#### task-driver 専用テンプレート

| モード | Worker | 役割 | Bash |
|--------|--------|------|------|
| Plan リサーチ | researcher | 事実収集 | ❌ |
| Plan リサーチ | architect | 方針提案 | ❌ |
| Plan リサーチ | critic | 穴突き | ❌ |
| Run 実装 | worker-impl | ロジック実装 | ❌ |
| Run 実装 | worker-test | テスト実装 | ❌ |
| Run 実装 | worker-quality | ビルド/テスト確認 | ✅ |

詳細は `~/.claude/skills/task-driver/references/team-templates.md` を参照。

### リーダーの進行フロー
1. ユーザーの依頼を分析し、上記テンプレートから最適なものを選ぶ
2. 3ワーカーの役割、担当ファイル、タスク依存関係を決定する
3. `TeamCreate` でチーム作成
4. `TaskCreate` でタスク作成（依存関係は `addBlockedBy` で設定）
5. `Task`（`team_name` + `name` 必須）で3ワーカーを生成し、十分なコンテキストを渡す
6. ワーカーからの `SendMessage` 通知で進捗を把握、必要に応じて `SendMessage` で軌道修正
7. 全ワーカー完了後、`Read` で成果物を統合レビューし、問題があれば `SendMessage` で修正指示
8. `SendMessage`（type: shutdown_request）で全ワーカーを終了
9. `TeamDelete` でチームリソースをクリーンアップ
10. 最終確認してユーザーに結果を報告する

---

## クイックリファレンス

```bash
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
```

## 技術スタック

| カテゴリ | 技術 |
|---------|------|
| 言語 | Kotlin 2.3.0 / JVM 17 |
| UI | Jetpack Compose + Material 3 (BOM 2026.01.01) |
| DI | Hilt 2.59.1 (KSP 2.3.5) |
| DB | Room 2.8.4 + SQLCipher 4.6.1 (`carenote_database` v23, fallbackToDestructiveMigration) |
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

### DI モジュール

| モジュール | 責務 |
|-----------|------|
| `di/AppModule.kt` | 15 Repository + 8 Exporter + Clock/Compressor/RootDetector/PremiumFeatureGuard バインディング |
| `di/DatabaseModule.kt` | Room DB + DAO (14 テーブル) + PassphraseManager + RecoveryHelper |
| `di/FirebaseModule.kt` | FirebaseAuth, Firestore, Messaging, Storage, Analytics + AuthRepository + AnalyticsRepository + No-Op フォールバック |
| `di/SyncModule.kt` | SyncRepository + EntitySyncer 群 |
| `di/WorkerModule.kt` | WorkManager + 3 Scheduler (Sync, MedicationReminder, TaskReminder) |
| `di/BillingModule.kt` | BillingRepository + PremiumFeatureGuard + No-Op フォールバック |
| `di/BillingAvailability.kt` | Google Play Billing 利用可否チェック |
| `di/WidgetEntryPoint.kt` | Glance Widget DI (EntryPointAccessors) |
| `di/FirebaseAvailability.kt` | Firebase 利用可否チェック |

### ナビゲーション

`ui/navigation/Screen.kt` の sealed class でルート定義:
- **Auth**: Login, Register, ForgotPassword
- **BottomNav**: Home, Medication, Calendar, Tasks, HealthRecords, Notes（6タブ）
- **Secondary**: AddMedication, EditMedication, MedicationDetail, AddNote, EditNote, AddHealthRecord, EditHealthRecord, AddCalendarEvent, EditCalendarEvent, AddTask, EditTask, EmergencyContacts, AddEmergencyContact, EditEmergencyContact, CareRecipientProfile, Timeline, PrivacyPolicy, TermsOfService, Search, Settings, OnboardingWelcome, MemberManagement, SendInvitation, AcceptInvitation
- `ui/navigation/CareNoteNavHost.kt` でルーティング管理
- `ui/navigation/AdaptiveNavigationScaffold.kt` — ウィンドウサイズに応じて Compact=Bottom, Medium=Rail, Expanded=Drawer を自動選択

### エラーハンドリング

- `domain/common/Result.kt` — 独自の `Result<T, E>` sealed class（kotlin.Result ではない）
- `domain/common/DomainError.kt` — 6 種の sealed class (Database, NotFound, Validation, Network, Unauthorized, Unknown)
- DomainError は **Throwable ではない**。Timber に渡す際は `Timber.w("msg: $error")` と文字列化

### 同期パターン（Firestore）

- `domain/common/SyncResult.kt` — 同期結果 (Success, PartialSuccess, Failure)
- `domain/common/SyncState.kt` — 同期状態 (Idle, Syncing, Success, Error)
- **競合解決**: Last-Write-Wins (LWW) — `updatedAt` 比較で新しい方を採用

## パッケージ構成

```
app/src/main/java/com/carenote/app/
├── config/              AppConfig（全設定値の一元管理。マジックナンバー禁止）
├── data/
│   ├── export/          HealthRecord/MedicationLog/Task/Note の CsvExporter + PdfExporter（計 8 ファイル）
│   ├── local/           Room (DB, DAO, Entity, Converter, Migration) + ImageCompressor, DatabasePassphraseManager, DatabaseRecoveryHelper
│   ├── mapper/          Entity ↔ Domain マッパー
│   │   └── remote/      Firestore ↔ Domain マッパー (RemoteMapper)
│   ├── remote/
│   │   └── model/       SyncMetadata（同期メタデータ）
│   ├── repository/      Repository 実装 (Medication, Note, HealthRecord, Calendar, Task, CareRecipient, EmergencyContact, Photo, Settings, Timeline, Search, NoteComment, ActiveCareRecipientProvider, FirebaseStorage, NoOpStorage, FirebaseAnalytics, NoOpAnalytics, Member, Invitation, Billing, NoOpBilling, PremiumFeatureGuard)
│   │   └── sync/        EntitySyncer + ConfigDrivenEntitySyncer + MedicationLogSyncer + NoteCommentSyncer
│   ├── service/         CareNoteMessagingService (FCM)
│   └── worker/          SyncWorker, MedicationReminderWorker, TaskReminderWorker
├── di/                  Hilt モジュール (App, Database, Firebase, Sync, Worker, Billing) + WidgetEntryPoint, FirebaseAvailability, BillingAvailability
├── domain/
│   ├── common/          Result<T,E>, DomainError, SyncResult, SyncState
│   ├── model/           ドメインモデル (25 model: Medication, MedicationLog, Note, NoteComment, HealthRecord, CalendarEvent, CalendarEventType, Task, CareRecipient, EmergencyContact, Photo, User, UserSettings, TimelineItem, ThemeMode, TaskPriority, RecurrenceFrequency, RelationshipType, AppLanguage, SearchResult, Member, Invitation, PremiumStatus, ProductInfo, BillingConnectionState)
│   ├── repository/      Repository インターフェース (30: Medication, MedicationLog, Note, NoteComment, HealthRecord, CalendarEvent, Task, CareRecipient, EmergencyContact, Photo, Auth, Sync, Storage, Settings, Timeline, Analytics, Search, Member, Invitation, Billing, PremiumFeatureGuard + ActiveCareRecipientProvider + Scheduler/Exporter/Compressor interfaces)
│   └── util/            Clock interface + SystemClock（テスト用時刻制御）+ RecurrenceExpander
└── ui/
    ├── common/          共通 UI ユーティリティ
    ├── components/      再利用可能コンポーネント (CareNoteCard, CareNoteTextField, CareNoteDatePickerDialog, CareNoteTimePickerDialog, ConfirmDialog, EmptyState, ErrorDisplay, LoadingIndicator, PhotoPickerSection, SwipeToDismissItem, CareNoteAddEditScaffold)
    ├── navigation/      Screen sealed class + CareNoteNavHost + AdaptiveNavigationScaffold
    ├── preview/         PreviewAnnotations, PreviewData
    ├── screens/         各画面 (Screen.kt)
    │   ├── auth/        LoginScreen, RegisterScreen, ForgotPasswordScreen
    │   ├── calendar/    CalendarScreen + AddEditCalendarEventScreen + components/
    │   ├── carerecipient/  CareRecipientProfileScreen
    │   ├── emergencycontact/  EmergencyContactsScreen, AddEmergencyContactScreen, EditEmergencyContactScreen
    │   ├── healthrecords/ HealthRecordsScreen + AddEditHealthRecordScreen + HealthMetricsParser
    │   ├── home/        HomeScreen + HomeViewModel
    │   ├── medication/  MedicationScreen + AddEditMedicationScreen + MedicationDetailScreen + components/
    │   ├── notes/       NotesScreen + AddEditNoteScreen
    │   ├── onboarding/  OnboardingWelcomeScreen
    │   ├── search/      SearchScreen + SearchViewModel
    │   ├── member/      MemberManagementScreen + SendInvitationScreen + AcceptInvitationScreen + ViewModels
    │   ├── settings/    SettingsScreen + dialogs/ (SettingsDialogs, DataExportDialog), sections/ (各セクション + DataExportSection + MemberManagementSection)
    │   ├── tasks/       TasksScreen + AddEditTaskScreen
    │   └── timeline/    TimelineScreen
    ├── testing/         TestTags
    ├── theme/           Material3 テーマ（Color, Type, Theme）
    ├── util/            NotificationHelper, CrashlyticsTree, BiometricHelper, RootDetector, LocaleManager, SnackbarController, FormValidator, DateTimeFormatters, AssetReader
    ├── viewmodel/       ViewModel 群 + PhotoManager（写真状態管理）+ ExportState
    └── widget/          CareNoteWidget, CareNoteWidgetReceiver (Glance)
```

## Firebase 統合

### Firebase Auth（認証）

- `AuthRepository` — 認証インターフェース (signIn, signUp, signOut, etc.)
- `FirebaseAuthRepositoryImpl` — Firebase Auth 実装
- `currentUser: Flow<User?>` で認証状態を監視
- FirebaseAuthException → DomainError マッピング

### Cloud Firestore（データ同期）

- **構造**: `careRecipients/{id}/medications/{id}` のサブコレクション構造
- **同期**: Room ↔ Firestore 双方向同期
- **ID マッピング**: `sync_mappings` テーブルで Room ID ↔ Firestore ID を管理

### EntitySyncer パターン

```kotlin
// 基底クラス: data/repository/sync/EntitySyncer.kt
abstract class EntitySyncer<Entity, Domain> {
    abstract val entityType: String
    abstract fun collectionPath(careRecipientId: String): String

    // テンプレートメソッド
    suspend fun sync(careRecipientId: String, lastSyncTime: LocalDateTime?): SyncResult {
        val pushResult = pushLocalChanges(...)
        val pullResult = pullRemoteChanges(...)
        return mergeResults(pushResult, pullResult)
    }
}
```

各 Syncer: `MedicationSyncer`, `MedicationLogSyncer`, `NoteSyncer`, `NoteCommentSyncer`, `HealthRecordSyncer`, `CalendarEventSyncer`, `TaskSyncer`

### RemoteMapper パターン

```kotlin
// インターフェース: data/mapper/remote/RemoteMapper.kt
interface RemoteMapper<Domain> {
    fun toDomain(data: Map<String, Any?>): Domain
    fun toRemote(domain: Domain, syncMetadata: SyncMetadata?): Map<String, Any?>
    fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata
}
```

### FCM（プッシュ通知）

- `CareNoteMessagingService` — FirebaseMessagingService 実装
- `NotificationHelper` — 通知チャンネル管理 + 通知表示

### Crashlytics

- `CrashlyticsTree` — Timber Tree 実装
- WARN 以上のログを Crashlytics に送信
- 例外は `recordException()` で自動記録

### Firebase Storage（写真保存）

- `StorageRepository` — ストレージインターフェース (upload, download, delete)
- `FirebaseStorageRepositoryImpl` — Firebase Storage 実装
- `NoOpStorageRepository` — Firebase 未初期化時のフォールバック（グレースフルデグラデーション）

### Firebase Analytics（使用状況分析）

- `AnalyticsRepository` — Analytics インターフェース (logScreenView, logEvent)
- `FirebaseAnalyticsRepositoryImpl` — Firebase Analytics 実装
- `NoOpAnalyticsRepository` — Firebase 未初期化時のフォールバック
- **自動画面トラッキング**: MainActivity の `NavController.OnDestinationChangedListener` で全画面遷移を自動記録
- **イベント定数**: `AppConfig.Analytics` に 40+ イベント定数（Auth, Medication, Calendar, Task, HealthRecord, Note, EmergencyContact, CareRecipient, Sync）

### Firebase グレースフルデグラデーション

`google-services.json` 未配置時や Firebase 未初期化時でもアプリがクラッシュしない仕組み。

- `FirebaseAvailability.check()` — Firebase 利用可否チェック。`Exception`（`IllegalStateException` だけでなく `RuntimeException` も含む）をキャッチ
- **No-Op 実装**: `NoOpAuthRepository`, `NoOpSyncRepository`, `NoOpSyncWorkScheduler`, `NoOpStorageRepository`, `NoOpAnalyticsRepository`
- `dagger.Lazy<T>` で Firebase 依存の遅延初期化。`FirebaseAvailability` の結果に応じて本番 or No-Op を DI で注入

## Worker パターン

### SyncWorker（定期同期）

```kotlin
@HiltWorker
class SyncWorker : CoroutineWorker {
    // 1. 認証確認
    // 2. careRecipientId 取得
    // 3. syncRepository.syncAll() 実行
    // 4. 結果に応じて Result.success/retry/failure
}
```

- 定期実行: 15分間隔（WorkManager 最小値）
- 制約: NetworkType.CONNECTED
- リトライ: NetworkError → 可能, UnauthorizedError → 不可

### MedicationReminderWorker（服薬リマインダー）

- 指定時刻に通知を発行
- おやすみ時間（quietHours）チェック
- ユーザー設定で通知オン/オフ
- 服薬済みチェック（TAKEN ログあればスキップ）+ フォローアップ再通知

### TaskReminderWorker（タスクリマインダー）

- 指定時刻にタスクリマインダー通知を発行
- おやすみ時間（quietHours）チェック
- ユーザー設定で通知オン/オフ

## テーマ

- **ライトテーマ**（温かみのあるクリーム背景 #FFF8F0）
- **プライマリカラー**: グリーン系（信頼感 #2E7D32）
- **フォントサイズ**: bodyLarge 18sp（高齢者向け大きめ）
- **最小タッチターゲット**: 48dp
- **Dynamic Color (Material You)**: Android 12+ で `dynamicLightColorScheme()`/`dynamicDarkColorScheme()` を条件分岐。Settings で切替可能。CareNoteColors は Dynamic Color 時も独自ブランドカラー維持

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

Firebase 関連:
- `FakeAuthRepository` — 認証状態のテスト制御
- `FakeSyncRepository` — 同期状態のテスト制御
- `FakeSyncWorkScheduler` — WorkManager 依存排除
- `FakeStorageRepository` — Firebase Storage 依存排除
- `FakeAnalyticsRepository` — Analytics イベント記録のテスト検証

データ関連:
- `FakeMedicationRepository`, `FakeMedicationLogRepository`, `FakeNoteRepository`, `FakeHealthRecordRepository`, `FakeCalendarEventRepository`, `FakeTaskRepository`
- `FakeCareRecipientRepository`, `FakeEmergencyContactRepository`, `FakePhotoRepository`, `FakeSettingsRepository`, `FakeTimelineRepository`, `FakeSearchRepository`
- `FakeMedicationReminderScheduler`, `FakeTaskReminderScheduler`
- `FakeMedicationLogCsvExporter`, `FakeMedicationLogPdfExporter`, `FakeNoteCsvExporter`, `FakeNotePdfExporter`, `FakeTaskCsvExporter`, `FakeTaskPdfExporter`
- `FakeNotificationHelper`, `FakeRootDetector`, `FakeSyncMappingDao`, `FakeClock`, `FakeNoteCommentRepository`, `FakeActiveCareRecipientProvider`
- `FakeMemberRepository`, `FakeInvitationRepository`, `FakeBillingRepository`, `FakePremiumFeatureGuard`

### E2E テスト

`androidTest/.../di/TestFirebaseModule.kt` で本番モジュールを Fake に置換。

18 テストファイル（`androidTest/.../e2e/`）:
- **基盤**: `E2eTestBase`, `E2eTestUtils`
- **画面別**: `AuthFlowTest`, `MedicationFlowTest`, `CalendarFlowTest`, `TasksFlowTest`, `HealthRecordsFlowTest`, `NotesFlowTest`, `NavigationFlowTest`, `MemberInvitationFlowTest`, `AcceptInvitationFlowTest`
- **横断**: `CriticalPathFlowTest`, `EditFlowTest`, `DeleteFlowTest`, `ValidationFlowTest`, `ExportFlowTest`, `PhotoSectionFlowTest`, `SyncFlowTest`

## コード規約

### ログ

**Timber 必須**。`println()`, `Log.d()`, `Log.e()` 等は禁止。

**PII ログ禁止**: UID, email, 個人名等をログに含めない。
```kotlin
// NG
Timber.d("User signed in: ${user.uid}")

// OK
Timber.d("User signed in successfully")
```

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
2. **Room Entity 変更時** — Migration ファイル作成 + `DatabaseModule.kt` への登録が必須（v14 を baseline として squash 済み。v1-v13 の migration は削除済み。新規 migration は v14 以降から作成する。未リリースのため `fallbackToDestructiveMigration()` を使用中）
3. **strings.xml は JP/EN ペア更新** — 片方だけ更新すると実行時に英語/日本語が混在
4. **DomainError は Throwable ではない** — `Timber.w(error, msg)` は使えない。`Timber.w("msg: $error")` と書く
5. **Result は独自実装** — `domain/common/Result.kt` の `Result<T, E>`。kotlin.Result ではない
6. **Windows 環境** — `./gradlew.bat` を使用。パス区切りは `\`
7. **ProGuard (release)** — 新ライブラリ追加時は `app/proguard-rules.pro` の keep ルール確認
8. **Zero Detekt tolerance** — maxIssues=0, all issues must be fixed
9. **google-services.json** — Firebase 設定ファイル。`.gitignore` 済み。`docs/FIREBASE_SETUP.md` 参照
10. **PII ログ禁止** — UID, email, 個人名をログに含めない（L-2 セキュリティ要件）
11. **WorkManager 最小間隔** — 定期実行は最短 15分。それ未満は設定しても 15分になる
12. **Firebase 例外処理** — FirebaseAuthException は DomainError にマッピングして返す
13. **Firebase グレースフルデグラデーション** — `google-services.json` 未配置時は No-Op 実装を使用。`FirebaseAvailability.check()` は `Exception`（`IllegalStateException` だけでなく `RuntimeException` も含む）をキャッチ
14. **Screen sealed class の companion object** — `val bottomNavItems get() = listOf(...)` (computed property) を使う。`val bottomNavItems = listOf(...)` は JVM data object 初期化順序で NPE
15. **Paging 3 テスト** — `cachedIn(viewModelScope)` は `UncompletedCoroutinesError` を発生させるため、ViewModel テストでは Repository 直接検証パターンを採用
16. **Glance Widget DI** — 標準 `@Inject` 不可。`WidgetEntryPoint` + `EntryPointAccessors.fromApplication()` を使用
17. **Adaptive Navigation** — `AdaptiveNavigationScaffold` がウィンドウサイズに応じて Bottom/Rail/Drawer を自動選択。BottomBar をハードコードしない
18. **Root 検出ダイアログ** — MainActivity で `RootDetector` がルート検出時に「続ける/終了」AlertDialog を表示。テストでは `FakeRootDetector` で制御
19. **リリース前チェックリスト** — `docs/RELEASE_CHECKLIST.md` を確認。署名、ProGuard、Firebase 設定、ストア掲載情報等の最終確認事項
20. **エクスポート PII 注意** — CSV/PDF エクスポートに患者情報を含む。キャッシュクリア、ログ PII 禁止ルール遵守
21. **Worker Bash approval spam** — 実装 Worker（worker-impl, worker-test）に Bash を許可すると、ビルド/テスト実行の approval プロンプトが大量発生する。Bash は worker-quality のみに許可し、ビルド/テスト実行を集約する
22. **テスト開発 Best Practices** — (a) Syncer テスト: EntitySyncerTest + TestEntitySyncer パターンを踏襲。MedicationLogSyncer は独自実装（サブコレクション）のため専用テスト必須。他の Syncer は ConfigDrivenEntitySyncer（SyncerConfigTest でカバー）。(b) PagingSource テスト: ViewModel の cachedIn(viewModelScope) は UncompletedCoroutinesError を発生させるため、DAO 層で直接テスト推奨。(c) FakeRepository エラーテスト: shouldFail フラグで DatabaseError 返却。ViewModel エラーハンドリングは snackbar パターンで統一。(d) TestDataFixtures.NOW / NOW_STRING でハードコード日時を統一

## 今後の追加予定

- Wear OS 対応
- FCM リモート通知（Cloud Functions バックエンド必要）
