# HANDOVER.md - CareNote Android

## セッションステータス: Phase 5 完了（E2E テスト修正 + Task 残存コード削除）

## 現在のタスク: Task→CalendarEvent 統合 — Phase 5 完了（全フェーズ完了）

Phase 4 完了。Task モデル・TaskRepository・CalendarEventTaskAdapter を完全削除。全プロダクションコード・テストコードから旧 Task 参照を除去。SearchResult.TaskResult を削除し CalendarEventResult に統一。PreviewData.kt の Task → CalendarEvent 移行。assembleDebug ビルド成功、testDebugUnitTest 全1897テスト合格。

## 次のアクション

1. Detekt 実行で静的解析 0 issues 確認
2. E2E テスト手動実行（エミュレータ必要）: `./gradlew.bat connectedDebugAndroidTest`

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (`support@carenote.app`) — リリース前に実アドレス確定必要
- リリース APK の実機テスト未実施
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

### Phase 2: Data+DI 層統合 — CalendarEventTaskAdapter Bridge + Task 削除 - DONE
CalendarEventTaskAdapter で TaskRepository→CalendarEventRepository Bridge 実装。TaskEntity/TaskDao/TaskMapper/TaskRemoteMapper/TaskRepositoryImpl 削除。SyncModule の taskSyncer 削除（ENTITY_TYPE_COUNT 7→6）。CareNoteDatabase v24→v25（TaskEntity 除去）。CalendarEventTaskAdapterTest 19件新規 + FirestoreSyncRepositoryImplTest 修正。全1985テスト合格。Detekt 0 issues。

### Phase 3: UI 層統合 — Screen/ViewModel 統合 + BottomNav 変更 - DONE
AddEditCalendarEventScreen/ViewModel に Task フィールド統合（TaskFields.kt 分離で Detekt 準拠）。TasksScreen/AddEditTaskScreen/ViewModel 全削除。BottomNav Tasks→Timeline 置換。HomeViewModel TaskRepository→CalendarEventRepository 変更。テスト 13 件追加。PreviewData.kt の AddEditTaskFormState 参照除去。DueDateSelectorTest を calendar パッケージに移動。AddEditCalendarEventViewModel の eventId null 安全対応。Detekt LongMethod/MaxLineLength 4 件修正（CalendarEventDialogs 抽出、ReminderTimePicker 抽出）。assembleDebug ビルド成功、Detekt 0 issues。

### Phase 4: 全 Task 参照除去 + 完全統合 - DONE
Task モデル・TaskRepository・CalendarEventTaskAdapter を完全削除（空ファイル化→プロダクションコード全除去）。SearchResult.TaskResult 除去、CalendarEventResult に統一。PreviewData.kt の旧 Task データ削除、CalendarEvent(type=TASK) に置換。SearchScreen.kt の TaskResult 分岐除去。SearchRepositoryImplTest の timestamp ソートテスト修正（startTime 追加）。FakeSyncRepository/NoOpSyncRepository/FirestoreSyncRepositoryImpl から Task 関連メソッド除去。全1897テスト合格。
- 変更ファイル:
  - `ui/preview/PreviewData.kt` — Task import/データ削除、SearchResult.CalendarEventResult に置換
  - `ui/screens/search/SearchScreen.kt` — TaskResult 分岐削除、CheckCircle import 除去
  - `test/.../SearchRepositoryImplTest.kt` — createTaskEvent に startTime 追加、timestamp ソートテスト修正
  - (worker-impl/worker-test による変更): SyncRepository, NoOpSyncRepository, FirestoreSyncRepositoryImpl, FakeSyncRepository, HomeViewModel, HomeViewModelTest 等
- 品質ゲート: assembleDebug 成功、testDebugUnitTest 全1897テスト合格

### Phase 5: E2E テスト + カバレッジ最終調整 - DONE
TasksFlowTest 空ファイル化。EditFlowTest/DeleteFlowTest/ValidationFlowTest から Task テスト削除。NavigationFlowTest Tasks→Timeline 置換。Screen.Tasks data object 削除。TestTags.TASKS_FAB 削除。assembleDebug ビルド成功、testDebugUnitTest 全テスト合格。

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
| Phase 1(統合) | CalendarEvent 拡張: +4フィールド(priority,reminderEnabled,reminderTime,createdBy) + CalendarEventType.TASK + validate() + isTask + DB v23→v24 | DONE |
| Phase 2(統合) | CalendarEventTaskAdapter Bridge + TaskEntity/DAO/Mapper/Syncer/Repository 5ファイル削除 + DB v24→v25 + ENTITY_TYPE_COUNT 7→6 + テスト19件新規 + 全1985テスト合格 | DONE |
| Phase 3(統合) | AddEditCalendarEventScreen/ViewModel Task フィールド統合 + TasksScreen/ViewModel 全削除 + BottomNav Tasks→Timeline + テスト13件追加 + Detekt 0 issues | DONE |
| Phase 4(統合) | Task モデル/Repository/Adapter 完全削除 + SearchResult.TaskResult 除去 + PreviewData/SearchScreen 修正 + 全1897テスト合格 | DONE |
| Phase 5(統合) | E2E テスト修正 + Task 残存コード削除。TasksFlowTest 空ファイル化、EditFlowTest/DeleteFlowTest/ValidationFlowTest Task テスト削除、NavigationFlowTest Tasks→Timeline 置換、Screen.Tasks/TestTags.TASKS_FAB 削除 | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v25, SQLCipher 4.6.1, fallbackToDestructiveMigration, 13 Entity (TaskEntity 削除) |
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
