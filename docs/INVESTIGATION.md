# Investigation: Task→CalendarEvent 統合 + デイリータイムラインビュー

日付: 2026-02-17
カテゴリ: リファクタリング
対象: Task を CalendarEvent に統合し、デイリータイムラインビューを BottomNav に追加する実現可能性調査

## 概要

Task と CalendarEvent は共通フィールドが多く（title, description, date, recurrence, completed 等）、統合の技術的実現性は高い。Task 固有フィールド（priority, reminderEnabled, reminderTime, createdBy, isCompleted）を CalendarEvent に追加し、CalendarEventType に TASK を追加することで統合可能。`fallbackToDestructiveMigration(dropAllTables = true)` が有効なため Room Migration の複雑度は低い。BottomNav は現 6 タブ（Home, Medication, Calendar, Tasks, HealthRecords, Notes）から Tasks を Timeline に置換する形で再構成する。影響ファイル数は 40+ で大規模リファクタリングとなる。

## 調査結果

### 事実収集 (researcher)

#### Task モデル（現状）
`app/src/main/java/com/carenote/app/domain/model/Task.kt:12-27`
- `id: Long = 0`
- `careRecipientId: Long = 0`
- `title: String`
- `description: String = ""`
- `dueDate: LocalDate? = null`
- `isCompleted: Boolean = false`
- `priority: TaskPriority = TaskPriority.MEDIUM` (LOW, MEDIUM, HIGH)
- `recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE`
- `recurrenceInterval: Int = 1`
- `reminderEnabled: Boolean = false`
- `reminderTime: LocalTime? = null`
- `createdBy: String = ""`
- `createdAt: LocalDateTime = LocalDateTime.now()`
- `updatedAt: LocalDateTime = LocalDateTime.now()`

#### CalendarEvent モデル（現状）
`app/src/main/java/com/carenote/app/domain/model/CalendarEvent.kt:12-27`
- `id: Long = 0`
- `careRecipientId: Long = 0`
- `title: String`
- `description: String = ""`
- `date: LocalDate` (非 nullable, Task の dueDate は nullable)
- `startTime: LocalTime? = null`
- `endTime: LocalTime? = null`
- `isAllDay: Boolean = true`
- `type: CalendarEventType = CalendarEventType.OTHER`
- `completed: Boolean = false`
- `recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE`
- `recurrenceInterval: Int = 1`
- `createdAt: LocalDateTime = LocalDateTime.now()`
- `updatedAt: LocalDateTime = LocalDateTime.now()`

#### CalendarEventType（現状）
`app/src/main/java/com/carenote/app/domain/model/CalendarEventType.kt:6-11`
- `HOSPITAL` (通院)
- `VISIT` (訪問)
- `DAYSERVICE` (デイサービス)
- `OTHER` (その他)

#### フィールド差分（Task にあって CalendarEvent にないもの）
| Task フィールド | CalendarEvent 対応 | 備考 |
|---|---|---|
| `dueDate: LocalDate?` | `date: LocalDate` | Task は nullable, CalendarEvent は non-null |
| `isCompleted: Boolean` | `completed: Boolean` | 名前が微妙に異なるが同等 |
| `priority: TaskPriority` | -- なし -- | Task 固有。追加が必要 |
| `reminderEnabled: Boolean` | -- なし -- | Task 固有。追加が必要 |
| `reminderTime: LocalTime?` | -- なし -- | Task 固有。追加が必要 |
| `createdBy: String` | -- なし -- | Task 固有。追加が必要 |
| -- なし -- | `startTime: LocalTime?` | CalendarEvent 固有 |
| -- なし -- | `endTime: LocalTime?` | CalendarEvent 固有 |
| -- なし -- | `isAllDay: Boolean` | CalendarEvent 固有 |
| -- なし -- | `type: CalendarEventType` | CalendarEvent 固有 |

#### BottomNav 現状
`app/src/main/java/com/carenote/app/ui/navigation/Screen.kt:269`
```kotlin
val bottomNavItems get() = listOf(Home, Medication, Calendar, Tasks, HealthRecords, Notes)
```
6 タブ構成: Home, Medication, Calendar, Tasks, HealthRecords, Notes

#### DB バージョン
`app/src/main/java/com/carenote/app/data/local/CareNoteDatabase.kt:53`
- **version = 23**
- 14 エンティティ: MedicationEntity, MedicationLogEntity, NoteEntity, HealthRecordEntity, CalendarEventEntity, TaskEntity, SyncMappingEntity, CareRecipientEntity, PhotoEntity, EmergencyContactEntity, NoteCommentEntity, PurchaseEntity, MemberEntity, InvitationEntity
- `exportSchema = true`

`app/src/main/java/com/carenote/app/di/DatabaseModule.kt:59`
- `.fallbackToDestructiveMigration(dropAllTables = true)` **有効**
- 未リリースアプリのため、Migration SQL なしでスキーマ変更可能

#### Task 関連ファイル一覧（影響範囲）

**Domain 層:**
- `domain/model/Task.kt` -- 削除対象
- `domain/model/TaskPriority.kt` -- 移行 or 維持
- `domain/repository/TaskRepository.kt` -- 削除対象（CalendarEventRepository に統合）
- `domain/repository/TaskReminderSchedulerInterface.kt` -- リマインダー I/F（CalendarEvent 対応に改修）

**Data 層:**
- `data/local/entity/TaskEntity.kt` -- 削除対象
- `data/local/dao/TaskDao.kt` -- 削除対象（CalendarEventDao に統合）
- `data/mapper/TaskMapper.kt` -- 削除対象
- `data/mapper/remote/TaskRemoteMapper.kt` -- 削除対象
- `data/repository/TaskRepositoryImpl.kt` -- 削除対象
- `data/repository/sync/TaskSyncer.kt` -- 削除対象
- `data/worker/TaskReminderWorker.kt` -- CalendarEvent ベースに改修
- `data/export/TaskCsvExporter.kt` -- CalendarEvent ベースに改修
- `data/export/TaskPdfExporter.kt` -- CalendarEvent ベースに改修

**DI 層:**
- `di/DatabaseModule.kt` -- TaskDao 提供削除、CalendarEventDao 改修
- `di/AppModule.kt` -- TaskRepository バインディング削除/統合

**UI 層:**
- `ui/navigation/Screen.kt` -- Tasks 削除、Timeline 追加（bottomNavItems 変更）
- `ui/navigation/CareNoteNavHost.kt` -- ルーティング変更
- `ui/screens/tasks/TasksScreen.kt` -- 削除対象
- `ui/screens/tasks/AddEditTaskScreen.kt` -- 削除対象（AddEditCalendarEvent に統合）
- `ui/screens/tasks/components/*` -- 削除対象
- `ui/screens/timeline/TimelineScreen.kt` -- BottomNav 対応に改修（現在は Secondary 画面）
- `ui/screens/timeline/TimelineViewModel.kt` -- BottomNav 対応に改修
- `ui/screens/timeline/components/TimelineItemCard.kt` -- 改修
- `ui/screens/home/HomeViewModel.kt` -- Task 参照削除
- `ui/screens/calendar/CalendarScreen.kt` -- Task 統合表示追加
- `ui/screens/calendar/AddEditCalendarEventScreen.kt` -- Task フィールド追加
- `ui/viewmodel/TasksViewModel.kt` -- 削除対象
- `ui/viewmodel/AddEditTaskViewModel.kt` -- 削除対象

**テスト:**
- `test/**/fakes/FakeTaskRepository.kt` -- 削除対象
- `test/**/fakes/FakeTaskReminderScheduler.kt` -- 改修
- `test/**/fakes/FakeTaskCsvExporter.kt` -- 削除対象
- `test/**/fakes/FakeTaskPdfExporter.kt` -- 削除対象
- `test/**/viewmodel/TasksViewModelTest.kt` -- 削除対象
- `test/**/viewmodel/AddEditTaskViewModelTest.kt` -- 削除対象
- `test/**/viewmodel/TimelineViewModelTest.kt` -- 改修
- `test/**/mapper/TaskMapperTest.kt` -- 削除対象
- `androidTest/**/e2e/TasksFlowTest.kt` -- 削除 or 大幅改修

#### 既存 TimelineScreen
`app/src/main/java/com/carenote/app/ui/screens/timeline/TimelineScreen.kt`
- 既に実装済み。日次タイムラインビューとして機能
- `TimelineViewModel` が `TimelineRepository.getTimelineItemsForDate()` を呼び出し
- 日付ナビゲーション（前日/翌日/今日）搭載
- `TimelineItem` sealed class が MedicationLog, CalendarEvent, Task, HealthRecord, Note の 5 種を統合表示
- 現在は Secondary 画面（`Screen.Timeline`）で、BottomNav には含まれていない
- `onNavigateBack` で戻るナビゲーションのみ

### コード分析 (code-analyst)

#### 統合時のフィールド設計案

CalendarEvent に以下のフィールドを追加:
```kotlin
data class CalendarEvent(
    // ... 既存フィールド ...
    val priority: TaskPriority? = null,          // Task 統合: 優先度（TASK タイプのみ使用）
    val reminderEnabled: Boolean = false,         // Task 統合: リマインダー有効
    val reminderTime: LocalTime? = null,          // Task 統合: リマインダー時刻
    val createdBy: String = "",                   // Task 統合: 作成者
)
```

- `date` の nullable 問題: Task の `dueDate` は nullable だが CalendarEvent の `date` は non-null。TASK タイプでは `date` を必須にするか、`date` を nullable に変更するか検討が必要。**推奨**: `date` は non-null 維持。TASK 作成時にデフォルト日付（作成日）を設定
- `isCompleted` vs `completed`: CalendarEvent 既存の `completed` をそのまま使用

#### CalendarEventType 拡張案
```kotlin
enum class CalendarEventType {
    HOSPITAL,    // 通院
    VISIT,       // 訪問
    DAYSERVICE,  // デイサービス
    TASK,        // タスク（統合）
    OTHER        // その他
}
```

TASK タイプ追加のみ。既存の enum 値は変更不要。

#### Room Migration 戦略
- `fallbackToDestructiveMigration(dropAllTables = true)` が有効のため、**Migration SQL 不要**
- DB version を 23 → 24 にインクリメント
- `tasks` テーブル削除、`calendar_events` テーブルにカラム追加
- 未リリースアプリのため既存データ損失リスクなし
- `CareNoteDatabase.kt` から `TaskEntity::class` と `TaskDao` を削除

#### CalendarEventEntity 拡張案
```kotlin
@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    // ... 既存カラム ...
    @ColumnInfo(name = "priority") val priority: String? = null,
    @ColumnInfo(name = "reminder_enabled") val reminderEnabled: Int = 0,
    @ColumnInfo(name = "reminder_time") val reminderTime: String? = null,
    @ColumnInfo(name = "created_by") val createdBy: String = "",
)
```

インデックス追加案:
- `Index(value = ["type"])` -- TASK タイプのフィルタリング用
- `Index(value = ["completed"])` -- 未完了タスクの取得用

#### CalendarEventDao 拡張案
既存クエリに加え、Task 固有のクエリを追加:
- `getTaskEvents()` -- type = 'TASK' フィルタ
- `getIncompleteTaskEvents()` -- type = 'TASK' AND completed = 0
- `getPagedTaskEvents()` -- Paging 対応
- `getIncompleteTaskCount()` -- バッジ表示用

#### BottomNav 再構成案
```kotlin
// 変更前: Home, Medication, Calendar, Tasks, HealthRecords, Notes (6タブ)
// 変更後: Home, Medication, Calendar, Timeline, HealthRecords, Notes (6タブ)
val bottomNavItems get() = listOf(Home, Medication, Calendar, Timeline, HealthRecords, Notes)
```

- Tasks タブを Timeline タブに置換（タブ数は 6 のまま）
- Timeline アイコン: `Icons.Filled.ViewTimeline` / `Icons.Outlined.ViewTimeline` (Material Icons)
- 既存 `Screen.Timeline` の route を再利用

#### タイムラインビュー実装パターン
既存の `TimelineScreen` をベースに以下を改修:
1. **TopAppBar 変更**: 戻るボタン削除（BottomNav 画面のため不要）
2. **FAB 追加**: イベント/タスク追加ボタン
3. **フィルタ機能**: タイムラインアイテムのタイプ別フィルタ（全て/予定/タスク/服薬/健康記録/メモ）
4. **タスク完了トグル**: タスクアイテムのチェックボックスをタップで完了/未完了切替
5. **タスク管理 UI**: カレンダー画面の AddEditCalendarEventScreen で TASK タイプ選択時に priority, reminder 等のフィールドを動的表示

#### TaskRepository → CalendarEventRepository 統合
`CalendarEventRepository` に以下メソッドを追加:
- `getTaskEvents(): Flow<List<CalendarEvent>>` -- type=TASK のイベント一覧
- `getIncompleteTaskEvents(): Flow<List<CalendarEvent>>` -- 未完了タスク
- `getTaskEventsByDueDate(date: LocalDate): Flow<List<CalendarEvent>>` -- 日付別タスク
- `getPagedTaskEvents(query: String): Flow<PagingData<CalendarEvent>>` -- Paging 対応
- `getIncompleteTaskCount(): Flow<Int>` -- バッジ用カウント

#### Syncer 統合
- `TaskSyncer` を削除
- `CalendarEventSyncer` がタスクイベントも同期（type フィールドで区別）
- Firestore コレクションパス: `careRecipients/{id}/calendarEvents/{id}` に統合
- 既存の `careRecipients/{id}/tasks/{id}` データは Firestore 側でマイグレーション不要（未リリース）

#### テスト影響範囲
| カテゴリ | 削除 | 新規/大幅改修 | 軽微改修 |
|---------|------|-------------|---------|
| ViewModel テスト | 2 (Tasks, AddEditTask) | 1 (Timeline 改修) | 2 (Home, Calendar) |
| Mapper テスト | 1 (TaskMapper) | 0 (CalendarEventMapper 拡張) | 1 (CalendarEventMapper) |
| Repository テスト | 1 (TaskRepository) | 0 | 1 (CalendarEventRepository) |
| Fake | 4 (FakeTask*) | 0 | 1 (FakeCalendarEventRepository) |
| E2E | 1 (TasksFlowTest) | 0 | 3+ (Calendar, Timeline, CriticalPath) |
| Worker テスト | 0 | 0 | 1 (TaskReminderWorker) |
| **合計** | **9** | **1** | **9** |

### リスク評価 (risk-assessor)

#### リスク一覧

| リスク | レベル | 詳細 | 緩和策 |
|--------|--------|------|--------|
| 大規模変更による回帰バグ | **HIGH** | 40+ ファイル変更。見落としリスク | フェーズ分割実装、各フェーズでテスト |
| CalendarEvent モデル肥大化 | **MEDIUM** | Task フィールド追加で CalendarEvent が複雑化 | nullable フィールドで TASK 以外は影響なし |
| date non-null 制約と Task の dueDate nullable 不一致 | **MEDIUM** | Task は dueDate なしが可能だが CalendarEvent は date 必須 | デフォルト日付（作成日）で対処 |
| BottomNav UX 変更 | **LOW** | Tasks タブが消えることによるユーザー混乱 | 未リリースアプリのため影響なし |
| Firestore データ互換性 | **LOW** | 既存 tasks コレクションのデータ | 未リリースのためマイグレーション不要 |
| Room Migration 失敗 | **LOW** | fallbackToDestructiveMigration が有効 | 未リリースのためデータ損失許容 |
| TimelineScreen の BottomNav 適合 | **LOW** | 既存実装は Secondary 画面向け | TopAppBar 変更のみで対応可能 |
| Exporter 互換性 | **LOW** | TaskCsvExporter/PdfExporter の統合 | CalendarEvent Exporter で TASK フィルタ |

#### 代替案比較

| 案 | 概要 | メリット | デメリット | 工数 | リスク | 推奨 |
|---|---|---|---|---|---|---|
| A | **完全統合（Task 廃止）** | モデル統一、重複コード削減、保守性向上 | 大規模変更、CalendarEvent 肥大化 | 大（40+ ファイル） | MEDIUM | **推奨** |
| B | **軽量統合（isTask フラグ）** | CalendarEvent に `isTask: Boolean` 追加のみ。最小変更 | Task 固有フィールド（priority 等）の扱いが曖昧、型安全性低下 | 中 | MEDIUM | -- |
| C | **UI 統合のみ（別モデル維持）** | Task/CalendarEvent モデルは維持し、Timeline ビューで統合表示 | 重複コード残存、2 つの Repository/DAO 維持コスト | 小（UI のみ） | LOW | -- |

**推奨: 案 A（完全統合）**
- 未リリースアプリのため後方互換性の心配不要
- `fallbackToDestructiveMigration` で DB Migration リスクゼロ
- 長期的な保守性向上が最大のメリット
- CalendarEventType.TASK で型安全にタスクを区別可能
- 大規模だが、フェーズ分割で段階的に実装すればリスク管理可能

**実装フェーズ案:**
1. **Phase 1: Domain 層統合** -- CalendarEvent モデル拡張、CalendarEventType.TASK 追加、TaskPriority 維持、Task モデル削除
2. **Phase 2: Data 層統合** -- CalendarEventEntity 拡張、CalendarEventDao 拡張、TaskEntity/TaskDao/TaskMapper/TaskSyncer 削除、DB version 24
3. **Phase 3: DI 層統合** -- TaskRepository バインディング削除、CalendarEventRepository 拡張バインディング追加
4. **Phase 4: UI 層統合** -- AddEditCalendarEventScreen に TASK フィールド追加、TasksScreen/AddEditTaskScreen 削除、BottomNav 変更（Tasks → Timeline）、TimelineScreen を BottomNav 対応に改修
5. **Phase 5: Worker/Exporter 統合** -- TaskReminderWorker を CalendarEvent ベースに改修、Exporter 統合
6. **Phase 6: テスト更新** -- Fake 削除/改修、ViewModel テスト更新、E2E テスト更新

## 関連ファイル

| ファイル | 関連度 | メモ |
|---------|--------|------|
| `domain/model/Task.kt` | HIGH | 削除対象 |
| `domain/model/CalendarEvent.kt` | HIGH | フィールド追加 |
| `domain/model/CalendarEventType.kt` | HIGH | TASK 追加 |
| `domain/model/TaskPriority.kt` | HIGH | 維持（CalendarEvent から参照） |
| `domain/model/TimelineItem.kt` | HIGH | TaskItem → CalendarEventItem 統合 |
| `domain/repository/TaskRepository.kt` | HIGH | 削除対象 |
| `domain/repository/CalendarEventRepository.kt` | HIGH | Task 系メソッド追加 |
| `domain/repository/TimelineRepository.kt` | MEDIUM | TaskItem 参照削除 |
| `data/local/entity/TaskEntity.kt` | HIGH | 削除対象 |
| `data/local/entity/CalendarEventEntity.kt` | HIGH | カラム追加 |
| `data/local/dao/TaskDao.kt` | HIGH | 削除対象 |
| `data/local/dao/CalendarEventDao.kt` | HIGH | Task 系クエリ追加 |
| `data/local/CareNoteDatabase.kt` | HIGH | TaskEntity 削除、version 24 |
| `data/mapper/TaskMapper.kt` | HIGH | 削除対象 |
| `data/mapper/CalendarEventMapper.kt` | MEDIUM | フィールド追加対応 |
| `data/repository/TaskRepositoryImpl.kt` | HIGH | 削除対象 |
| `data/repository/CalendarEventRepositoryImpl.kt` | MEDIUM | Task 系メソッド追加 |
| `data/repository/sync/TaskSyncer.kt` | HIGH | 削除対象 |
| `data/worker/TaskReminderWorker.kt` | MEDIUM | CalendarEvent ベースに改修 |
| `data/export/TaskCsvExporter.kt` | MEDIUM | 削除 or 統合 |
| `data/export/TaskPdfExporter.kt` | MEDIUM | 削除 or 統合 |
| `di/DatabaseModule.kt` | MEDIUM | TaskDao 削除 |
| `di/AppModule.kt` | MEDIUM | TaskRepository バインディング変更 |
| `ui/navigation/Screen.kt` | HIGH | bottomNavItems 変更 |
| `ui/navigation/CareNoteNavHost.kt` | MEDIUM | ルーティング変更 |
| `ui/screens/tasks/*` | HIGH | 全削除 |
| `ui/screens/timeline/TimelineScreen.kt` | HIGH | BottomNav 対応改修 |
| `ui/screens/calendar/AddEditCalendarEventScreen.kt` | MEDIUM | TASK フィールド追加 |
| `ui/viewmodel/TasksViewModel.kt` | HIGH | 削除対象 |
| `ui/viewmodel/AddEditTaskViewModel.kt` | HIGH | 削除対象 |
| `ui/screens/home/HomeViewModel.kt` | LOW | Task 参照変更 |
| `config/AppConfig.kt` | LOW | Analytics イベント定数変更 |

## 未解決の疑問

1. **dueDate nullable 問題**: Task の `dueDate` は nullable（期限なしタスク可）だが、CalendarEvent の `date` は non-null。期限なしタスクをどう表現するか？ → 案: 作成日をデフォルト date として設定 / または date を nullable に変更
2. **Firestore コレクション構造**: 統合後のコレクションパスは `calendarEvents` に一本化するが、将来リリース時に既存 `tasks` コレクションからの移行が必要になる可能性
3. **TaskReminderScheduler 命名**: CalendarEvent ベースに改修後、名前を `CalendarEventReminderScheduler` に変更するか、`EventReminderScheduler` のような汎用名にするか
4. **Exporter 統合方針**: Task エクスポートを CalendarEvent エクスポートに完全統合するか、TASK タイプ専用エクスポーターを別途維持するか
5. **Timeline 画面のフィルタ UI**: 現在の TimelineScreen にはフィルタ機能がない。BottomNav 昇格時にフィルタ（タスクのみ表示等）を追加すべきか、最小限の改修に留めるか
