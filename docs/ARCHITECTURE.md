# CareNote Architecture Reference

> CLAUDE.md から移動した詳細参照情報。必要時に `Read` で参照する。
> CLAUDE.md と同じコミットで更新すること。

## DI モジュール

| モジュール | 責務 |
|-----------|------|
| `di/AppModule.kt` | 15 Repository + 8 Exporter + Clock/Compressor/RootDetector/PremiumFeatureGuard バインディング |
| `di/DatabaseModule.kt` | Room DB + DAO (14 テーブル) + PassphraseManager + RecoveryHelper |
| `di/FirebaseModule.kt` | FirebaseAuth, Firestore, Messaging, Storage, Analytics + AuthRepository + AnalyticsRepository + No-Op フォールバック |
| `di/SyncModule.kt` | SyncRepository + EntitySyncer 群 |
| `di/WorkerModule.kt` | WorkManager + 4 Scheduler (Sync, MedicationReminder, TaskReminder, CalendarEventReminder) |
| `di/BillingModule.kt` | BillingRepository + PremiumFeatureGuard + No-Op フォールバック |
| `di/BillingAvailability.kt` | Google Play Billing 利用可否チェック |
| `di/WidgetEntryPoint.kt` | Glance Widget DI (EntryPointAccessors) |
| `di/FirebaseAvailability.kt` | Firebase 利用可否チェック |

## ナビゲーション

`ui/navigation/Screen.kt` の sealed class でルート定義:
- **Auth**: Login, Register, ForgotPassword
- **BottomNav**: Home, Medication, Calendar, Tasks, HealthRecords, Notes（6タブ）
- **Secondary**: AddMedication, EditMedication, MedicationDetail, AddNote, EditNote, AddHealthRecord, EditHealthRecord, AddCalendarEvent, EditCalendarEvent, AddTask, EditTask, EmergencyContacts, AddEmergencyContact, EditEmergencyContact, CareRecipientProfile, Timeline, PrivacyPolicy, TermsOfService, Search, Settings, OnboardingWelcome, MemberManagement, SendInvitation, AcceptInvitation
- `ui/navigation/CareNoteNavHost.kt` でルーティング管理
- `ui/navigation/AdaptiveNavigationScaffold.kt` — ウィンドウサイズに応じて Compact=Bottom, Medium=Rail, Expanded=Drawer を自動選択

## 同期パターン（Firestore）

- `domain/common/SyncResult.kt` — 同期結果 (Success, PartialSuccess, Failure)
- `domain/common/SyncState.kt` — 同期状態 (Idle, Syncing, Success, Error)
- **競合解決**: Last-Write-Wins (LWW) — `updatedAt` 比較で新しい方を採用

## パッケージ構成（詳細）

~~~
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
~~~

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

~~~kotlin
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
~~~

各 Syncer: `MedicationSyncer`, `MedicationLogSyncer`, `NoteSyncer`, `NoteCommentSyncer`, `HealthRecordSyncer`, `CalendarEventSyncer`, `TaskSyncer`

### RemoteMapper パターン

~~~kotlin
// インターフェース: data/mapper/remote/RemoteMapper.kt
interface RemoteMapper<Domain> {
    fun toDomain(data: Map<String, Any?>): Domain
    fun toRemote(domain: Domain, syncMetadata: SyncMetadata?): Map<String, Any?>
    fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata
}
~~~

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

~~~kotlin
@HiltWorker
class SyncWorker : CoroutineWorker {
    // 1. 認証確認
    // 2. careRecipientId 取得
    // 3. syncRepository.syncAll() 実行
    // 4. 結果に応じて Result.success/retry/failure
}
~~~

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

### CalendarEventReminderWorker（カレンダーイベントリマインダー）

- 指定時刻にカレンダーイベント（非タスク）のリマインダー通知を発行
- おやすみ時間（quietHours）チェック
- ユーザー設定で通知オン/オフ
- PremiumFeatureGuard なし（カレンダーイベントは無制限）
- スキップ条件: イベント未存在、isTask=true、reminderEnabled=false

## テーマ

- **ライトテーマ**（温かみのあるクリーム背景 #FFF8F0）
- **プライマリカラー**: グリーン系（信頼感 #2E7D32）
- **フォントサイズ**: bodyLarge 18sp（高齢者向け大きめ）
- **最小タッチターゲット**: 48dp
- **Dynamic Color (Material You)**: Android 12+ で `dynamicLightColorScheme()`/`dynamicDarkColorScheme()` を条件分岐。Settings で切替可能。CareNoteColors は Dynamic Color 時も独自ブランドカラー維持

## テスト詳細

### Fake Repository 一覧

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

### E2E テスト一覧

18 テストファイル（`androidTest/.../e2e/`）:
- **基盤**: `E2eTestBase`, `E2eTestUtils`
- **画面別**: `AuthFlowTest`, `MedicationFlowTest`, `CalendarFlowTest`, `TasksFlowTest`, `HealthRecordsFlowTest`, `NotesFlowTest`, `NavigationFlowTest`, `MemberInvitationFlowTest`, `AcceptInvitationFlowTest`
- **横断**: `CriticalPathFlowTest`, `EditFlowTest`, `DeleteFlowTest`, `ValidationFlowTest`, `ExportFlowTest`, `PhotoSectionFlowTest`, `SyncFlowTest`

## よくある落とし穴（詳細）

CLAUDE.md の落とし穴リストから移動した、特定機能に触る時に参照する項目:

9. **google-services.json** — Firebase 設定ファイル。`.gitignore` 済み。`docs/FIREBASE_SETUP.md` 参照
11. **WorkManager 最小間隔** — 定期実行は最短 15分。それ未満は設定しても 15分になる
12. **Firebase 例外処理** — FirebaseAuthException は DomainError にマッピングして返す
13. **Firebase グレースフルデグラデーション** — `google-services.json` 未配置時は No-Op 実装を使用。`FirebaseAvailability.check()` は `Exception`（`IllegalStateException` だけでなく `RuntimeException` も含む）をキャッチ
14. **Screen sealed class の companion object** — `val bottomNavItems get() = listOf(...)` (computed property) を使う。`val bottomNavItems = listOf(...)` は JVM data object 初期化順序で NPE
15. **Paging 3 テスト** — `cachedIn(viewModelScope)` は `UncompletedCoroutinesError` を発生させるため、ViewModel テストでは Repository 直接検証パターンを採用
16. **Glance Widget DI** — 標準 `@Inject` 不可。`WidgetEntryPoint` + `EntryPointAccessors.fromApplication()` を使用
17. **Adaptive Navigation** — `AdaptiveNavigationScaffold` がウィンドウサイズに応じて Bottom/Rail/Drawer を自動選択。BottomBar をハードコードしない
18. **Root 検出ダイアログ** — MainActivity で `RootDetector` がルート検出時に「続ける/終了」AlertDialog を表示。テストでは `FakeRootDetector` で制御
19. **リリース前チェックリスト** — `docs/RELEASE_CHECKLIST.md` を確認。署名、ProGuard、Firebase 設定、ストア掲載情報等の最終確認事項
22. **テスト開発 Best Practices** — (a) Syncer テスト: EntitySyncerTest + TestEntitySyncer パターンを踏襲。MedicationLogSyncer は独自実装（サブコレクション）のため専用テスト必須。他の Syncer は ConfigDrivenEntitySyncer（SyncerConfigTest でカバー）。(b) PagingSource テスト: ViewModel の cachedIn(viewModelScope) は UncompletedCoroutinesError を発生させるため、DAO 層で直接テスト推奨。(c) FakeRepository エラーテスト: shouldFail フラグで DatabaseError 返却。ViewModel エラーハンドリングは snackbar パターンで統一。(d) TestDataFixtures.NOW / NOW_STRING でハードコード日時を統一

## 今後の追加予定

- Wear OS 対応
- FCM リモート通知（Cloud Functions バックエンド必要）
