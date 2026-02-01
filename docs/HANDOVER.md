# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## タスク: Phase 7-3 タスク管理 UI 実装

## フェーズロードマップ

### Item 5: 健康記録機能 - DONE

#### Phase 5-1: ドメイン層 + データ層 - DONE
#### Phase 5-2: ViewModel 層 (TDD) - DONE
#### Phase 5-3: UI 実装 - DONE
#### Phase 5-4: グラフ表示機能 - DONE

---

### Item 6: カレンダー機能 - DONE

介護予定（通院・訪問介護・デイサービス等）を管理するカレンダー機能。
月表示カレンダー + イベント一覧 + 追加/編集フォーム。

#### Phase 6-1: ドメイン層 + データ層 - DONE
CalendarEvent ドメインモデル、Room Entity/DAO、Mapper、Repository 実装
- 変更対象: domain/model/, domain/repository/, data/local/, data/mapper/, data/repository/, di/, config/
- 依存: なし
- 新規ファイル 8、変更ファイル 6（DB v4、DI x2、AppConfig、strings x2）
- 成果物: CalendarEvent モデル + CRUD リポジトリ + Mapper テスト 12件 + Repository テスト 12件
- テスト: 全パス、ビルド成功

#### Phase 6-2: ViewModel 層 (TDD) - DONE
CalendarViewModel + AddEditCalendarEventViewModel を TDD で実装
- 変更対象: ui/screens/calendar/, test/.../calendar/, test/.../fakes/, ui/navigation/Screen.kt
- 依存: Phase 6-1 完了後
- 新規ファイル 5（ViewModel x2, Test x2, FakeRepo x1）、変更 1（Screen.kt）
- 成果物: ViewModel ロジック + テスト 35件（CalendarVM 14件 + AddEditVM 21件）+ ナビゲーション定義
- テスト: 全パス、ビルド成功

#### Phase 6-3: UI 実装 - DONE
CalendarScreen（月表示 + イベント一覧）+ AddEditCalendarEventScreen + ナビゲーション接続
- 変更対象: ui/screens/calendar/, ui/navigation/, res/values/, config/
- 依存: Phase 6-2 完了後
- 新規ファイル 4（CalendarEventCard, DayCell, MonthCalendarGrid, AddEditCalendarEventScreen）
- 変更ファイル 5（CalendarScreen, CareNoteNavHost, AppConfig, strings.xml x2）
- 成果物: カレンダー画面フル実装 + イベント追加/編集フォーム + Material3 DatePicker/TimePicker
- テスト: 全パス（既存テスト影響なし）、ビルド成功

---

### Item 7: タスク管理機能 - DONE

介護タスク（買い物・掃除・書類手続き等）の管理機能。
タスク一覧（完了/未完了フィルタ）+ 追加/編集フォーム + 完了トグル。

#### Phase 7-1: ドメイン層 + データ層 - DONE
Task ドメインモデル（TaskPriority enum）、Room Entity/DAO、Mapper、Repository 実装
- 変更対象: domain/model/, domain/repository/, data/local/, data/mapper/, data/repository/, di/, config/, res/values/
- 依存: Phase 6-1 後（DB バージョン管理）
- 新規ファイル 10（TaskPriority, Task, TaskRepository, TaskEntity, TaskDao, TaskMapper, TaskRepositoryImpl, TaskMapperTest, TaskRepositoryImplTest, FakeTaskRepository）
- 変更ファイル 6（CareNoteDatabase v4→v5, DatabaseModule, AppModule, AppConfig, strings.xml JP/EN）
- 成果物: Task モデル + CRUD リポジトリ + Mapper テスト 13件 + Repository テスト 12件 + FakeTaskRepository
- テスト: 全パス、ビルド成功

#### Phase 7-2: ViewModel 層 (TDD) - DONE
TasksViewModel（フィルタ + 完了トグル + 削除）+ AddEditTaskViewModel を TDD で実装
- 変更対象: ui/screens/tasks/, test/.../tasks/, ui/navigation/Screen.kt
- 依存: Phase 7-1 完了後
- 新規ファイル 5（TaskFilterMode, TasksViewModel, AddEditTaskViewModel, TasksViewModelTest, AddEditTaskViewModelTest）、変更 1（Screen.kt）
- 成果物: ViewModel ロジック + テスト 40件（TasksVM 17件 + AddEditTaskVM 23件）+ ナビゲーション定義
- テスト: 全パス、ビルド成功

#### Phase 7-3: UI 実装 - DONE
TasksScreen（タスク一覧 + フィルタチップ + 完了チェック + 削除）+ AddEditTaskScreen + ナビゲーション接続
- 変更対象: ui/screens/tasks/, ui/navigation/, res/values/
- 依存: Phase 7-2 完了後
- 新規ファイル 3（TaskCard.kt, TaskFilterChips.kt, AddEditTaskScreen.kt）
- 変更ファイル 3（TasksScreen.kt, CareNoteNavHost.kt, strings.xml JP/EN）
- 成果物: タスク画面フル実装 + タスク追加/編集フォーム + フィルタチップ + 優先度バッジ + DatePicker
- テスト: 全パス（既存テスト影響なし）、ビルド成功

---

## 各 Phase の詳細設計

### Phase 6-1 設計メモ

**CalendarEvent ドメインモデル:**
```
CalendarEvent(
    id: Long,
    title: String,
    description: String,
    date: LocalDate,         // 日付（時刻なしイベント対応）
    startTime: LocalTime?,   // 開始時刻（任意）
    endTime: LocalTime?,     // 終了時刻（任意）
    isAllDay: Boolean,       // 終日イベント
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
)
```

**CalendarEventDao クエリ:**
- getAllEvents(): Flow（作成日降順）
- getEventById(id): Flow
- getEventsByDate(date: String): Flow（特定日のイベント）
- getEventsByDateRange(start, end): Flow（月表示用）
- insert / update / delete

**DB 変更:** v3 → v4（CalendarEventEntity 追加）

### Phase 6-2 設計メモ

**CalendarViewModel:**
- selectedDate: StateFlow<LocalDate>（選択中の日付）
- currentMonth: StateFlow<YearMonth>（表示中の月）
- eventsForMonth: StateFlow<Map<LocalDate, List<CalendarEvent>>>（月のイベントマップ）
- eventsForSelectedDate: StateFlow<List<CalendarEvent>>（選択日のイベント一覧）
- selectDate(date) / changeMonth(yearMonth) / deleteEvent(id)

**AddEditEventViewModel:**
- SavedStateHandle で eventId 取得（編集モード）
- FormState: title, description, date, startTime, endTime, isAllDay
- バリデーション: title 必須

### Phase 6-3 設計メモ

**CalendarScreen 構成:**
- TopAppBar（月名 + 前月/次月ボタン + 「今日」ボタン）
- MonthCalendarGrid（7列 x 6行、イベントありの日にドット表示）
- EventList（選択日のイベント一覧、CalendarEventCard で表示）
- FAB（イベント追加）
- SnackbarHost + ConfirmDialog（削除確認）

**コンポーネント分割:**
- MonthCalendarGrid.kt（カレンダーグリッド、曜日ヘッダー + 6x7 Row/Column）
- DayCell.kt（日付セル、選択ハイライト + 今日強調 + イベントドット）
- CalendarEventCard.kt（イベントカード、CareNoteCard ラップ）
- AddEditCalendarEventScreen.kt（フォーム画面、DatePicker/TimePicker/Switch）

### Phase 7-1 設計メモ

**Task ドメインモデル:**
```
TaskPriority enum: LOW, MEDIUM, HIGH

Task(
    id: Long,
    title: String,
    description: String,
    dueDate: LocalDate?,      // 期限（任意）
    isCompleted: Boolean,
    priority: TaskPriority,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
)
```

**TaskDao クエリ:**
- getAllTasks(): Flow（未完了優先、作成日降順）
- getTaskById(id): Flow
- getIncompleteTasks(): Flow（未完了のみ）
- getTasksByDueDate(date: String): Flow（期限日フィルタ）
- insert / update / delete

**DB 変更:** v4 → v5（TaskEntity 追加）

### Phase 7-2 設計メモ

**TasksViewModel:**
- tasks: StateFlow<UiState<List<Task>>>
- filterMode: StateFlow<TaskFilterMode>（ALL / INCOMPLETE / COMPLETED）
- toggleCompletion(id) — isCompleted をトグル
- deleteTask(id)
- TaskFilterMode enum

**AddEditTaskViewModel:**
- SavedStateHandle で taskId 取得
- FormState: title, description, dueDate, priority
- バリデーション: title 必須

### Phase 7-3 設計メモ

**TasksScreen 構成:**
- TopAppBar
- FilterChip 行（すべて / 未完了 / 完了済み）
- TaskList（LazyColumn + TaskCard）
- FAB（タスク追加）
- SnackbarHost + ConfirmDialog（削除確認）

**コンポーネント分割:**
- TaskCard.kt（Checkbox + タイトル（完了時取消線）+ 説明プレビュー + 期限 + 優先度バッジ、CareNoteCard ラップ）
- TaskFilterChips.kt（FilterChip x3: ALL/INCOMPLETE/COMPLETED）
- AddEditTaskScreen.kt（フォーム画面、CareNoteTextField + DueDateSelector + PrioritySelector + DatePicker）

---

## 実行順序

| 順番 | Phase | 内容 | 新規/変更ファイル |
|------|-------|------|------------------|
| ~~1~~ | ~~6-1~~ | ~~カレンダー ドメイン+データ層~~ | ~~~12~~ |
| ~~2~~ | ~~6-2~~ | ~~カレンダー ViewModel (TDD)~~ | ~~~7~~ |
| ~~3~~ | ~~6-3~~ | ~~カレンダー UI 実装~~ | ~~~9~~ |
| ~~4~~ | ~~7-1~~ | ~~タスク ドメイン+データ層~~ | ~~16~~ |
| ~~5~~ | ~~7-2~~ | ~~タスク ViewModel (TDD)~~ | ~~6~~ |
| ~~6~~ | ~~7-3~~ | ~~タスク UI 実装~~ | ~~6~~ |

## 完了フェーズ

### Phase 1（過去セッション完了）
Clean Architecture 基盤、Hilt DI、Room DB v1、Navigation Compose、Material 3 テーマ

### Phase 2（過去セッション完了）
UiState パターン、共通UIコンポーネント (6個)、ユーティリティ、i18n

### Phase 3（過去セッション完了）
服薬管理機能フル実装 (ViewModel x3, Screen x3, テスト 77個)

### Phase 4（過去セッション完了）
メモ・申し送り機能フル実装 (ViewModel x2, Screen x2, テスト 60個)

### Phase 5（過去セッション完了）
健康記録機能フル実装 (ドメイン+データ+ViewModel+UI+グラフ、テスト ~75件)

### Phase 6（過去セッション完了）
カレンダー機能フル実装 (ドメイン+データ+ViewModel+UI、テスト 59件)

### Phase 7（今回セッション完了）
タスク管理機能フル実装 (ドメイン+データ+ViewModel+UI、テスト 65件)

## 完了タスク（今回セッション）

- Phase 7-3: タスク管理 UI 実装
  - TaskCard.kt（CareNoteCard ラップ、Checkbox + タイトル取消線 + 説明プレビュー + 期限表示 + 優先度バッジ HIGH=赤/LOW=青）
  - TaskFilterChips.kt（FilterChip x3: ALL/INCOMPLETE/COMPLETED）
  - TasksScreen.kt（プレースホルダー → フル実装: Scaffold + TopAppBar + FAB + LazyColumn + FilterChips + TaskCard + EmptyState/Loading/Error + ConfirmDialog + Snackbar）
  - AddEditTaskScreen.kt（フォーム画面: CareNoteTextField title/description + DueDateSelector nullable + PrioritySelector FilterChip x3 + DatePicker + Cancel/Save ボタン）
  - CareNoteNavHost.kt（TasksScreen にナビゲーションコールバック追加 + AddTask/EditTask composable ルート追加）
  - strings.xml JP/EN にフィルタラベル追加（tasks_filter_all, tasks_filter_incomplete, tasks_filter_completed）

## 変更ファイル

| 操作 | ファイル |
|------|---------|
| 新規 | ui/screens/tasks/components/TaskCard.kt |
| 新規 | ui/screens/tasks/components/TaskFilterChips.kt |
| 新規 | ui/screens/tasks/AddEditTaskScreen.kt |
| 変更 | ui/screens/tasks/TasksScreen.kt（プレースホルダー → フル実装） |
| 変更 | ui/navigation/CareNoteNavHost.kt（タスクルート追加） |
| 変更 | res/values/strings.xml（フィルタラベル 3件追加） |
| 変更 | res/values-en/strings.xml（フィルタラベル 3件追加） |

## テスト結果

- `./gradlew.bat assembleDebug` — BUILD SUCCESSFUL
- `./gradlew.bat testDebugUnitTest` — BUILD SUCCESSFUL（全テストパス）

## 次のアクション

- Item 7 タスク管理機能は全 Phase 完了
- 次の機能実装へ（Firebase Auth、Cloud Firestore 等）

## 既知の問題
- Room DB は `fallbackToDestructiveMigration()` 使用中（プレリリースのため）
- ダークモード未対応
