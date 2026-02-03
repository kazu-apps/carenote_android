# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## タスク: Item 39-45 全体リファクタリング

## 次のアクション

1. `/task-exec` を繰り返し実行して Item 33-38（テスト品質改善）を進める
2. v1.0 リリース残作業（手動）: スクリーンショット撮影、実機テスト、Google Play ストア提出
3. v2.0: Firebase Auth, Cloud Firestore, FCM, Cloud Storage, Crashlytics

## 既知の問題

### 要対応（未修正 HIGH/CRITICAL）

Item 31 テストレビューで発見。別 Item として対応推奨:

**E2E インフラ:**
- [CRITICAL] DB クリーンアップなし — TestDatabaseModule (in-memory) + @After tearDown が必要
- [CRITICAL] MedicationFlowTest の silent failure — `firstOrNull()?.let{}` が UI 欠落を隠す
- [HIGH] fillTextField に wait guard なし — 画面遷移直後の NoMatchFoundException リスク
- [HIGH] waitForIdle() デフォルトタイムアウト — CI エミュレータで不足の可能性
- [HIGH] NotesFlowTest 検索フィールドが `hasSetTextAction()` のみで曖昧
- [HIGH] CalendarFlowTest 月ナビゲーションにアサーションなし
- [HIGH] TasksFlowTest 3秒タイムアウトが他（5-10秒）より短い

**Fake / テスト不足:**
- [HIGH] FakeMedicationRepository / FakeMedicationLogRepository に shouldFail フラグなし
- [HIGH] FakeSettingsRepository のバリデーションが production と乖離
- [HIGH] SettingsRepositoryImplTest に DB 例外パステストゼロ
- [HIGH] MedicationViewModelTest が UnconfinedTestDispatcher（他11ファイルは Standard）
- [HIGH] MedicationViewModelTest / AddMedicationViewModelTest に失敗テストなし
- [HIGH] HealthRecordsViewModelTest が7件のみ（他は14-16件）

**Production コード:**
- ~~[HIGH] MedicationViewModel — 削除/記録失敗時に Snackbar 未表示~~ → Item 40 で修正済み

**その他:**
- 連絡先メールアドレスが仮値（carenote.app.support@gmail.com）— 公開前に確定が必要
- リリース APK の実機テスト未実施

### 記録のみ（MEDIUM/LOW）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | Item 29 | SettingsDataSource 動的キー生成（Clean Architecture 制約で修正不可） |
| MEDIUM | Item 30 | ValidationUtils.kt が未使用デッドコード（production import ゼロ） |
| MEDIUM | Item 32 | WorkManager/HiltWork 先行実装（Worker クラスなし、v2.0 通知用） |
| MEDIUM | Item 32 | JaCoCo `**/util/*` 除外が広い（テスト自体は存在） |
| MEDIUM | Item 32 | `allowBackup="true"`（データ移行手段として意図的） |
| MEDIUM | Item 31 | テスト品質: Mapper round-trip 不完全、Repository Turbine 未使用、ViewModel Loading→Success テストなし、E2E アサーション曖昧（詳細は git log Item 31 参照） |
| LOW | Item 23 | SettingsDataSource 動的キー（呼び出し元が定数で制限、実害なし） |
| LOW | Item 32 | POST_NOTIFICATIONS 先行宣言（無害） |
| LOW | Item 32 | 依存バージョン 2024年後半（CVE なし、内部整合性あり） |

### 横断的 INFO（全 ViewModel 共通パターン）

- ~~Snackbar 文字列のハードコード（i18n 未対応）~~ → Item 40 で修正済み
- 削除確認ダイアログが UI から到達不能（スワイプ削除用準備コード）
- ~~保存失敗時の UI フィードバック欠如（isSaving リセットのみ）~~ → Item 42 で修正済み
- Flow `.catch` なし（Room Flow は安定、実害リスク低）

## 完了サマリー

### 機能実装（Item 1-7）

| Item | 内容 | Status |
|------|------|--------|
| 1-4 | Clean Architecture 基盤 + 服薬管理 + メモ機能 | DONE |
| 5 | 健康記録（ドメイン+データ+VM+UI+グラフ） | DONE |
| 6 | カレンダー（月表示+イベント管理） | DONE |
| 7 | タスク管理（フィルタ+完了トグル） | DONE |

### リリース準備（Item 8-17）

| Item | 内容 | Status |
|------|------|--------|
| 8 | 設定画面（DataStore + 4セクション） | DONE |
| 9 | Room Migration 戦略（v1→v6） | DONE |
| 10 | カスタムアプリアイコン | DONE |
| 11 | Splash Screen（Android 12+） | DONE |
| 12 | アクセシビリティ監査と改善 | DONE |
| 13 | E2E テスト（17テスト, 6クラス） | DONE |
| 14 | リリースビルド署名設定 | DONE |
| 15 | ProGuard ルール検証 | DONE |
| 16 | プライバシーポリシー・利用規約 | DONE |
| 17 | Google Play ストア掲載情報 | DONE |

### 品質改善（Item 18-21）

| Item | 内容 | Status |
|------|------|--------|
| 18 | バリデーションエラー i18n（UiText sealed class） | DONE |
| 19 | アクセシビリティ改善（フォント+コントラスト WCAG AA） | DONE |
| 20 | 法的文書 asset ファイル移行 | DONE |
| 21 | ダークモード対応（SYSTEM/LIGHT/DARK） | DONE |

### コードレビュー（Item 22-32）

| Item | 内容 | Status |
|------|------|--------|
| 22 | Foundation & DI — CancellationException 修正, Gson 削除 | DONE |
| 23 | Database インフラ — 4 Entity に @Index 追加, MIGRATION_5_6 | DONE |
| 24 | 服薬管理機能 — 問題なし | DONE |
| 25 | メモ機能 — 問題なし | DONE |
| 26 | 健康記録 — チャートステップ値 AppConfig 移行 | DONE |
| 27 | カレンダー — TimePicker デフォルト AppConfig 移行 | DONE |
| 28 | タスク管理 — TaskMapper try-catch, PriorityBadge テーマ色 | DONE |
| 29 | 設定 — 時間バウンド定数 AppConfig 移行 | DONE |
| 30 | 共通 UI・テーマ — コード変更なし（INFO のみ） | DONE |
| 31 | テスト品質 — フォールバックテスト+アサーション強化 | DONE |
| 32 | ビルド設定 — Gson 依存+ProGuard ルール削除 | DONE |

## 全体リファクタリング（Item 39-45） - 全 DONE

### Item 39: STOP_TIMEOUT_MS の AppConfig.UI 集約 - DONE
8つの ViewModel の `STOP_TIMEOUT_MS = 5_000L` を `AppConfig.UI.FLOW_STOP_TIMEOUT_MS` に集約。

### Item 40: Snackbar メッセージの strings.xml 移行 - DONE
SnackbarEvent を sealed interface に変更（WithResId/WithString）。17個のハードコード日本語メッセージを strings.xml（JP/EN）に移行。全7 ViewModel + 7 Screen + 4 テストファイルを更新。

### Item 41: SettingsViewModel バリデーションエラー i18n - DONE
5箇所の `error.message` 表示を `R.string.settings_error_validation` に統一。

### Item 42: AddEdit ViewModel に保存失敗 Snackbar 追加 - DONE
5つの AddEdit ViewModel に SnackbarController を追加し、save 失敗時にエラー Snackbar を表示。5つの AddEdit Screen に SnackbarHost を追加。

### Item 43: MedicationDetailViewModelTest 新規作成 - DONE
6テストケース（初期状態、詳細取得成功、NotFound エラー、ログ取得、削除成功、削除失敗）を新規作成。

### Item 44: AddEditHealthRecordScreen 分割 - DONE
VitalSignsInputSection.kt と LifestyleInputSection.kt を components/ に抽出。

### Item 45: savedTask → savedEvent リネーム - DONE
AddEditTaskViewModel の `savedTask` を他 ViewModel と同じ `savedEvent` に統一。

---

## テスト品質改善ロードマップ（Item 31 未修正分）

Item 31 テストレビューで発見された未修正 HIGH/CRITICAL を対応。

### Item 33: Fake shouldFail 統一 + MedicationViewModel Production 修正 - PENDING
Fake Repository に shouldFail フラグを追加し、MedicationViewModel の失敗時 Snackbar 欠如を修正。
- 種別: 実装
- 対象: FakeMedicationRepository, FakeMedicationLogRepository, FakeSettingsRepository（バリデーション乖離修正）, MedicationViewModel（onFailure に Snackbar 追加）
- 変更ファイル: ~4
- 依存: なし

### Item 34: MedicationViewModelTest 改善 - PENDING
UnconfinedTestDispatcher を StandardTestDispatcher に統一し、失敗パステストを追加。
- 種別: テスト
- 対象: MedicationViewModelTest（Dispatcher 変更 + recordMedication/deleteMedication 失敗テスト追加）
- 変更ファイル: 1
- 依存: Item 33（shouldFail フラグ必要）

### Item 35: AddMedicationViewModelTest + HealthRecordsViewModelTest 補完 - PENDING
テストカバレッジが不足している 2 ViewModel のテストを拡充。
- 種別: テスト
- 対象: AddMedicationViewModelTest（save 失敗テスト追加）, HealthRecordsViewModelTest（7件→15件程度に拡充）
- 変更ファイル: 2
- 依存: Item 33（shouldFail フラグ必要）

### Item 36: SettingsRepositoryImplTest DB 例外パス追加 - PENDING
DataStore 例外発生時の挙動テストを追加。現在バリデーションテストのみで DB 例外パスがゼロ。
- 種別: テスト
- 対象: SettingsRepositoryImplTest（MockK で DataStore 例外をスロー → DomainError.DatabaseError 検証）
- 変更ファイル: 1
- 依存: なし

### Item 37: E2E インフラ改善 - PENDING
E2E テスト基盤の CRITICAL/HIGH 問題を修正。DB クリーンアップ、wait guard、silent failure 対策。
- 種別: テスト
- 対象: E2eTestBase（fillTextField wait guard, waitForIdle タイムアウト設定）, MedicationFlowTest（silent failure 修正 — firstOrNull → first + 明示的アサーション）, DB クリーンアップ基盤（@After tearDown）
- 変更ファイル: ~3
- 依存: なし
- 注意: E2E テスト実行にはエミュレータが必要。ビルド成功のみ CLI で検証可能

### Item 38: E2E テスト品質改善 - PENDING
個別 E2E テストの HIGH 問題を修正（マッチャー改善、アサーション追加、タイムアウト統一）。
- 種別: テスト
- 対象: NotesFlowTest（検索フィールドマッチャー改善）, CalendarFlowTest（月ナビゲーションアサーション追加）, TasksFlowTest（タイムアウト 3秒→5秒に統一）
- 変更ファイル: 3
- 依存: Item 37（E2E インフラ改善後）

---

## 実行順序（テスト品質改善ロードマップ）

| 順番 | Item | 内容 | 依存 |
|------|------|------|------|
| 1 | 33 | Fake shouldFail + MedicationVM 修正 | なし |
| 2 | 36 | SettingsRepositoryImplTest 例外パス | なし |
| 3 | 34 | MedicationViewModelTest 改善 | Item 33 |
| 4 | 35 | AddMedicationVMTest + HealthRecordsVMTest 補完 | Item 33 |
| 5 | 37 | E2E インフラ改善 | なし |
| 6 | 38 | E2E テスト品質改善 | Item 37 |

**並行実行可能**: Item 33, 36, 37 は独立して着手可能

---

## アーキテクチャ参照

| カテゴリ | 値 |
|---------|-----|
| Room DB バージョン | v6（Migration v1→v2→v3→v4→v5→v6） |
| エラー i18n | `UiText.Resource` / `UiText.ResourceWithArgs` sealed class |
| テーマ色参照 | `CareNoteColors.current.xxxColor`（ハードコード Color() 禁止） |
| 定数管理 | `AppConfig` オブジェクト（マジックナンバー禁止） |
| enum パース | try-catch + フォールバック（NoteMapper, HealthRecordMapper, TaskMapper 統一済み） |
| Snackbar i18n | `SnackbarEvent` sealed interface（WithResId / WithString）。ViewModel は `R.string.xxx` を使用 |
| テストパターン | StandardTestDispatcher + Turbine + FakeRepository（MutableStateFlow） |

## スコープ外 / 将来

- **v2.0**: Firebase Auth, Cloud Firestore, FCM, Cloud Storage, Crashlytics
- **手動作業**: スクリーンショット撮影、フィーチャーグラフィック作成、プライバシーポリシー Web ホスティング
- **LegalDocumentScreen テスト**: 純粋表示コンポーネント、ロジックなし
