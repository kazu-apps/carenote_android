# HANDOVER.md - CareNote Android

## セッションステータス: Phase 1 CalendarEvent 拡張完了

## 現在のタスク: Task→CalendarEvent 統合 + デイリータイムライン — ロードマップ策定完了

Phase 1-3 完了。Task→CalendarEvent 統合の Expert 議論完了、5 フェーズのロードマップを策定。

## 次のアクション

1. `/exec` で Phase 2 (Data+DI 層統合) を実行開始

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (`support@carenote.app`) — リリース前に実アドレス確定必要
- リリース APK の実機テスト未実施
- TaskReminderWorker PII ログ違反（`title=$taskTitle`）— Phase 4 で修正予定
- `fallbackToDestructiveMigration` リリース前に無効化 + 適切な Migration 作成必要

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | v4.0 | Rate Limiting 未実装（API エンドポイント、バックエンド依存） |
| LOW | v2.0 | FCM トークンのサーバー送信未実装（バックエンド前提） |
| LOW | v10.0-tdd | SettingsViewModelTest 1170 行（Detekt 対象外だが将来的に分割検討） |
| LOW | Detekt | Roborazzi スクリーンショット Windows/Linux フォントレンダリング差分（CI soft-fail 対応済み） |
| LOW | Detekt | SwipeToDismissItem deprecated API 警告（将来的な対応推奨） |
| INFO | Detekt | Kotlin コンパイラ annotation-default-target 警告、テストコード型チェック警告（機能影響なし） |

## ロードマップ

### Phase 1: リマインダー通知バグ修正 (calculateDelay + Clock injection) - DONE
`calculateDelay()` に `plusDays(1)` ロジック追加 + Clock injection で決定的テスト実現。TaskReminderSchedulerTest 13件 + MedicationReminderSchedulerTest 14件、全テスト pass、ビルド成功。

### Phase 2: タブレット DatePicker/TimePicker バグ修正 - DONE
4箇所の TextButton → Text + `Modifier.clickable(role = Role.Button)` + `padding(12.dp)` に変更。i18n 4文字列追加（JP+EN）。DueDateSelectorTest 9件 + DateTimeSelectorTest 8件 = 17テスト新規。ビルド・全テスト pass。

### Phase 3: GPP アップグレード + セキュリティ修正 - DONE
GPP 3.10.1 → 4.0.0 アップグレード + android.newDsl=false 削除 + api-key.json を .gitignore 追加。ビルド成功、publish タスク確認。

### Expert 議論サマリー (2026-02-17)

参加: debugger, security（固定）, tester — 各2-3ラウンドの peer-to-peer 議論完了

**全 Expert 合意（11項目）:**
1. Clock injection 必須（既存インフラ活用、追加コストほぼゼロ）
2. plusDays(1) 戦略（セキュリティリスクなし）
3. delay = 0 → 翌日スケジュール（isAfter の自然な挙動維持）
4. 防御的ガードを `delay <= 0` に変更して残す
5. 2層テスト（calculateDelay 直接 + scheduleReminder 統合）計 11 テストケース
6. delay 安全性 assert（0 < delay <= 86,400,000）
7. ExistingWorkPolicy.REPLACE の verify
8. semantics Role.Button 必須 + onClickLabel 推奨
9. DueDateSelector 内側 Row のみ clickable（削除ボタン衝突回避）
10. Compose UI テスト + Roborazzi + 手動タブレット検証
11. NavigationDrawer 干渉説は棄却（コード確認で干渉メカニズムなし）

**未合意（実装時に判断）:**
- Phase 2 の真因確定は実機検証待ち（TextButton が最有力、改善しなければ別途調査）
- Scheduler コード共通化は将来のリファクタリング候補
- PII ログ修正（`TaskReminderWorker` の `title=$taskTitle`）はオプション（DEBUG レベル、低リスク）

### Expert 議論サマリー: GPP アップグレード (2026-02-17)

参加: researcher, security（固定）, critic — 2ラウンドの peer-to-peer 議論完了

**全 Expert 合意:**
1. 修正方針: GPP 3.10.1 → 4.0.0 + `android.newDsl=false` 削除（選択肢 b）
2. `api-key.json` の `.gitignore` 未登録は CRITICAL — 即時対応必須
3. テスト: `assembleDebug` + `tasks --group publishing` で publish タスク確認
4. ProGuard への影響: なし（GPP はビルドプラグイン、実行時クラスなし）
5. AGP 10.0（2026年後半）で `android.newDsl=false` 完全削除のため今対応が合理的

**未合意（実装時に判断）:**
- フェーズ分割: security は Phase 0 + Phase 1 分離推奨、critic は単一コミットで十分
- `play {}` API 完全互換の断言: critic は留保付きを推奨（ビルド実行で最終確認）

### Expert 議論サマリー: Task→CalendarEvent 統合 (2026-02-17)

参加: architect, security（固定）, quality — 各2-3ラウンドの peer-to-peer 議論完了

**全 Expert 合意（GO 判定）:**
1. 案A（完全統合）を全員一致で推奨 — 未リリース + fallbackToDestructiveMigration でリスク最小
2. date non-null 維持 — 期限なしタスクは `createdAt.toLocalDate()` をデフォルト設定
3. `CalendarEvent.isTask` computed property 追加（`type == CalendarEventType.TASK`）
4. `CalendarEvent.validate()` 拡張関数でタイプ別フィールドバリデーション
5. Exporter の type フィルタは non-nullable 必須パラメータ（secure by default）
6. TaskReminderScheduler 名称維持 + 二重 type チェック（ViewModel + Scheduler）
7. 5 フェーズ構成（Investigation の 6 → 5 に統合。Phase 3 DI を Phase 2 Data に吸収）
8. AddEditCalendarEventScreen の Detekt LargeClass 対策: TaskFields.kt 分離必須
9. テスト影響修正: 削除 24 artifacts、拡張 7 ファイル +40 テストケース、E2E 5 ファイル
10. scope creep 防止: Timeline のフィルタ/FAB は後続タスク
11. TaskReminderWorker の PII ログ違反修正（`title=$taskTitle` 除去）

**未合意（実装時に判断）:**
- TaskReminderScheduler の命名変更（architect: 現名維持、quality: EventTaskReminderScheduler 推奨）
- Firestore Security Rules 更新タイミング（未デプロイなら不要、デプロイ済みなら Phase 2 で対応）

### Phase 1: Domain 層統合 — CalendarEvent モデル拡張 + Task 廃止 - DONE
CalendarEvent に 4 フィールド追加（priority, reminderEnabled, reminderTime, createdBy）。CalendarEventType.TASK 追加。isTask computed property + validate() 拡張関数。CalendarEventRepository に 5 メソッド追加。CalendarEventEntity/DAO/Mapper/RemoteMapper/RepositoryImpl 拡張。DB v23→v24。テスト 20 件追加。Task 関連ファイル未変更。

### Phase 2: Data+DI 層統合 — Entity/DAO/Syncer/DI 統合 - PENDING
CalendarEventEntity 拡張（+4カラム + インデックス）。CalendarEventDao に TASK フィルタクエリ追加。TaskEntity/TaskDao/TaskMapper/TaskSyncer 削除。SyncModule の TaskSyncer config 削除。DatabaseModule/AppModule の Task バインディング削除。DB version 23→24。
- 対象ファイル:
  - `data/local/entity/CalendarEventEntity.kt`
  - `data/local/dao/CalendarEventDao.kt`
  - `data/local/entity/TaskEntity.kt` (削除)
  - `data/local/dao/TaskDao.kt` (削除)
  - `data/repository/CalendarEventRepositoryImpl.kt`
  - `data/repository/TaskRepositoryImpl.kt` (削除)
  - `data/repository/sync/TaskSyncer.kt` (削除)
  - `data/mapper/remote/TaskRemoteMapper.kt` (削除)
  - `data/local/CareNoteDatabase.kt` (version 24)
  - `di/DatabaseModule.kt`
  - `di/AppModule.kt`
  - テスト: FakeCalendarEventRepository 拡張（+5メソッド）、FakeTask* 4ファイル削除、DAO テスト追加
- 依存: Phase 1
- 品質ゲート: `testDebugUnitTest` 全パス + Detekt 0 issues
- 信頼度: HIGH

### Phase 3: UI 層統合 — Screen/ViewModel 統合 + BottomNav 変更 - PENDING
AddEditCalendarEventScreen/ViewModel に TASK フィールド追加（TaskFields.kt 分離で Detekt 準拠）。TasksScreen/AddEditTaskScreen/ViewModel 削除。BottomNav の Tasks→Timeline 置換。TimelineScreen を BottomNav 対応に改修（戻るボタン削除のみ）。HomeViewModel の TaskRepository→CalendarEventRepository 変更。
- 対象ファイル:
  - `ui/screens/calendar/AddEditCalendarEventScreen.kt`
  - `ui/screens/calendar/components/TaskFields.kt` (新規)
  - `ui/screens/tasks/` (全削除)
  - `ui/screens/timeline/TimelineScreen.kt`
  - `ui/navigation/Screen.kt`
  - `ui/navigation/CareNoteNavHost.kt`
  - `ui/viewmodel/TasksViewModel.kt` (削除)
  - `ui/viewmodel/AddEditTaskViewModel.kt` (削除)
  - `ui/viewmodel/AddEditCalendarEventViewModel.kt`
  - `ui/screens/home/HomeViewModel.kt`
  - テスト: AddEditCalendarEventViewModelTest +18、HomeViewModelTest 改修、TasksViewModelTest/AddEditTaskViewModelTest 削除
- 依存: Phase 2
- 品質ゲート: `testDebugUnitTest` 全パス + `recordRoborazziDebug` 成功
- 信頼度: HIGH

### Phase 4: Worker/Exporter 統合 - PENDING
TaskReminderWorker を CalendarEventRepository 依存に変更 + type==TASK 防御チェック + PII ログ修正。TaskCsvExporter/TaskPdfExporter を CalendarEvent Exporter に統合（exportByType 必須パラメータ）。
- 対象ファイル:
  - `data/worker/TaskReminderWorker.kt`
  - `data/export/TaskCsvExporter.kt` (削除/統合)
  - `data/export/TaskPdfExporter.kt` (削除/統合)
  - `di/WorkerModule.kt`
  - テスト: Worker テスト修正、Exporter テスト修正
- 依存: Phase 3
- 品質ゲート: `testDebugUnitTest` 全パス
- 信頼度: HIGH

### Phase 5: E2E テスト + カバレッジ最終調整 - PENDING
TasksFlowTest 削除/書き直し。EditFlowTest/DeleteFlowTest/ValidationFlowTest の Task テスト書き直し。NavigationFlowTest の Tab 名変更。TestBuilders の aTask()→aCalendarEvent() 拡張。jacocoTestCoverageVerification 80% 確認。
- 対象ファイル:
  - `androidTest/.../e2e/TasksFlowTest.kt` (削除/書き直し)
  - `androidTest/.../e2e/EditFlowTest.kt`
  - `androidTest/.../e2e/DeleteFlowTest.kt`
  - `androidTest/.../e2e/ValidationFlowTest.kt`
  - `androidTest/.../e2e/NavigationFlowTest.kt`
  - `test/.../util/TestBuilders.kt`
  - テスト: E2E 全体動作確認 + カバレッジ 80% 確認
- 依存: Phase 4
- 品質ゲート: `jacocoTestCoverageVerification` (80% LINE) パス
- 信頼度: MEDIUM

## PENDING 項目

### Phase 1B: Billing サーバーサイド検証 (Cloud Functions) - PENDING
Google Play Developer API 経由のレシート検証を Cloud Functions で実装。本番リリース前の必須要件。
- 種別: 実装
- 対象: Cloud Functions (Node.js), Firestore の purchaseTokens コレクション
- 依存: v9.0 Phase 1 完了済み
- 注意: **Claude Code の守備範囲外**。Firebase CLI + Node.js 環境が必要

## 完了タスク

| Item | 概要 | Status |
|------|------|--------|
| v1.0 1-53 | Clean Architecture + 5機能 + リリース準備 + 品質改善 + テスト強化 | DONE |
| v2.0 55-81 | Firebase Auth + Firestore 同期 + FCM + Crashlytics + セキュリティ強化 | DONE |
| v2.2 82-102 | TDD リファクタリング（Syncer, Settings, Auth, コード品質） | DONE |
| v3.0 Ph1-35 | バグ修正 + リマインダー + 依存更新 + Dynamic Color | DONE |
| v4.0 Ph1-25 | CI/CD + Paging 3 + Roborazzi + Widget + Root 検出 | DONE |
| v5.0 Ph1-6 | TDD リファクタリング（Clock, HealthMetricsParser, Scaffold, FormValidator, PhotoManager） | DONE |
| v6.0 Ph1-5 | Root ダイアログ + E2E 拡充 + Firebase Analytics + パフォーマンス + CLAUDE.md | DONE |
| v7.0 Ph1-6 | ProGuard + エクスポート（CSV/PDF）+ クロスモジュール検索 + Roborazzi + CLAUDE.md | DONE |
| v8.0 Ph1-3 | ホーム画面 + CareRecipient 拡張 + CalendarEvent type/completed | DONE |
| v8.1 Ph4-7 | recipientId + createdBy + オンボーディング + NoteComment + recurrence | DONE |
| v9.0-sec Ph1-3 | Firestore Rules + Session timeout + PBKDF2 + domain/validator/ + APPI ドキュメント | DONE |
| v9.0-test Ph1-3 | TestDataFixtures + TestBuilders + ResultMatchers + MedicationLogSyncerTest | DONE |
| v10.0-tdd Ph1-4 | MainCoroutineRule + ViewModel テスト移行 + ResultMatchers 全面採用 | DONE |
| v9.0 Ph1-2 | Billing 基盤 + PremiumFeatureGuard + 通知制限 | DONE |
| v9.0 Ph3-4 | Firestore 構造移行 + Member/Invitation データモデル + DB v23 | DONE |
| v9.0 Ph5-6 | 招待 UI + フロー + E2E テスト（MemberInvitation 8件 + AcceptInvitation 5件） | DONE |
| CLAUDE.md | v9.0 Phase 1-6 反映（DB v23, 14テーブル, 25モデル, 30リポジトリ, 18 E2E） | DONE |
| Detekt 全修正 | 367 issues → 0。87 ファイル変更。AppModule 分割 (→RepositoryModule + ExporterModule)、CsvUtils 抽出、50+ 画面ヘルパー分割 | DONE |
| CI グリーン化 | Detekt 1.23.7、workflow_dispatch、screenshot soft-fail、PR #4 マージ済み | DONE |
| Phase 1 | リマインダー通知バグ修正: calculateDelay + Clock injection + plusDays(1)。TaskReminderSchedulerTest 13件 + MedicationReminderSchedulerTest 14件 新規 | DONE |
| Phase 2 | タブレット DatePicker/TimePicker バグ修正: TextButton → clickable 化 + i18n + Compose UI テスト 17件 | DONE |
| Phase 3 | GPP 4.0.0 アップグレード + api-key.json セキュリティ修正 | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v24, SQLCipher 4.6.1, fallbackToDestructiveMigration, 14 Entity |
| Firebase | BOM 34.8.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics) + No-Op フォールバック |
| Billing | Google Play Billing 7.1.1, BillingAvailability + NoOpBillingRepository パターン |
| DI 分割 | AppModule + RepositoryModule + ExporterModule + DatabaseModule + FirebaseModule + SyncModule + WorkerModule + BillingModule |
| セキュリティ | SQLCipher + EncryptedPrefs + Root検出 + 生体認証 + PBKDF2 + Session timeout + domain/validator/ |
| テスト基盤 | MainCoroutineRule + TestBuilders (11モデル) + ResultMatchers (13種) + TestDataFixtures |
| エクスポート | HealthRecord/MedicationLog/Task/Note CSV/PDF + CsvUtils 共通ヘルパー |
| Detekt | 1.23.7, maxIssues=0, Compose FunctionNaming 除外, LongParameterList functionThreshold=8 |

## スコープ外 / 将来

- **FCM リモート通知**: Cloud Functions / バックエンド構築が前提
- **Wear OS 対応**: Horologist + Health Services、別モジュール必要
- **CSV データインポート**: 対象ユーザー適合性検証後
- **Timeline フィルタ/FAB/タスク完了トグル**: Task→CalendarEvent 統合後の後続タスク
