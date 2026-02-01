# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 完了タスク

### Phase 4: メモ・申し送り機能

TDD でメモ・申し送り機能をフル実装した。検索（デバウンス 300ms）、タグフィルタ、CRUD 操作、ナビゲーションすべて動作確認済み。

1. **ドメイン層**
   - `Note.kt` — NoteTag enum (CONDITION, MEAL, REPORT, OTHER) + Note data class
   - `NoteRepository.kt` — searchNotes(query, tag) を含む 6 メソッド

2. **データ層**
   - `NoteEntity.kt` — Room Entity (notes テーブル)
   - `NoteDao.kt` — 9 メソッド (全文検索 + タグフィルタ 4 パターン)
   - `NoteMapper.kt` — Entity ↔ Domain 変換、不明タグは OTHER にフォールバック
   - `NoteRepositoryImpl.kt` — 4 パターン分岐の searchNotes 実装

3. **DB 更新 + DI**
   - `CareNoteDatabase.kt` — NoteEntity 追加、version 1 → 2
   - `DatabaseModule.kt` — provideNoteDao 追加
   - `AppModule.kt` — provideNoteRepository 追加

4. **ViewModel (TDD)**
   - `NotesViewModel.kt` — 検索デバウンス + タグフィルタの combine/flatMapLatest
   - `AddEditNoteViewModel.kt` — SavedStateHandle で追加/編集モード切替
   - テスト: `NotesViewModelTest.kt` (16 tests), `AddEditNoteViewModelTest.kt` (20 tests)

5. **UI コンポーネント**
   - `NoteTagChip.kt` — タグ別カラー FilterChip
   - `NoteCard.kt` — CareNoteCard ラップ、タイトル/内容プレビュー/タグ/日時
   - `NotesScreen.kt` — 検索バー + タグフィルタ + LazyColumn + Empty/Loading/Error
   - `AddEditNoteScreen.kt` — タイトル/内容/タグ選択フォーム + バリデーション

6. **ナビゲーション**
   - `Screen.kt` — AddNote, EditNote ルート追加
   - `CareNoteNavHost.kt` — Notes/AddNote/EditNote composable 追加

7. **i18n**
   - 15 文字列追加（JP/EN 両方）

8. **テスト**
   - `NoteMapperTest.kt` (10 tests)
   - `NoteRepositoryImplTest.kt` (14 tests)
   - `FakeNoteRepository.kt` (shouldFail フラグ付き)

### Phase 3: 服薬管理機能（フル実装）

既存バックエンドを活用し、服薬機能をエンドツーエンドで完成した。

1. **MedicationViewModel (TDD)**
   - StateFlow<UiState<List<Medication>>> で薬一覧管理
   - 今日の服薬ログ追跡、飲んだ/飲めなかった/後で記録
   - テスト: `MedicationViewModelTest.kt` (17 tests)

2. **AddMedicationViewModel (TDD)**
   - フォーム状態管理（名前/用量/タイミング/時刻/リマインダー）
   - バリデーション（名前必須）、保存処理
   - テスト: `AddMedicationViewModelTest.kt` (20 tests)

3. **MedicationDetailViewModel**
   - SavedStateHandle から medicationId 取得
   - 薬情報 + 服薬履歴の表示、削除機能

4. **MedicationScreen.kt 実装**（プレースホルダー置換）
   - タイミング別（朝/昼/夕）グループ表示
   - 各薬にステータス表示（未服用/服用済/スキップ）
   - ワンタップで服薬記録、Empty/Loading/Error 状態対応
   - スナックバー通知、削除確認ダイアログ

5. **AddMedicationScreen.kt 実装**（プレースホルダー置換）
   - 薬名入力（必須）、用量入力、タイミング選択チェックボックス
   - 時刻表示、リマインダートグル、保存/キャンセル

6. **MedicationDetailScreen.kt 新規作成**
   - 薬情報カード、タイミングチップ、時刻表示
   - 服薬履歴一覧、削除アクション
   - ナビゲーション: Screen.MedicationDetail ルート追加

7. **専用コンポーネント**
   - `MedicationCard.kt` — ステータスバッジ + アクションボタン
   - `MedicationTimingChip.kt` — 朝/昼/夕色分けチップ

8. **既存バックエンドのテスト追加**
   - `MedicationMapperTest.kt` (8 tests)
   - `MedicationLogMapperTest.kt` (8 tests)
   - `MedicationRepositoryImplTest.kt` (12 tests, MockK)
   - `MedicationLogRepositoryImplTest.kt` (12 tests, MockK)

9. **i18n 文字列追加**
   - 服薬関連 24 文字列追加（JP/EN 両方）

### Phase 2: 共通UIコンポーネント + インフラ整備

1. **UiState パターン** — `sealed class UiState<T>` (Loading, Success, Error) + テスト (20)
2. **共通UIコンポーネント** (6個) — LoadingIndicator, ErrorDisplay, EmptyState, ConfirmDialog, CareNoteCard, CareNoteTextField
3. **ユーティリティ** — DateTimeFormatters (16 tests), ValidationUtils (26 tests)
4. **スナックバー基盤** — SnackbarController (Channel ベース)
5. **i18n** — 共通UI文字列 16 個追加

### Phase 1（前回セッション完了済み）

Clean Architecture 基盤、Hilt DI、Room DB v1、Navigation Compose、Material 3 テーマ

## 変更ファイル

### Phase 4 新規作成（16ファイル）

| ファイル | 内容 |
|---------|------|
| `.../domain/model/Note.kt` | NoteTag enum + Note data class |
| `.../domain/repository/NoteRepository.kt` | Repository interface |
| `.../data/local/entity/NoteEntity.kt` | Room Entity |
| `.../data/local/dao/NoteDao.kt` | DAO (9 メソッド) |
| `.../data/mapper/NoteMapper.kt` | Entity ↔ Domain マッパー |
| `.../data/repository/NoteRepositoryImpl.kt` | Repository 実装 |
| `.../ui/screens/notes/NotesViewModel.kt` | 一覧 + 検索 ViewModel |
| `.../ui/screens/notes/AddEditNoteViewModel.kt` | 追加/編集 ViewModel |
| `.../ui/screens/notes/AddEditNoteScreen.kt` | 追加/編集画面 |
| `.../ui/screens/notes/components/NoteCard.kt` | メモカード |
| `.../ui/screens/notes/components/NoteTagChip.kt` | タグチップ |
| `.../test/.../fakes/FakeNoteRepository.kt` | テスト用 Fake |
| `.../test/.../NoteMapperTest.kt` | Mapper テスト (10) |
| `.../test/.../NoteRepositoryImplTest.kt` | Repo テスト (14) |
| `.../test/.../NotesViewModelTest.kt` | ViewModel テスト (16) |
| `.../test/.../AddEditNoteViewModelTest.kt` | ViewModel テスト (20) |

### Phase 4 変更（8ファイル）

| ファイル | 変更内容 |
|---------|---------|
| `.../data/local/CareNoteDatabase.kt` | NoteEntity 追加, version 1→2 |
| `.../di/DatabaseModule.kt` | provideNoteDao 追加 |
| `.../di/AppModule.kt` | provideNoteRepository 追加 |
| `.../ui/screens/notes/NotesScreen.kt` | プレースホルダー → フル実装 |
| `.../ui/navigation/Screen.kt` | AddNote, EditNote ルート追加 |
| `.../ui/navigation/CareNoteNavHost.kt` | Notes/AddNote/EditNote ルーティング追加 |
| `res/values/strings.xml` | 15 文字列追加 |
| `res/values-en/strings.xml` | 15 文字列追加（英語） |

### Phase 4 その他変更

| ファイル | 変更内容 |
|---------|---------|
| `.../config/AppConfig.kt` | Note オブジェクト追加 |
| `.../ui/theme/Color.kt` | NoteTag 色 4 色追加 |

## テスト結果

- `gradlew.bat testDebugUnitTest` → **235 テスト全パス**
- `gradlew.bat assembleDebug` → ビルド成功
- 0 failures, 0 errors, 0 skipped

| テストクラス | テスト数 | フェーズ |
|------------|---------|---------|
| ResultTest | 24 | Phase 1 |
| DomainErrorTest | 11 | Phase 1 |
| UiStateTest | 20 | Phase 2 |
| DateTimeFormattersTest | 16 | Phase 2 |
| ValidationUtilsTest | 26 | Phase 2 |
| MedicationViewModelTest | 17 | Phase 3 |
| AddMedicationViewModelTest | 20 | Phase 3 |
| MedicationMapperTest | 8 | Phase 3 |
| MedicationLogMapperTest | 8 | Phase 3 |
| MedicationRepositoryImplTest | 12 | Phase 3 |
| MedicationLogRepositoryImplTest | 12 | Phase 3 |
| NoteMapperTest | 10 | Phase 4 |
| NoteRepositoryImplTest | 14 | Phase 4 |
| NotesViewModelTest | 16 | Phase 4 |
| AddEditNoteViewModelTest | 20 | Phase 4 |
| **合計** | **235** | |

## アーキテクチャ変更

```
app/src/main/java/com/carenote/app/
├── config/
│   └── AppConfig.kt                        ← Phase 4 (Note 追加)
├── data/
│   ├── local/
│   │   ├── CareNoteDatabase.kt             ← Phase 4 (v2, NoteEntity 追加)
│   │   ├── dao/NoteDao.kt                  ← Phase 4 (new)
│   │   └── entity/NoteEntity.kt            ← Phase 4 (new)
│   ├── mapper/NoteMapper.kt                ← Phase 4 (new)
│   └── repository/NoteRepositoryImpl.kt    ← Phase 4 (new)
├── di/
│   ├── AppModule.kt                        ← Phase 4 (NoteRepo 追加)
│   └── DatabaseModule.kt                   ← Phase 4 (NoteDao 追加)
├── domain/
│   ├── model/Note.kt                       ← Phase 4 (new)
│   └── repository/NoteRepository.kt        ← Phase 4 (new)
├── ui/
│   ├── theme/Color.kt                      ← Phase 4 (NoteTag 色追加)
│   ├── navigation/
│   │   ├── Screen.kt                       ← Phase 4 (AddNote, EditNote 追加)
│   │   └── CareNoteNavHost.kt              ← Phase 4 (3 ルート追加)
│   └── screens/notes/
│       ├── NotesScreen.kt                  ← Phase 4 (replaced)
│       ├── AddEditNoteScreen.kt            ← Phase 4 (new)
│       ├── NotesViewModel.kt               ← Phase 4 (new)
│       ├── AddEditNoteViewModel.kt         ← Phase 4 (new)
│       └── components/                     ← Phase 4 (new)
│           ├── NoteCard.kt
│           └── NoteTagChip.kt
```

## 次のアクション

1. **Phase 5: 健康記録機能**
   - ドメインモデル: HealthRecord, RecordType
   - データ層: HealthRecordEntity, HealthRecordDao, Mapper, Repository
   - Room Migration 2→3
   - ViewModel: HealthRecordsViewModel, AddHealthRecordViewModel
   - UI: HealthRecordsScreen, AddHealthRecordScreen, RecordCard
   - グラフ表示（体温/血圧推移）
2. **Phase 6 以降**: タスク → カレンダー → 設定 → Firebase → 課金

## 既知の問題

- アプリアイコンはプレースホルダー
- Detekt は CLI ツールとして実行（Gradle プラグインではない）— 環境にインストール要
- ダークモード未対応（ライトテーマのみ）
- TimePicker は未実装（FilterChip で時刻表示のみ、タップで変更不可）
- ViewModel のスナックバーメッセージはハードコードされている（将来的に strings.xml 参照に移行予定）
- Room DB は `fallbackToDestructiveMigration()` 使用中（プレリリースのため）
