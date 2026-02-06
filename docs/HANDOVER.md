# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: Phase 23 完了

Phase 23: DB インデックス追加 + 依存関係アップグレード。
`medications` テーブルに `name` インデックス、`tasks` テーブルに `(is_completed, created_at)` 複合インデックスを追加（Room migration v9→v10）。依存関係を最新安定版に更新（Compose BOM 2025.01.01, Espresso 3.6.1, Test Runner 1.6.2, Test Rules/Core 1.6.1）。

## 次のアクション

1. `/task-driver` で Phase 22 から順に実行
2. 各フェーズ完了後にビルド・テスト確認
3. リリース前に実機テスト + APK 検証

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (carenote.app.support@gmail.com) — リリース前に確認
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| ~~MEDIUM~~ | ~~BugHunt~~ | ~~`MedicationViewModel.todayLogs` が `LocalDate.now()` を VM 作成時に固定 → 深夜に古い表示~~ → **Phase 13 で修正済み** |
| ~~MEDIUM~~ | ~~BugHunt~~ | ~~`MedicationLogSyncer.collectionPath()` の UnsupportedOperationException 設計~~ → **リサーチで呼出し 0 件確認、KDoc 追加済み、現状維持** |
| ~~MEDIUM~~ | ~~BugHunt~~ | ~~`NotificationHelper` の `medicationId.toInt()` — Long→Int オーバーフロー可能性~~ → **Phase 13 で修正済み** |
| ~~MEDIUM~~ | ~~BugHunt~~ | ~~`readAssetText()` が Compose composition 中（メインスレッド）でファイル I/O 実行~~ → **Phase 13 で修正済み** |
| ~~MEDIUM~~ | ~~BugHunt~~ | ~~`SettingsViewModel.isSyncing` の `LiveData.asFlow()` がライフサイクル外で購読~~ → **調査の結果、WhileSubscribed で正常動作** |
| ~~MEDIUM~~ | ~~BugHunt~~ | ~~`startDestination` が Compose state で動的変更 → NavHost 再構築リスク~~ → **Phase 9 で修正済み** |
| ~~MEDIUM~~ | ~~M-5~~ | ~~Room スキーマ JSON がコミット済み~~ → **リサーチで公式推奨プラクティスと確認、機密情報なし、現状維持** |
| ~~MEDIUM~~ | ~~Item 30~~ | ~~ValidationUtils.kt が未使用のデッドコード~~ → **Phase 14 で削除済み** |
| ~~MEDIUM~~ | ~~Item 32~~ | ~~JaCoCo `**/util/*` 除外が広範囲~~ → **Phase 14 で具体化済み** |
| ~~MEDIUM~~ | ~~Item 31~~ | ~~テスト品質: Repository Turbine 未使用(6件)、ViewModel Loading→Success 遷移テスト PARTIAL(7件)、UserMapper テスト欠落~~ → **Phase 16 で修正済み** |
| ~~LOW~~ | ~~BugHunt~~ | ~~`DatabasePassphraseManager` — EncryptedPrefs 破損~~ → **リサーチで低頻度+Firestore同期でリカバリ可と確認、v3.0スコープ** |
| ~~LOW~~ | ~~BugHunt~~ | ~~`savedEvent` の `SharedFlow(replay=1)` が設定変更時にリプレイ~~ → **Phase 14 で Channel に変更済み** |
| ~~LOW~~ | ~~L-4~~ | ~~全 DAO が OnConflictStrategy.REPLACE~~ → **リサーチで現在の同期フローでは重複防止済みと確認、CASCADE外部キーなし、現状維持** |
| ~~LOW~~ | ~~Item 99~~ | ~~FCM リモート通知の受信処理が未実装~~ → **リサーチでバックエンド未存在を確認、v3.0スコープ** |
| ~~LOW~~ | ~~Item 99~~ | ~~FCM トークンのサーバー送信が未実装~~ → **同上、v3.0スコープ** |
| ~~LOW~~ | ~~Item 100~~ | ~~Screen ファイルの UI ハードコード値~~ → **リサーチで Color/sp は移行済み、.dp は Compose 標準プラクティスと確認、現状維持** |
| ~~INFO~~ | ~~—~~ | ~~削除確認ダイアログが UI から到達不可（5リスト画面）~~ → **Phase 17 で接続済み** |
| ~~INFO~~ | ~~—~~ | ~~Flow `.catch` が欠落（全 ViewModel 13箇所、SQLCipher 使用でリスク増）~~ → **Phase 15 で修正済み** |
| ~~MEDIUM~~ | ~~v2.3 リサーチ~~ | ~~全 AddEdit 画面に BackHandler / 未保存確認ダイアログがない~~ → **Phase 18 で修正済み** |
| ~~MEDIUM~~ | ~~v2.3 リサーチ~~ | ~~全リスト画面の ErrorDisplay に onRetry=null（リトライ不可）~~ → **Phase 19 で修正済み** |
| ~~MEDIUM~~ | ~~v2.3 リサーチ~~ | ~~通知タップで該当画面に遷移しない（PendingIntent 未設定）~~ → **Phase 20 で修正済み** |
| ~~MEDIUM~~ | ~~v2.3 リサーチ~~ | ~~`AuthViewModel._authSuccessEvent` が SharedFlow(replay=1)（他VMと不統一）~~ → **Phase 21 で修正済み** |
| ~~LOW~~ | ~~v2.3 リサーチ~~ | ~~DatePicker/TimePicker が3箇所に重複~~ → **Phase 21 で共通化済み** |
| ~~LOW~~ | ~~v2.3 リサーチ~~ | ~~@Preview アノテーションが全画面で未定義~~ → **Phase 22 で修正済み** |
| ~~LOW~~ | ~~v2.3 リサーチ~~ | ~~medications テーブルにインデックスなし、tasks に複合インデックスなし~~ → **Phase 23 で修正済み** |
| ~~LOW~~ | ~~v2.3 リサーチ~~ | ~~依存関係が約1年古い (Compose BOM 2024.12, Espresso 3.5, Test Runner 1.5)~~ → **Phase 23 で更新済み** |
| LOW | v2.3 リサーチ | 生体認証ロックなし（個人健康情報保護） → **Phase 24 で対応** |

## ロードマップ

### Phase 1: SQLCipher パスフレーズ消失時のグレースフルリカバリ - DONE
`DatabaseRecoveryHelper` を追加。`Room.databaseBuilder().build()` 前にパスフレーズ検証を行い、不一致なら DB ファイルを削除して新規作成。
- 新規: `DatabaseRecoveryHelper.kt`, `DatabaseRecoveryHelperTest.kt` (4テスト)
- 変更: `DatabaseModule.kt` に `recoveryHelper.recoverIfNeeded()` 呼び出し追加

### Phase 2: アプリ内言語切り替え機能（日本語/英語/システム） - DONE
`AppLanguage` enum + `LanguageSection` + Per-App Language API。10テスト追加。

### Phase 3: Per-App Language API クラッシュ修正 (CRITICAL) - DONE
`MainActivity` を `AppCompatActivity` に変更、テーマ親を `Theme.Material3.Light.NoActionBar` に移行、`AppLocalesMetadataHolderService` をマニフェストに追加、`com.google.android.material:material:1.12.0` 依存追加。
- 変更: `MainActivity.kt`, `themes.xml`, `AndroidManifest.xml`, `libs.versions.toml`, `build.gradle.kts`

### Phase 4: Firebase DI クラッシュ修正 (CRITICAL) - DONE
`EntitySyncer` / `ConfigDrivenEntitySyncer` / `MedicationLogSyncer` の `FirebaseFirestore` を `dagger.Lazy<FirebaseFirestore>` に変更し、Firestore アクセスを同期実行時まで遅延。
`FirebaseModule` の `provideFirebaseFirestore()` / `provideFirebaseMessaging()` に availability ガードを追加。
`SyncModule` の 5 Syncer provider から `firestore.get()` の eager 呼出しを削除。テスト 3 ファイルも `DaggerLazy` に対応。
- 変更: `EntitySyncer.kt`, `ConfigDrivenEntitySyncer.kt`, `MedicationLogSyncer.kt`, `SyncModule.kt`, `FirebaseModule.kt`, `EntitySyncerTest.kt`, `TestEntitySyncer.kt`, `SyncerConfigTest.kt`

### Phase 5: CrashlyticsTree 例外キャッチ + Auth FormHandler スコープリーク修正 (HIGH) - DONE
(A) `CrashlyticsTree.log()` の catch を `IllegalStateException` → `Exception` に拡大。
(B) 3 FormHandler のスコープをコンストラクタ注入に変更。`AuthViewModel` が `viewModelScope` を渡す形に。テスト 3 ファイルも `TestScope` に対応。
- 変更: `CrashlyticsTree.kt`, `LoginFormHandler.kt`, `RegisterFormHandler.kt`, `ForgotPasswordFormHandler.kt`, `AuthViewModel.kt`, テスト 3 ファイル

### Phase 6: collectAsState → collectAsStateWithLifecycle 統一 (MEDIUM) - DONE
14 Screen ファイル + UiState.kt KDoc の `collectAsState()` を `collectAsStateWithLifecycle()` に置換。
バックグラウンド時の不要な Flow 購読を停止し、リソース消費を削減。
- 変更: MedicationScreen, MedicationDetailScreen, AddMedicationScreen, CalendarScreen, AddEditCalendarEventScreen, TasksScreen, AddEditTaskScreen, HealthRecordsScreen, AddEditHealthRecordScreen, NotesScreen, AddEditNoteScreen, LoginScreen, RegisterScreen, ForgotPasswordScreen, UiState.kt

### Phase 7: 言語切替クラッシュ修正 (CRITICAL) - DONE
`LaunchedEffect` 内の `setApplicationLocales()` を削除し、`SettingsViewModel.updateAppLanguage()` の `onSuccess` コールバックから `LocaleManager.applyLanguage()` を呼ぶ形に変更。Compose 描画中の Activity 再生成を回避。
- 新規: `LocaleManager.kt`（`setApplicationLocales` の1箇所集約）
- 変更: `SettingsViewModel.kt`（onSuccess で LocaleManager 呼出し）、`MainActivity.kt`（LaunchedEffect 削除 + 不要 import 削除）
- テスト: `SettingsViewModelTest.kt` に mockkObject テスト 2件追加

### Phase 8: 設定をボトムナビタブに移動 (MEDIUM) - DONE
`bottomNavItems` に `Settings` を追加（6番目タブ）。`MedicationScreen` の歯車アイコンと `onNavigateToSettings` を削除。`SettingsScreen` の戻る矢印と `onNavigateBack` を削除。`CareNoteNavHost` のナビゲーションラムダを更新。
- 変更: `Screen.kt`, `MedicationScreen.kt`, `SettingsScreen.kt`, `CareNoteNavHost.kt`

### Phase 9: アプリ再起動クラッシュ修正 (CRITICAL) - DONE
`startDestination` を `remember` でラップし初回 composition 時に1回だけ評価するよう修正。`LaunchedEffect(isLoggedIn)` に `currentDestination` null ガードを追加。Auth 状態変化はプログラマティックナビゲーションで安全に処理。
- 変更: `MainActivity.kt`（2箇所のみ）

### Phase 10: 服薬ログ per-timing 修正 (CRITICAL) - DONE
`MedicationLog` / `MedicationLogEntity` に `timing: MedicationTiming?` フィールドを追加。Room migration v7→v8。`todayLogs` のキーを `Pair<Long, String?>` に変更し、タイミングごとに独立した服薬記録を実現。
- 変更: `MedicationLog.kt`, `MedicationLogEntity.kt`, `Migrations.kt`, `CareNoteDatabase.kt`, `MedicationLogMapper.kt`, `MedicationLogRemoteMapper.kt`, `MedicationViewModel.kt`, `MedicationScreen.kt`
- テスト: `MigrationsTest.kt`, `MedicationLogMapperTest.kt`, `MedicationLogRemoteMapperTest.kt`, `MedicationViewModelTest.kt` に timing テスト追加

### Phase 11: 服薬リマインダー接続 + 未服薬チェック (HIGH) - DONE
`MedicationReminderSchedulerInterface` を抽出し、ViewModel に接続。Worker に服薬済みチェック（TAKEN ログあればスキップ）とフォローアップ再通知を追加。削除時・服薬記録時にリマインダー/フォローアップをキャンセル。
- 新規: `MedicationReminderSchedulerInterface.kt`, `FakeMedicationReminderScheduler.kt`
- 変更: `AppConfig.kt`（3定数追加）, `MedicationLogDao.kt`（hasTakenLogForDateRange）, `MedicationLogRepository.kt`/`Impl`（hasLogForMedicationToday）, `MedicationReminderScheduler.kt`（interface実装+followUp）, `MedicationReminderWorker.kt`（taken-check+followUp）, `WorkerModule.kt`（provider追加）, `AddMedicationViewModel.kt`/`MedicationViewModel.kt`/`MedicationDetailViewModel.kt`（scheduler接続）
- テスト: 3 ViewModel テストに新規 8 テスト追加（schedule/cancel/follow-up 検証）

### Phase 12: タスク繰り返し + 時間帯指定リマインダー (HIGH) - DONE
(a) ドメインモデル + DB migration v8→v9: `Task`/`TaskEntity` に recurrence/reminder 4フィールド追加、`RecurrenceFrequency` enum 新規作成
(b) Worker + Scheduler: `TaskReminderWorker`, `TaskReminderScheduler`, `TaskReminderSchedulerInterface` 追加、`NotificationHelper` にタスク通知チャンネル追加
(c) UI + ViewModel: `AddEditTaskScreen` に繰り返しセクション（FilterChip + 間隔入力）とリマインダーセクション（Switch + TimePicker）追加。`AddEditTaskViewModel` に `TaskReminderSchedulerInterface` 注入、保存時にスケジュール/キャンセル。`TasksViewModel` に完了時の次回タスク自動生成、リマインダーキャンセル/再スケジュール追加。`calculateNextDueDate()` companion関数で DAILY/WEEKLY/MONTHLY 計算。テスト: AddEditTaskViewModelTest に13新規テスト、TasksViewModelTest に10新規テスト追加（全テスト PASS）。
- 変更: `strings.xml` (JP/EN), `AddEditTaskViewModel.kt`, `AddEditTaskScreen.kt`, `TasksViewModel.kt`, `AddEditTaskViewModelTest.kt`, `TasksViewModelTest.kt`

### Phase 13: ランタイムバグ修正 (MEDIUM) - DONE
MEDIUM バグ 3 件修正: (a) `MedicationViewModel.todayLogs` を `_currentDate` flatMapLatest + `LifecycleResumeEffect` で日付変更検知、(b) `NotificationHelper.safeIntId()` で Long→Int 安全変換（下位31ビット）、(c) `CareNoteNavHost` の `readAssetText` を `produceState` + `Dispatchers.IO` で非同期化。テスト 6 件追加。
- 変更: `MedicationViewModel.kt`, `MedicationScreen.kt`, `NotificationHelper.kt`, `CareNoteNavHost.kt`, `MedicationViewModelTest.kt`, `NotificationHelperTest.kt`

### Phase 14: デッドコード削除 + イベント修正 + コード品質 (MEDIUM) - DONE
ValidationUtils 削除、savedEvent/deletedEvent を Channel+receiveAsFlow に変更（6 VM）、MedicationLogSyncer KDoc 追加、JaCoCo 除外具体化。

### Phase 15: Flow .catch 追加（全 ViewModel 防御的エラーハンドリング） (MEDIUM) - DONE
全 8 ViewModel、13 箇所の `.stateIn()` パイプラインに `.catch` を追加。
- UiState Flow (7箇所): `.catch { emit(UiState.Error(DomainError.DatabaseError(...))) }`
- 非 UiState Flow (6箇所): `.catch { Timber.w(...); emit(フォールバック値) }`
- MedicationDetailViewModel の FQN `com.carenote.app.domain.common.DomainError` を import に統一
- ビルド成功、全テスト PASS

### Phase 16: テスト品質強化（Repository Turbine + ViewModel 遷移 + UserMapper） (MEDIUM) - DONE
テスト品質ギャップ 3 件を修正。
(a) 6 RepositoryImpl テストを `.first()` → Turbine `.test { awaitItem() }` に移行（Medication, MedicationLog, Note, HealthRecord, CalendarEvent, Task）
(b) 6 ViewModel テストに Loading→Success シーケンシャル遷移テストを追加（Medication, Notes, Calendar, HealthRecords, Tasks, MedicationDetail）
(c) `UserMapperTest.kt` を新規作成（5 テストケース: 正常マッピング、null displayName/email/metadata、isEmailVerified）
- 変更: 6 RepositoryImpl テスト + 6 ViewModel テスト
- 新規: `UserMapperTest.kt`
- ビルド成功、全テスト PASS

### Phase 17: リスト画面の削除ダイアログ接続（スワイプ削除） (LOW) - DONE
5 リスト画面に `SwipeToDismissBox` ベースのスワイプ削除を接続。共通 `SwipeToDismissItem<T>` コンポーネントを作成し、各画面の `items` ブロック内 Card を `SwipeToDismissItem` でラップ。
- 新規: `ui/components/SwipeToDismissItem.kt`（汎用スワイプ削除ラッパー）
- 変更: `MedicationScreen.kt`（MedicationList に onDelete 追加、items 2箇所ラップ）、`NotesScreen.kt`（NoteCard ラップ）、`HealthRecordsScreen.kt`（HealthRecordListContent に onDelete 追加）、`CalendarScreen.kt`（CalendarEventCard ラップ）、`TasksScreen.kt`（TaskCard ラップ）
- ビルド成功、全テスト PASS

### Phase 18: 未保存データ保護（BackHandler + isDirty） (HIGH) - DONE
全5つの AddEdit 画面に「保存せずに戻る？」確認ダイアログを追加。各 ViewModel に `isDirty` computed property を追加し、FormState の data class 比較でフォーム変更を検知。`BackHandler` でシステムバックもインターセプト。
- 変更: `strings.xml` (JP/EN, 4文字列追加), 5 ViewModel (`isDirty` + `_initialFormState`), 5 Screen (`BackHandler` + `ConfirmDialog` + `handleBack`), 5 ViewModel テスト (各3-6テスト追加)
- ビルド成功、全テスト PASS

### Phase 19: エラーリトライ + Pull-to-Refresh (MEDIUM) - DONE
(a) 全5 ViewModel に `_refreshTrigger`, `_isRefreshing`, `refresh()` 追加。`flatMapLatest` / `combine` で Flow 再収集をトリガー。`onEach` で `isRefreshing` をリセット。
(b) 全5リスト画面の `ErrorDisplay(onRetry = null)` を `onRetry = { viewModel.refresh() }` に接続。
(c) 全5リスト画面に `PullToRefreshBox` を追加。Success/Empty コンテンツをラップし、スワイプダウンでデータ再読み込み。
- 変更: 5 ViewModel, 5 Screen, 5 ViewModel テスト (各2テスト追加: refresh triggers reload, isRefreshing lifecycle)
- ビルド成功、全テスト PASS

### Phase 20: 通知タップナビゲーション（PendingIntent） (MEDIUM) - DONE
Navigation Compose Deep Links を使用し、通知タップで該当画面に直接遷移。`carenote://` カスタム URI スキームで MedicationDetail / EditTask にルーティング。
- 変更: `AppConfig.kt`（DEEP_LINK_SCHEME 定数追加）, `NotificationHelper.kt`（deep link URI ビルダー + Intent ACTION_VIEW 設定）, `CareNoteNavHost.kt`（navDeepLink 追加 2箇所）, `MainActivity.kt`（addOnNewIntentListener で起動中 deep link 処理）, `AndroidManifest.xml`（carenote:// intent-filter 追加）
- テスト: `NotificationHelperTest.kt` に deep link URI 生成テスト 4件追加
- ビルド成功、全テスト PASS

### Phase 21: DatePicker/TimePicker 共通化 + AuthEvent Channel 修正 (MEDIUM) - DONE
(a) `CareNoteDatePickerDialog.kt`, `CareNoteTimePickerDialog.kt` を `ui/components/` に新規作成。`AddEditTaskScreen`, `AddEditCalendarEventScreen` の private ダイアログを削除し共通コンポーネントに置換。`SettingsDialogs.kt` の import を変更。旧 `TimePickerPreference.kt` を削除。
(b) `AuthViewModel._authSuccessEvent` を `MutableSharedFlow(replay=1)` → `Channel(Channel.BUFFERED)` + `receiveAsFlow()` に修正。`LoginFormHandler` / `RegisterFormHandler` のパラメータと emit/send も対応。テスト修正済み。
- 新規: `ui/components/CareNoteDatePickerDialog.kt`, `ui/components/CareNoteTimePickerDialog.kt`
- 変更: `AddEditTaskScreen.kt`, `AddEditCalendarEventScreen.kt`, `SettingsDialogs.kt`, `AuthViewModel.kt`, `LoginFormHandler.kt`, `RegisterFormHandler.kt`
- 削除: `ui/screens/settings/components/TimePickerPreference.kt`
- テスト: `LoginFormHandlerTest.kt`, `RegisterFormHandlerTest.kt` を Channel 対応に更新
- ビルド成功、全テスト PASS

### Phase 22: Compose Preview 追加 (LOW) - DONE
全13画面の主要 Composable に `@Preview` アノテーションを追加。`@LightDarkPreview` MultiPreview カスタムアノテーション（Light/Dark 2バリアント）+ `PreviewData` サンプルデータ object を新規作成。LoginScreen / RegisterScreen は `LoginContent` / `RegisterContent` を抽出して stateless 化し Preview 対応。
- 新規: `ui/preview/PreviewAnnotations.kt`（`@LightDarkPreview`）, `ui/preview/PreviewData.kt`（全ドメインモデル + FormState サンプル）
- 変更: Auth 3画面（LoginScreen, RegisterScreen, ForgotPasswordScreen）, リスト 5画面（MedicationScreen, CalendarScreen, TasksScreen, HealthRecordsScreen, NotesScreen）, AddEdit 5画面（AddMedicationScreen, AddEditCalendarEventScreen, AddEditTaskScreen, AddEditHealthRecordScreen, AddEditNoteScreen）
- ビルド成功、全テスト PASS

### Phase 23: DB インデックス追加 + 依存関係アップグレード (LOW) - DONE
(a) `medications` テーブルに `name` インデックス、`tasks` テーブルに `(is_completed, created_at)` 複合インデックスを追加（Room migration v9→v10）。
(b) `libs.versions.toml` の依存関係を更新: Compose BOM 2024.12.01→2025.01.01, Espresso 3.5.1→3.6.1, Test Runner 1.5.2→1.6.2, Test Rules 1.5.0→1.6.1, Test Core 1.5.0→1.6.1。
- 変更: `MedicationEntity.kt`（@Index 追加）, `TaskEntity.kt`（複合インデックス追加）, `Migrations.kt`（MIGRATION_9_10 追加）, `CareNoteDatabase.kt`（v10）, `libs.versions.toml`, `MigrationsTest.kt`（4テスト追加/更新）
- ビルド成功、全テスト PASS

### Phase 24: 生体認証ロック（BiometricPrompt） (LOW) - PENDING
アプリ起動時・バックグラウンド復帰時に `BiometricPrompt` で認証。設定画面でオン/オフ切替。介護記録（個人健康情報）の保護を強化。`androidx.biometric:biometric` 依存追加。
- 対象: 新規 `BiometricHelper.kt`, `MainActivity.kt`, `SettingsScreen.kt`, `SettingsViewModel.kt`, `SettingsDataSource.kt`
- 依存: なし

---

## 完了タスク

| Item | 概要 | Status |
|------|------|--------|
| 1-4 | Clean Architecture + 服薬管理 + メモ | DONE |
| 5 | 健康記録（domain/data/VM/UI/チャート） | DONE |
| 6 | カレンダー（月表示 + イベント管理） | DONE |
| 7 | タスク管理（フィルター + 完了トグル） | DONE |
| 8-17 | リリース準備（設定、移行、アイコン、スプラッシュ、A11y、E2E、署名、ProGuard、法的文書、ストア） | DONE |
| 18-21 | 品質改善（バリデーション i18n、A11y WCAG AA、法的アセット、ダークモード） | DONE |
| 22-32 | コードレビュー（基盤、DB、全機能、テスト、ビルド） | DONE |
| 33-38 | テスト強化（Fake shouldFail、StandardTestDispatcher、VM 拡張、E2E インフラ） | DONE |
| 39-45 | コード品質（AppConfig 集約、Snackbar/バリデーション i18n、Screen 分割、リネーム） | DONE |
| 46-53 | セキュリティ修正（SQLCipher、EncryptedPrefs、backup除外、networkConfig、PII削除） | DONE |
| 55-61 | v2.0 Phase 1-2: Firebase Auth（SDK、認証 UI、ナビゲーション、テスト） | DONE |
| 62-68 | v2.0 Phase 3: Firestore 同期（スキーマ、Mapper、Syncer、Worker、UI、テスト） | DONE |
| 69-72 | v2.0 Phase 4: FCM（通知チャンネル、服薬リマインダー、テスト） | DONE |
| 73-78 | v2.0 Phase 5: Crashlytics + ProGuard + E2E + コードレビュー + CLAUDE.md | DONE |
| 79-81 | v2.1 セキュリティ強化（PII マスク、メール検証、パスワード 8文字） | DONE |
| 82-84 | v2.2 Phase 1: レガシー保護テスト（RemoteMapper 150、EntitySyncer 20、SyncRepo 20） | DONE |
| 85-88 | v2.2 Phase 2: Syncer TDD（SyncerConfig + ConfigDrivenEntitySyncer、5 Syncer 削除、~335行削減） | DONE |
| 89-92 | v2.2 Phase 3a: SettingsScreen TDD 分割（462→193行、SettingsDialogState sealed class） | DONE |
| 93-94 | v2.2 Phase 3b: AuthViewModel TDD 分割（316→89行、AuthValidators + 3 FormHandler） | DONE |
| 95-96 | v2.2 Phase 3c: SettingsViewModel TDD 整理（205→147行、updateSetting() 汎用化） | DONE |
| 97-100 | v2.2 Phase 4: コード品質（キー定数整理、Premium 削除、TODO 整理、UI 定数 AppConfig 移行） | DONE |
| 101 | Mapper 統合 ADR 作成（Option C: 統合しない を採択、`docs/ADR-002-UNIFIED-MAPPER.md`） | DONE |
| 102 | Mapper 統合実装 — ADR-002 により不要と判定（スキップ） | SKIP |

## セキュリティレビュー結果

| カテゴリ | ステータス | 詳細 |
|---------|----------|------|
| CRITICAL | 0 | 重大な脆弱性なし |
| HIGH | 0 | H-1 PII ログ (Item 79 完了), H-2 メール検証 (Item 80 完了) |
| MEDIUM | 2 | Rate Limiting, ProGuard |
| LOW | 1 | FCM トークン管理（サーバー側実装待ち） |
| **全体リスクレベル** | **LOW** | HIGH 問題は全て対応完了 |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v10, SQLCipher 4.6.1 暗号化, sync_mappings テーブル, medication_logs.timing, tasks.recurrence/reminder カラム追加, medications.name インデックス, tasks(is_completed,created_at) 複合インデックス |
| DB キー保存 | EncryptedSharedPreferences (Android Keystore AES256_GCM) |
| 設定保存 | EncryptedSharedPreferences (`carenote_settings_prefs`) |
| バックアップ除外 | DB, DB パスフレーズ prefs, 設定 prefs |
| Firebase | BOM 33.7.0 (Auth, Firestore, Messaging, Crashlytics, Analytics) |
| Firebase プラグイン | google-services.json 存在時のみ適用（条件付き） |
| 同期パターン | ConfigDrivenEntitySyncer + SyncerConfig（MedicationLogSyncer のみカスタム） |
| Worker | SyncWorker (15分定期), MedicationReminderWorker (指定時刻) |
| E2E テスト | TestFirebaseModule + TestDatabaseModule で Fake 実装に置換 |
| エラー i18n | `UiText.Resource` / `UiText.ResourceWithArgs` sealed class |
| Snackbar i18n | `SnackbarEvent` sealed interface (WithResId / WithString) |
| テーマカラー | `CareNoteColors.current.xxxColor`（ハードコード Color() 禁止） |
| 定数 | `AppConfig` オブジェクト（マジックナンバー禁止、UI 定数含む） |
| Mapper 設計 | Local/Remote 分離維持（ADR-002 で統合しない判定） |
| Enum パース | try-catch + フォールバック（NoteMapper, HealthRecordMapper, TaskMapper） |
| テストパターン | StandardTestDispatcher + Turbine + FakeRepository (MutableStateFlow) |
| Robolectric | 4.14.1（Android SDK シャドウ、Compose UI Test） |
| BugHunt 2026-02-06 | Agent Teams リサーチ: collectAsState 残存=0、strings.xml 不整合=0、isSyncing=正常。実バグ: todayLogs 日付固定, Long→Int, メインスレッド I/O |
| v2.3 改善リサーチ 2026-02-06 | Agent Teams 3並列調査: コード品質=良好（800行超0, TODO 0, デッドコード0）、リスク=LOW（Coroutine安全, メモリリーク0, ProGuard完備）、UXギャップ=BackHandler未実装(5画面), onRetry=null(5画面), PullToRefresh無, 通知PendingIntent無, DatePicker重複3箇所, @Preview 0件, DBインデックス不足(medications,tasks) |

## スコープ外 / 将来

- **v3.0**: Cloud Storage（写真保存）, Google Play Billing（プレミアムサブスクリプション）
- **v3.0**: FCM リモート通知実装（バックエンド構築と合わせて）, Firestore リストアフロー
- **v3.0**: Adaptive Layout（タブレット対応）, Dynamic Color オプション, PagingSource（大量データ対応）
- **v3.0**: Baseline Profile / App Startup Library, Incremental Sync（updatedAt フィルター）
- **v3.0**: 服薬編集画面（EditMedication）, 検索機能追加（服薬/タスク/カレンダー/健康記録）
- **v3.0**: Root 検出, Certificate Pinning, material-icons-extended 最適化
- **手動**: スクリーンショット、フィーチャーグラフィック、プライバシーポリシー Web ホスティング
- **手動**: Play Console メタデータ（データ安全性フォーム、コンテンツレーティング、ストア説明文）
- **スキップ**: LegalDocumentScreen テスト（純粋な表示、ロジックなし）
