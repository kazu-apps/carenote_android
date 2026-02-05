# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: Phase 10 実装完了

Phase 10（服薬ログ per-timing 修正）を実装・ビルド・テスト完了。

## 次のアクション

1. `/task-driver` で Phase 11 を実行（服薬リマインダー接続 + 未服薬チェック）
2. `/task-driver` で Phase 12 を実行（タスクリマインダーシステム構築）

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (carenote.app.support@gmail.com) — リリース前に確認
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | BugHunt | `MedicationViewModel.todayLogs` が `LocalDate.now()` を VM 作成時に固定 → 深夜に古い表示 |
| MEDIUM | BugHunt | `MedicationLogSyncer.collectionPath()` が `UnsupportedOperationException` を投げる設計 → 基底 `sync()` 誤呼び出しでクラッシュ |
| MEDIUM | BugHunt | `NotificationHelper` の `medicationId.toInt()` — Long→Int オーバーフロー可能性 |
| MEDIUM | BugHunt | `readAssetText()` が Compose composition 中（メインスレッド）でファイル I/O 実行 |
| MEDIUM | BugHunt | `SettingsViewModel.isSyncing` の `LiveData.asFlow()` がライフサイクル外で購読 |
| ~~MEDIUM~~ | ~~BugHunt~~ | ~~`startDestination` が Compose state で動的変更 → NavHost 再構築リスク~~ → **Phase 9 で修正済み** |
| MEDIUM | M-5 | Room スキーマ JSON がコミット済み（プライベートリポジトリでは許容） |
| MEDIUM | Item 30 | ValidationUtils.kt が未使用のデッドコード（本番インポートなし） |
| MEDIUM | Item 32 | JaCoCo `**/util/*` 除外が広範囲（テストは存在） |
| MEDIUM | Item 31 | テスト品質: Mapper ラウンドトリップ不完全、Repository Turbine 未使用、ViewModel Loading→Success テスト欠落 |
| LOW | BugHunt | `DatabasePassphraseManager` — EncryptedPrefs 破損時にパスフレーズ消失 → DB 再作成（データロス） |
| LOW | BugHunt | `AddMedicationViewModel.savedEvent` の `SharedFlow(replay=1)` が設定変更時にリプレイ |
| LOW | L-4 | 全 DAO が OnConflictStrategy.REPLACE 使用（マルチデバイス同期リスク） |
| LOW | Item 99 | FCM リモート通知の受信処理が未実装（`onMessageReceived` がログのみ） |
| LOW | Item 99 | FCM トークンのサーバー送信が未実装（`onNewToken` がログのみ） |
| LOW | Item 100 | 個別 Screen ファイルの UI ハードコード値が未置換（共有コンポーネントのみ完了） |
| INFO | — | 削除確認ダイアログが UI から到達不可（スワイプ削除の準備） |
| INFO | — | Flow `.catch` が欠落（Room Flow は安定、低リスク） |

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

### Phase 11: 服薬リマインダー接続 + 未服薬チェック (HIGH) - PENDING
`MedicationReminderScheduler` は実装済みだが未接続（孤立コード）。飲むまで通知し続ける機能が未実装。
**修正方針**: (A) `AddMedicationViewModel.saveMedication()` で `scheduler.scheduleAllReminders()` を呼ぶ。(B) `MedicationReminderWorker` で服薬済みチェック（`MedicationLogDao` から当日ログを照会し、TAKEN なら通知スキップ）。(C) 未服薬時のフォローアップリマインダー（N分後に再通知）。(D) 服薬記録時にフォローアップをキャンセル。
- 対象: `AddMedicationViewModel.kt`, `MedicationReminderWorker.kt`, `MedicationReminderScheduler.kt`, `MedicationViewModel.kt`
- 依存: Phase 10（timing フィールドが必要）

### Phase 12: タスクリマインダーシステム構築 (MEDIUM) - PENDING
タスクの繰り返し通知機能が完全未実装。
**修正方針**: (A) `Task` モデルに `reminderEnabled: Boolean` + `recurrence: TaskRecurrence?` 追加。(B) `TaskReminderWorker` 新規作成（完了チェック付き）。(C) `TaskReminderScheduler` 新規作成。(D) `NotificationHelper` にタスク通知チャンネル追加。(E) `AddEditTaskViewModel` で scheduler 接続。(F) Room migration で Task テーブルにカラム追加。
- 対象: `Task.kt`, `TaskEntity.kt`, 新規 Worker/Scheduler, `NotificationHelper.kt`, `AddEditTaskViewModel.kt`, DB migration
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
| Room DB | v8, SQLCipher 4.6.1 暗号化, sync_mappings テーブル, medication_logs.timing カラム追加 |
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

## スコープ外 / 将来

- **v3.0**: Cloud Storage（写真保存）, Google Play Billing（プレミアムサブスクリプション）
- **手動**: スクリーンショット、フィーチャーグラフィック、プライバシーポリシー Web ホスティング
- **スキップ**: LegalDocumentScreen テスト（純粋な表示、ロジックなし）
