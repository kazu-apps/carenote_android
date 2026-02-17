# Investigation: リマインダー通知バグ + タブレット DatePicker バグ

日付: 2026-02-17
カテゴリ: バグ修正
対象: HANDOVER.md Phase 1（リマインダー通知未着）+ Phase 2（タブレット DatePicker/TimePicker 不動作）の再調査

## 概要

2件のバグについてコードベースを再調査した。Phase 1（calculateDelay バグ）は根本原因が確定済み。Phase 2（タブレット DatePicker）は TextButton のタッチ領域問題が最有力仮説だが、実機検証が必要。

## 調査結果

### 事実収集 (researcher)

#### バグ1: リマインダー通知が届かない

- `TaskReminderScheduler.calculateDelay()` (data/worker/TaskReminderScheduler.kt:122-131): 過去時刻で `-1` を返す。翌日へのスケジュールロジックなし
- `TaskReminderScheduler.scheduleReminder()` (同:46-48): `delay < 0` で即 return（ログ出力のみ、リスケジュールなし）
- `MedicationReminderScheduler.calculateDelay()` (data/worker/MedicationReminderScheduler.kt:142-151): 同一実装、同一バグ
- `MedicationReminderScheduler.scheduleReminder()` (同:48-52): 同一 `delay < 0` ガード
- 両 Scheduler とも `LocalDateTime.now()` を直接使用 — Clock injection なし
- `TaskReminderSchedulerInterface` (domain/repository/TaskReminderSchedulerInterface.kt:10): インターフェース定義あり（Fake 注入用）
- `MedicationReminderSchedulerInterface` も同様に存在
- フォローアップ通知: `scheduleFollowUp()` は固定間隔（`FOLLOW_UP_INTERVAL_MINUTES`）で動作し、calculateDelay を使わないため影響なし

#### バグ2: タブレットで DatePicker/TimePicker が開かない

- `AddEditTaskScreen.kt:64-65`: `var showDatePicker by remember { mutableStateOf(false) }` / `var showTimePicker`
- `AddEditTaskScreen.kt:331`: DueDateSelector 内 `TextButton(onClick = onClickDate)` — タッチ領域がテキスト幅のみ
- `AddEditTaskScreen.kt:505`: ReminderSection 内 `TextButton(onClick = onClickTime)` — 同上
- `AddEditTaskScreen.kt:94-112`: Dialog は `CareNoteAddEditScaffold` の外に配置。Compose Dialog は独自 `android.app.Dialog` Window を作成するため、Scaffold のネスト構造に依存しない
- `AddEditCalendarEventScreen.kt:421, 466`: 同一 `TextButton` パターン（DateSelector, TimeSelector）
- `AdaptiveNavigationScaffold.kt:32-38`: タブレット（幅 >= 840dp）で `NavigationSuiteType.NavigationDrawer` を使用
- スマホでは正常動作するため、Compose の state 管理自体に問題はない

### コード分析 (code-analyst)

#### バグ1: calculateDelay パターン

- **テスタビリティ問題**: `calculateDelay()` は `private` メソッドで `LocalDateTime.now()` を直接呼び出し。テストするには:
  - (A) Clock injection を Scheduler に追加（推奨）
  - (B) calculateDelay を internal/package-private にして直接テスト
  - (C) scheduleReminder 経由で WorkManager の enqueue を verify
- **共通コード**: Task/Medication の calculateDelay は完全同一実装。共通化の余地あり（ただし修正スコープ外）
- **既存テスト**: `TaskReminderWorkerTest.kt`, `MedicationReminderWorkerTest.kt` は Worker のロジック（おやすみ時間、服薬済みチェック等）をテスト。Scheduler のスケジュールロジックはテストされていない
- **Scheduler テストなし**: `*SchedulerTest*` は存在しない

#### バグ2: UI タッチハンドリング

- **TextButton のタッチ領域**: Material 3 の `TextButton` は内部テキストの幅 + パディングのみ。タブレットの大画面では「期限なし」のテキスト幅が Row 全体に対して非常に小さい
- **Row 構造**: `DueDateSelector` は `Row(fillMaxWidth, SpaceBetween)` で左に「期限」、右に TextButton。右側の TextButton のタッチ領域は限定的
- **キーボード干渉仮説**: タスクフォームには複数の `OutlinedTextField`（タイトル、説明、繰り返し間隔）があり、フォーカスがある状態でのタブレットフローティングキーボードの挙動が影響する可能性
- **Dialog 配置は問題なし**: Scaffold 外配置は正しいパターン。Compose Dialog は独自 Window を生成するため

### リスク評価 (risk-assessor)

#### バグ1 修正リスク

- **エッジケース — delay = 0**: `target.isAfter(now)` は `false` を返す（`isAfter` は strictly after）。修正時に `>= 0` にするか `plusDays(1)` にするかの判断が必要
- **エッジケース — 深夜0時**: `LocalTime.of(0, 0)` は常に過去として扱われる（当日の 00:00 は過ぎている）。plusDays(1) で翌日 00:00 にスケジュールされるが、これが意図通りか確認必要
- **服薬リマインダー固有**: `MedicationReminderWorker` は服薬済みチェック（`TAKEN` ログ確認）とフォローアップ通知を持つ。calculateDelay 修正はこのロジックに影響しない（Worker 実行時のチェックのため）
- **セキュリティ**: 通知にタスクタイトル/薬名を含むが、これはローカル通知でありリスクは低い。WorkManager の delay 値は内部計算のみで外部入力なし
- **テスト不在リスク**: Scheduler テストが存在しないため、修正の正当性を担保するテストが必須

#### バグ2 修正リスク

- **スマホ動作への影響**: TextButton → Row+clickable 変更はタッチ領域拡大のみ。スマホでの既存動作が壊れるリスクは低い
- **アクセシビリティ**: TextButton は semantics に `Role.Button` を持つ。Row+clickable に変更する場合、`semantics { role = Role.Button }` を追加しないと TalkBack で「ボタン」と読まれなくなる
- **E2E テスト**: DatePicker/TimePicker の E2E テストは存在しない。修正しても既存テストは壊れない
- **Detekt**: Row+clickable パターンは LongMethod/NestedBlockDepth に抵触しない（現状のメソッド行数に余裕あり）
- **仮説検証**: TextButton 問題が真因でない場合、NavigationDrawer のタッチイベント問題を別途調査する必要がある

## 関連ファイル

| ファイル | 関連度 | メモ |
|---------|--------|------|
| `data/worker/TaskReminderScheduler.kt` | HIGH | calculateDelay バグ（Phase 1 修正対象） |
| `data/worker/MedicationReminderScheduler.kt` | HIGH | 同一 calculateDelay バグ（Phase 1 修正対象） |
| `ui/screens/tasks/AddEditTaskScreen.kt` | HIGH | TextButton タッチ領域問題（Phase 2 修正対象） |
| `ui/screens/calendar/AddEditCalendarEventScreen.kt` | HIGH | 同一 TextButton パターン（Phase 2 修正対象） |
| `data/worker/TaskReminderWorker.kt` | MEDIUM | Phase 1 で Worker 側の挙動確認用 |
| `data/worker/MedicationReminderWorker.kt` | MEDIUM | Phase 1 で Worker 側の挙動確認用 |
| `ui/navigation/AdaptiveNavigationScaffold.kt` | MEDIUM | タブレットの NavigationDrawer 実装 |
| `ui/components/CareNoteDatePickerDialog.kt` | LOW | 共通 DatePicker ダイアログ（変更不要） |
| `ui/components/CareNoteTimePickerDialog.kt` | LOW | 共通 TimePicker ダイアログ（変更不要） |
| `domain/repository/TaskReminderSchedulerInterface.kt` | LOW | Scheduler インターフェース（変更不要） |

## 未解決の疑問

1. **Phase 2 の真因未確定**: TextButton のタッチ領域が原因である可能性が最も高いが、タブレットエミュレータでの実機検証が未実施。NavigationDrawer のタッチイベント干渉の可能性も残る
2. **delay = 0 の扱い**: リマインダー時刻 = 現在時刻の場合、即時通知すべきか翌日スケジュールすべきか。ユーザー体験の観点から判断が必要
3. **Clock injection**: 修正スコープに含めるべきか。含めればテスタビリティが大幅に向上するが、Scheduler の constructor 変更 + DI 設定変更が必要
