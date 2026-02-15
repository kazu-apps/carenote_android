# HANDOVER.md - CareNote Android

## セッションステータス: v9.0 Phase 4 (家族招待データモデル + Room) 完了

## 現在のタスク: v9.0 Phase 4 完了。Phase 5 (招待 UI + フロー) から実行可能

Round 2 完了：researcher 調査 (v19 DB確認、No-Op実装確認) → architect 提案 (6 Phase Plan) → critic リスク分析 (6リスク指摘) → researcher 相互レビュー (誤認6点、見落とし3点指摘)。

修正内容:
- **DB version**: v20 ではなく v21 migration が必須（v20 は既に消費済み）
- **EntitySyncer**: ConfigDrivenEntitySyncer では Member/Invitation 実装不可。手動 Syncer が必要
- **Dynamic Links**: 廃止リスク過大評価（現在使用なし。App Links で代替可）
- **User.isPremium**: User モデルと PremiumFeatureGuard の責務分離を明記
- **実装順**: Billing → 家族招待 → 通知制限（critic 提案と逆順が最適）
- **フェーズ分割**: 1-6 Phase に細分化。Phase 1B (Cloud Functions) は Claude Code 守備範囲外

## 次のアクション

- v9.0 Phase 5: 家族招待 — 招待 UI + 招待フロー（InvitationScreen, MemberManagementScreen, App Links）
- v9.0 Phase 1B: Billing サーバーサイド検証 (Cloud Functions) — Claude Code 守備範囲外、手動作業
- v9.0 Phase 6: 統合テスト + E2E（Phase 5 完了後）

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (`support@carenote.app`) — リリース前に実アドレス確定必要
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | v4.0 | Rate Limiting 未実装（API エンドポイント、バックエンド依存） |
| LOW | v2.0 | FCM トークンのサーバー送信未実装（バックエンド前提） |
| LOW | v10.0-tdd | SettingsViewModelTest 1170 行（Detekt 対象外だが可読性の観点で将来的に分割検討） |

## ロードマップ

### v9.0-sec Phase 1: データ保護 + Firestore Rules - DONE

Firestore Security Rules 新規作成、ExceptionMasker/SecureFileDeleter 新規ユーティリティ、8 Exporter キャッシュ上書き削除、EntitySyncer/MedicationLogSyncer/FirebaseStorageRepositoryImpl PII ログマスク。テスト 35 件追加。全ビルド・テスト通過。

### v9.0-sec Phase 2A: Session タイムアウト + Derived Key - DONE

Session タイムアウト user-configurable（1-60分、デフォルト5分）、PBKDF2WithHmacSHA256 derived key（100K iterations, 256-bit）、master passphrase ゼロクリア。テスト ~16 件追加。全ビルド・テスト通過。
- 対象: `config/AppConfig.kt`, `domain/model/UserSettings.kt`, `data/local/SettingsDataSource.kt`, `domain/repository/SettingsRepository.kt`, `data/repository/SettingsRepositoryImpl.kt`, `ui/MainActivity.kt`, `data/local/DatabasePassphraseManager.kt`, `ui/screens/settings/SettingsViewModel.kt`, `res/values/strings.xml`, `res/values-en/strings.xml`
- テスト: DatabasePassphraseManagerTest (8件) + SettingsRepositoryImplTest (5件) + SettingsViewModelTest (3件)
- 依存: Phase 1

### v9.0-sec Phase 2B: 入力検証パターン統一 - DONE

domain/validator/ に InputValidator, HealthRecordValidator, MedicationValidator, RecurrenceValidator, SettingsValidator を新設。FormValidator/AuthValidators は UI ラッパーとして残し domain/validator に委譲。CareRecipientViewModel にバリデーション追加。SettingsRepositoryImpl/FakeSettingsRepository の検証ロジックを SettingsValidator に統一。テスト 58 件追加。全ビルド・テスト通過。
- 対象: `domain/validator/` (新規 5 ファイル), `config/AppConfig.kt`, `ui/common/UiText.kt`, `ui/util/FormValidator.kt`, `ui/screens/auth/AuthValidators.kt`, `ui/screens/medication/AddEditMedicationViewModel.kt`, `ui/screens/calendar/AddEditCalendarEventViewModel.kt`, `ui/screens/tasks/AddEditTaskViewModel.kt`, `ui/screens/carerecipient/CareRecipientViewModel.kt`, `data/repository/SettingsRepositoryImpl.kt`, `fakes/FakeSettingsRepository.kt`, `res/values/strings.xml`, `res/values-en/strings.xml`
- テスト: InputValidatorTest (22件) + HealthRecordValidatorTest (8件) + MedicationValidatorTest (6件) + RecurrenceValidatorTest (5件) + SettingsValidatorTest (14件) + CareRecipientViewModelTest (+3件)
- 依存: Phase 2A

### v9.0-sec Phase 3: バイナリ保護 + APPI 準拠 - DONE

DomainError.SecurityError 新設 + SyncWorker/ErrorDisplay の when 式更新。RootDetectionChecker を DI 登録、3 ViewModel（Settings, HealthRecords, Medication）+ FirebaseStorageRepo のエクスポート/アップロードを Root 時ブロック。MainActivity Root 検出時セッションタイムアウト短縮 (60秒)。ProGuard ルール強化（WorkManager, Paging, Security-Crypto, Biometric）。strings.xml JP/EN 4文言追加（security_root_export_blocked, security_root_upload_blocked, security_root_warning_dialog_message_restricted, ui_error_security）。APPI 準拠ドキュメント（docs/SECURITY.md, docs/DATA_RETENTION_POLICY.md）。docs/RELEASE_CHECKLIST.md セキュリティ項目拡充。テスト 12 件追加（SettingsVM 8件, HealthRecordsVM 2件, MedicationVM 2件）+ DomainErrorTest SecurityError 対応。全ビルド・テスト通過。
- 対象: `domain/common/DomainError.kt`, `data/worker/SyncWorker.kt`, `ui/components/ErrorDisplay.kt`, `config/AppConfig.kt`, `di/AppModule.kt`, `ui/screens/settings/SettingsViewModel.kt`, `ui/screens/healthrecords/HealthRecordsViewModel.kt`, `ui/screens/medication/MedicationViewModel.kt`, `data/repository/FirebaseStorageRepositoryImpl.kt`, `ui/MainActivity.kt`, `proguard-rules.pro`, `res/values/strings.xml`, `res/values-en/strings.xml`, `docs/SECURITY.md` (新規), `docs/DATA_RETENTION_POLICY.md` (新規), `docs/RELEASE_CHECKLIST.md`, `test/.../DomainErrorTest.kt`, 3 test files (SettingsVM, HealthRecordsVM, MedicationVM)
- 依存: Phase 1, Phase 2A, Phase 2B

### v9.0-test Phase 1: テストユーティリティ基盤構築 - DONE

testing/ パッケージに共通テストユーティリティ新設。TestDataFixtures（FakeClock統合定数）、TestBuilders（7モデルファクトリ関数: aMedication/aNote/aTask/aHealthRecord/aMedicationLog/aNoteComment/aCalendarEvent）、ResultMatchers（Result<T,E> extension function アサーション 10種）。テスト 26 件追加。全ビルド・テスト通過。
- 対象: `app/src/test/java/com/carenote/app/testing/` (新規 5 ファイル: TestDataFixtures.kt, TestBuilders.kt, ResultMatchers.kt, TestBuildersTest.kt, ResultMatchersTest.kt)
- 依存: なし

### v9.0-test Phase 2: テストデータ統一 + E2E デバッグ改善 - DONE

Mapper/Exporter テスト 16 ファイルのハードコード日時（"2025-03-15T10:00:00" / LocalDateTime.of(2025,3,15,10,0)）を TestDataFixtures.NOW / NOW_STRING に統一。E2E テスト失敗時の Screenshot 自動保存（TestWatcher）を E2eTestBase に追加。全ビルド・テスト通過。
- 対象: TestDataFixtures.kt（NOW_STRING/TODAY_STRING 追加）、Local Mapper テスト 10 ファイル + Exporter テスト 1 ファイル（worker-impl）、Remote Mapper テスト 7 ファイル + FirestoreTimestampConverterTest + E2eTestBase（worker-test）
- 依存: Phase 1

### v9.0-test Phase 3: カバレッジ向上 + ドキュメント - DONE

MedicationLogSyncerTest 新規作成（サブコレクション同期の専用テスト ~18件）。HomeViewModelTest にエラーシナリオ ~4件追加（Flow 例外の .catch ブロック検証）。CareRecipientViewModelTest にエラーシナリオ ~3件追加（save failure 時のフォーム保持検証）。CLAUDE.md 落とし穴 #22（テスト開発 Best Practices）追記。全ビルド・テスト通過。
- 対象: `app/src/test/.../sync/MedicationLogSyncerTest.kt` (新規), `app/src/test/.../home/HomeViewModelTest.kt`, `app/src/test/.../carerecipient/CareRecipientViewModelTest.kt`, `CLAUDE.md`
- 依存: Phase 2

### v10.0-tdd Phase 1: MainCoroutineRule + TestBuilders 拡充 - DONE

MainCoroutineRule（JUnit 4 TestWatcher, StandardTestDispatcher デフォルト）新設。TestBuilders に aUser/aCareRecipient/aEmergencyContact/aUserSettings 4 ビルダー追加。MainCoroutineRuleTest 6 件 + TestBuildersTest 12 件追加。全ビルド・テスト通過。
- 対象: `testing/MainCoroutineRule.kt` (新規), `testing/MainCoroutineRuleTest.kt` (新規), `testing/TestBuilders.kt`, `testing/TestBuildersTest.kt`
- 依存: なし

### v10.0-tdd Phase 2: RepositoryImpl テスト修正 - DONE

HealthRecordRepositoryImplTest, NoteRepositoryImplTest, TaskRepositoryImplTest の createEntity() に careRecipientId/createdBy 追加。TestDataFixtures.NOW_STRING 統一。Domain 直接作成→TestBuilders 置換。assertTrue→ResultMatchers 統一。全 46 テスト通過。
- 対象: `data/repository/HealthRecordRepositoryImplTest.kt`, `data/repository/NoteRepositoryImplTest.kt`, `data/repository/TaskRepositoryImplTest.kt`
- 依存: Phase 1

### v10.0-tdd Phase 3A: ViewModel テスト移行（StandardTestDispatcher 16 ファイル） - DONE

16 ViewModel テストを MainCoroutineRule に移行。@Before/@After の Dispatcher 手動管理を削除。7 ファイルで domain createXxx() ヘルパーを TestBuilders に置換。全テスト通過。
- 対象: HomeViewModelTest, MedicationViewModelTest, MedicationDetailViewModelTest, AddEditMedicationViewModelTest, AddEditTaskViewModelTest, AddEditNoteViewModelTest, CalendarViewModelTest, AddEditCalendarEventViewModelTest, HealthRecordsViewModelTest, AddEditHealthRecordViewModelTest, HealthRecordGraphViewModelTest, CareRecipientViewModelTest, SettingsViewModelTest, SettingsViewModelUpdateTest, AuthViewModelTest, SearchViewModelTest
- Group A (worker-impl): HomeViewModelTest, MedicationViewModelTest, MedicationDetailViewModelTest, AddEditMedicationViewModelTest, CalendarViewModelTest, AddEditCalendarEventViewModelTest, HealthRecordsViewModelTest, AddEditHealthRecordViewModelTest
- Group B (worker-test): HealthRecordGraphViewModelTest, CareRecipientViewModelTest, SettingsViewModelTest, SettingsViewModelUpdateTest, AuthViewModelTest, SearchViewModelTest, AddEditTaskViewModelTest, AddEditNoteViewModelTest
- 依存: Phase 1

### v10.0-tdd Phase 3B: ViewModel テスト移行（UnconfinedTestDispatcher 5 ファイル + 残り） - DONE

UnconfinedTestDispatcher 使用の 5 ファイルに MainCoroutineRule(UnconfinedTestDispatcher()) 適用。PhotoManagerTest, LoginFormHandlerTest, RegisterFormHandlerTest, ForgotPasswordFormHandlerTest にも MainCoroutineRule 適用。TestBuilders 採用。@Before/@After の Dispatcher 手動管理を削除。全 9 ファイル・全テスト通過。ビルド成功。
- 対象: TasksViewModelTest, NotesViewModelTest, EmergencyContactListViewModelTest, AddEditEmergencyContactViewModelTest, TimelineViewModelTest, PhotoManagerTest, LoginFormHandlerTest, RegisterFormHandlerTest, ForgotPasswordFormHandlerTest
- 依存: Phase 3A

### v10.0-tdd Phase 4: ResultMatchers 全面採用 - DONE

残り 8 RepositoryImpl テストに ResultMatchers 適用。assertTrue(result is Result.Success) → result.assertSuccess() パターンに統一。SyncResult 用 matchers 3関数追加（assertSyncSuccess/assertSyncFailure/assertSyncPartialSuccess）。全テスト通過。
- 対象: ResultMatchers.kt, SettingsRepositoryImplTest, PhotoRepositoryImplTest, FirestoreSyncRepositoryImplTest, NoteCommentRepositoryImplTest, CalendarEventRepositoryImplTest, MedicationLogRepositoryImplTest, CareRecipientRepositoryImplTest, EmergencyContactRepositoryImplTest
- 依存: Phase 2

### v7.0 Phase 1: リリース品質強化 - DONE

ProGuard ルール追加（Firebase Analytics + Coil 3.x）、RELEASE_CHECKLIST.md 拡張。Debug/Release ビルド + UT 全通過確認済み。
- 対象: `app/proguard-rules.pro`, `docs/RELEASE_CHECKLIST.md`

### v7.0 Phase 2: エクスポート基盤 + MedicationLog エクスポート - DONE

MedicationLog CSV/PDF エクスポート実装。ExportState 共有化、domain interface、Exporter 2 件、ViewModel export 機能、DI、i18n、テスト。共通ユーティリティ抽出は Phase 3 に延期（YAGNI）。
- 対象: `data/export/`, `domain/repository/`, `di/AppModule.kt`, `config/AppConfig.kt`, `ui/viewmodel/`, `ui/screens/medication/`, `res/values/strings.xml`, `res/values-en/strings.xml`
- 依存: Phase 1

### v7.0 Phase 3: Task/Note エクスポート + エクスポート設定画面 - DONE

Task/Note の CSV/PDF エクスポートを Phase 2 パターン踏襲で実装。Settings に DataExportSection + DataExportDialog（format+period 選択）追加。Exporter 4 件 + ViewModel export + DI + i18n + テスト。
- 対象: `data/export/`, `domain/repository/`, `ui/screens/settings/`, `config/AppConfig.kt`, `di/AppModule.kt`, `res/values/strings.xml`, `res/values-en/strings.xml`
- 依存: Phase 2

### v7.0 Phase 4: クロスモジュール検索 - DONE

全 6 エンティティ横断検索。SearchResult sealed class + SearchRepositoryImpl（6 Repository inject + combine(3,3)）+ SearchScreen + SearchViewModel（debounce 300ms + flatMapLatest）。5 コンテンツ画面の TopAppBar に Search アイコン追加。
- 対象: `domain/model/SearchResult.kt`, `domain/repository/SearchRepository.kt`, `data/repository/SearchRepositoryImpl.kt`, `ui/screens/search/`, `ui/navigation/`, `config/AppConfig.kt`, 5 コンテンツ Screen, `res/values/strings.xml`, `res/values-en/strings.xml`
- テスト: SearchRepositoryImplTest (~10 件) + SearchViewModelTest (8 件) + FakeSearchRepository

### v7.0 Phase 5: テスト強化 + Roborazzi 拡充 - DONE

SearchScreen（SearchContent 抽出 + 2 Preview）、DataExportDialog（1 Preview）、DataExportSection（1 Preview）に @LightDarkPreview 追加。PreviewData に searchResults 追加。新規 golden image 8 枚（合計 50 枚）。全ビルド + テスト + Roborazzi 通過。
- 対象: `ui/screens/search/SearchScreen.kt`, `ui/screens/settings/dialogs/DataExportDialog.kt`, `ui/screens/settings/sections/DataExportSection.kt`, `ui/preview/PreviewData.kt`, `app/src/test/snapshots/`
- 依存: Phase 2-4 完了後

### v7.0 Phase 6: CLAUDE.md 包括更新 + ドキュメント整備 - DONE

CLAUDE.md を実コードに完全同期（Secondary Screen 名修正、DI/Repository/Model カウント、search/export ディレクトリ、Fake リスト、落とし穴 #20 追加）。HANDOVER.md 完了項目に v7.0 圧縮追加。
- 対象: `CLAUDE.md`, `docs/HANDOVER.md`
- 依存: Phase 5 完了後

## v8.0 ロードマップ（MVP-First 戦略）

### 仕様書 vs 実装 乖離サマリー（v8.0 Phase 3 完了時点で再検証済み）

v8.0 で解消済み: ホーム画面（Phase 1）、CareRecipient 4フィールド（Phase 1）、CalendarEvent type/completed（Phase 2）
残存・重大（コンセプト影響）: 家族共有・マルチユーザー未実装、全 Entity に recipientId/createdBy なし
残存・中程度（機能差異）: CalendarEvent recurrence 欠落、メモコメントなし、オンボーディングなし
対応不要: 緊急連絡先カテゴリ（実装が仕様より詳細化で互換性あり）、通知プレミアム制限（Billing 未実装で無意味）
良い方向の進化: ローカルファースト設計、SQLCipher、生体認証、Root 検出、CSV/PDF エクスポート、横断検索、ウィジェット

### v8.0 Phase 1: ホーム画面 + CareRecipient 拡張 - DONE

HomeViewModel (6 flows combine) + HomeScreen (5 sections) + CareRecipient 4 フィールド追加 + BottomNav 変更 + DB v15 + テスト 19 件追加。全 1494+ テスト通過。
- 対象: `ui/screens/home/`, `domain/model/CareRecipient.kt`, `data/local/entity/`, `ui/navigation/`, `config/AppConfig.kt`, `res/values/strings.xml`
- 依存: なし

### v8.0 Phase 2: カレンダー拡張（type, completed） - DONE

CalendarEvent に type (HOSPITAL/VISIT/DAYSERVICE/OTHER) と completed フラグを追加。イベント種別アイコン表示 + 完了チェック UI。DB v16。CalendarViewModelTest 4 件 + AddEditCalendarEventViewModelTest 6 件追加。全テスト通過。
- 対象: `domain/model/`, `data/local/entity/`, `data/mapper/`, `ui/screens/calendar/`, `ui/screens/home/`, `config/AppConfig.kt`, `res/values/strings.xml`
- 依存: Phase 1

### v8.0 Phase 3: テスト + CLAUDE.md 更新 - DONE

CalendarEventMapper/RemoteMapper type/completed テスト 11 件 + HomeScreen/CareRecipientScreen Content 抽出 + @LightDarkPreview + CLAUDE.md 同期（DB v16, BottomNav, model 19, パッケージ構成）。Roborazzi 新規 ~6 枚追加。
- 対象: `app/src/test/`, `ui/screens/home/`, `ui/screens/carerecipient/`, `ui/preview/`, `CLAUDE.md`, `docs/HANDOVER.md`
- 依存: Phase 2

### v8.1 Phase 4: マルチユーザー基盤 — recipientId 追加 - DONE

全 8 Entity（Medication, MedicationLog, Note, HealthRecord, CalendarEvent, Task, Photo, EmergencyContact）に care_recipient_id カラム追加。全 DAO に WHERE care_recipient_id = :id フィルター追加。Mapper 8 件の recipientId マッピング追加。Repository 実装に recipientId パラメータ追加。Firestore 同期パスは既に CareRecipient ベース構造のため変更不要。DB migration v16→v17（ALTER TABLE × 8 + INDEX × 8、DEFAULT "1"）。Migration UT 必須。
- 対象: `data/local/entity/` (8 Entity), `data/local/dao/` (8 DAO, 15-20 クエリ修正), `data/mapper/` (8 Mapper), `data/repository/` (Repository 実装), `domain/model/` (8 Model)
- ファイル数: 20-25
- 依存: Phase 3（v8.0 完了後）

### v8.1 Phase 5: createdBy 統一 + オンボーディング - DONE

Note.authorId→createdBy リネーム + HealthRecord/Task に createdBy 追加。3 RepositoryImpl に AuthRepository inject（insert 時 createdBy 自動設定）。NoteRemoteMapper "authorId" レガシーフォールバック。OnboardingWelcomeScreen 新規作成。SettingsDataSource/Repository に onboarding_completed 追加。MainActivity startDestination 3分岐。DB v18。i18n JP/EN 追加。全テスト通過。
- 対象: `domain/model/` (3 Model), `data/local/entity/` (3 Entity), `data/mapper/` (3 Mapper + 3 RemoteMapper), `data/repository/` (3 RepositoryImpl + SettingsRepositoryImpl), `di/AppModule.kt`, `data/local/SettingsDataSource.kt`, `domain/repository/SettingsRepository.kt`, `ui/screens/onboarding/OnboardingWelcomeScreen.kt` (新規), `ui/screens/carerecipient/` (Screen + ViewModel), `ui/navigation/` (Screen.kt + CareNoteNavHost.kt), `ui/MainActivity.kt`, `ui/preview/PreviewData.kt`, `res/values/strings.xml`, `res/values-en/strings.xml`, テスト 10+ 件
- 依存: Phase 4

### v8.1 Phase 6: メモコメント + カレンダー recurrence - DONE

NoteComment 1:N 新規追加（Entity+DAO+Mapper+RemoteMapper+Repository+UI）+ CalendarEvent recurrence 追加（RecurrenceExpander+UI）。DB v19。DI/Sync 統合。テスト 44 件追加。全ビルド・テスト通過。
- 対象: `domain/model/` (NoteComment 新規, CalendarEvent 拡張), `data/local/` (Entity + DAO + DB v19), `data/repository/`, `data/mapper/`, `ui/screens/notes/`, `ui/screens/calendar/`, `di/`, `domain/util/RecurrenceExpander.kt`
- 依存: Phase 5

### v8.1 Phase 7: テスト強化 + CLAUDE.md 更新 - DONE

AddEditCalendarEventViewModelTest recurrence 12 件、AddEditNoteViewModelTest comment 10 件、CalendarEventRepositoryImplTest RecurrenceExpander 3 件追加。CLAUDE.md Phase 4-6 同期（DB v19、model 20、Repository 25、NoteComment/ActiveCareRecipientProvider/RecurrenceExpander/OnboardingWelcome 追加）。全ビルド・テスト通過。
- 対象: `app/src/test/`, `CLAUDE.md`, `docs/HANDOVER.md`
- 依存: Phase 6

## v7.0 リサーチサマリー

### コードベース状況（v6.0 完了時点）
- プロダクションコード: 25,877 行 / テストコード: 22,928 行 + E2E 18 ファイル
- JaCoCo 80% カバレッジ達成、Detekt maxIssues=0
- 全 8 機能モジュール 🟢 Mature、TODO/FIXME/HACK ゼロ
- ProGuard 71 行（包括的だが網羅性検証未実施）
- CI/CD: GitHub Actions 完備（Build + UT + Roborazzi + Detekt + E2E）

### 設計方針
- **Client サイドのみで完結する機能に特化**（FCM リモート通知、Wear OS、Billing はバックエンド/別モジュール依存のためスコープ外）
- **エクスポート拡充**: 既存 HealthRecord パターン（CsvExporter/PdfExporter + FileProvider）を踏襲。共通ユーティリティ先行抽出
- **検索**: Secondary Screen として実装。BottomNav 6 タブ維持（Material Design 3 制約: 3-5 推奨、7 タブは高齢者 UX 悪化）
- **CSV インポートはスコープ外**: 対象ユーザー（家族介護者・高齢者）に CSV フォーマット理解を前提とする機能は不適切と判断

### リスク・注意事項
- エクスポートファイルに PII（患者情報）を含む → キャッシュ管理、ログ PII 禁止ルール遵守
- Exporter インターフェース爆発防止 → 汎用 Base 設計を Phase 2 で先行
- SearchRepository の複数テーブル検索 → Detekt CyclomaticComplexity (15) 超過リスク → 関数分割で対応
- i18n: 各フェーズで strings.xml JP/EN ペア更新必須

## PENDING 項目

（v6.0 完了済み項目は「完了タスク」表に圧縮済み）

## 完了タスク

| Item | 概要 | Status |
|------|------|--------|
| v1.0 1-17 | Clean Architecture + 5機能 + リリース準備 | DONE |
| v1.0 18-21 | 品質改善（i18n, A11y, ダークモード） | DONE |
| v1.0 22-53 | コードレビュー + テスト強化 + セキュリティ修正 | DONE |
| v2.0 55-78 | Firebase Auth + Firestore 同期 + FCM + Crashlytics | DONE |
| v2.0 79-81 | セキュリティ強化（PII マスク、メール検証） | DONE |
| v2.2 82-102 | TDD リファクタリング（Syncer, Settings, Auth, コード品質） | DONE |
| v3.0 Ph1-10 | バグ修正 CRITICAL 4件 + ランタイム修正 + collectAsStateWithLifecycle | DONE |
| v3.0 Ph11-24 | 服薬リマインダー + タスク繰り返し + デッドコード + テスト品質 | DONE |
| v3.0 Ph25-35 | 依存関係アップグレード + 編集画面 + 検索 + アカウント + Dynamic Color | DONE |
| v4.0 Ph1-5 | CI/CD + targetSdk 36 + R8 full + PII + Layer boundary | DONE |
| v4.0 Ph6-10 | Migration squash + Incremental Sync + Paging 3 | DONE |
| v4.0 Ph11-17 | Badge + グラフ a11y + タイムライン + 緊急連絡先 + 在庫管理 | DONE |
| v4.0 Ph18-20 | Roborazzi + Macrobenchmark + E2E テスト | DONE |
| v4.0 Ph21-25 | Root 検出 + Compose 最適化 + Glance Widget + CLAUDE.md | DONE |
| v5.0 Ph1-6 | TDD リファクタリング（Clock, HealthMetricsParser, Scaffold, FormValidator, PhotoManager） | DONE |
| v6.0 Ph1 | Root ダイアログ改善 + 問い合わせ機能 + RELEASE_CHECKLIST | DONE |
| v6.0 Ph2 | E2E テスト拡充（CRUD/Edit/Delete/Validation 17テスト） | DONE |
| v6.0 Ph3-3b | Firebase Analytics 基盤 + ViewModel イベント送信（18 VM + 30+ イベント） | DONE |
| v6.0 Ph4 | パフォーマンス最適化（SettingsScreen LazyColumn key） | DONE |
| v6.0 Ph5 | CLAUDE.md 包括更新 + ドキュメント整備 | DONE |
| v7.0 Ph1-6 | ProGuard 強化 + エクスポート拡充（MedicationLog/Task/Note CSV/PDF）+ クロスモジュール検索 + Roborazzi 拡充 + CLAUDE.md 同期 | DONE |
| v8.0 Ph1 | ホーム画面（5 セクション）+ CareRecipient 4 フィールド + BottomNav 変更 + DB v15 + テスト 19 件 | DONE |
| v8.0 Ph2 | CalendarEvent type/completed 追加 + 種別アイコン + 完了チェック UI + DB v16 + テスト 10 件追加 | DONE |
| v8.0 Ph3 | CalendarEventMapper テスト 11 件 + HomeScreen/CareRecipientScreen Content 抽出 + Preview + CLAUDE.md 同期 | DONE |
| v8.0 仕様検証 | 仕様書 vs 実装 全11項目検証。3件解消確認、8件残存（v8.1 でカバー）、2件対応不要判定、Phase 7 を v9.0 先送り | DONE |
| v8.1 Ph4 | 全8 Entity/DAO/Model/Mapper/RepositoryImpl に care_recipient_id 追加。ActiveCareRecipientProvider パターン導入。SyncModule 修正。DB v16（fallbackToDestructiveMigration）。全テスト通過 | DONE |
| v8.1 Ph5 | Note.authorId→createdBy + HealthRecord/Task createdBy 追加。3 RepositoryImpl AuthRepository inject。NoteRemoteMapper authorId レガシーフォールバック。OnboardingWelcomeScreen + SettingsDataSource onboarding_completed + MainActivity 3分岐。DB v18。全テスト通過 | DONE |
| v8.1 Ph6 | NoteComment 1:N（Entity+DAO+Mapper+RemoteMapper+Repository+UI）+ CalendarEvent recurrence（RecurrenceExpander+UI）。DB v19。DI/Sync 統合。テスト 44 件追加。全ビルド・テスト通過 | DONE |
| v8.1 Ph7 | テスト強化（recurrence 12件 + comment 10件 + RecurrenceExpander 3件）+ CLAUDE.md Phase 4-6 同期。全ビルド・テスト通過 | DONE |
| v9.0-sec Ph2A | Session タイムアウト user-configurable（1-60分）+ PBKDF2 derived key + master passphrase ゼロクリア。テスト 16 件追加。全テスト通過 | DONE |
| v9.0-sec Ph2B | domain/validator/ 新設5ファイル + UI/Data 層委譲 + CareRecipientViewModel バリデーション + テスト 58 件。全テスト通過 | DONE |
| v9.0-sec Ph3 | DomainError.SecurityError + RootDetectionChecker DI + 3VM Root ブロック + ProGuard 強化 + APPI ドキュメント + テスト 12 件。全テスト通過 | DONE |
| task-driver v8 | SKILL.md + team-templates.md 全面書き換え（TeamCreate ハイブリッド）+ CLAUDE.md sub-agent-patterns 原則追加 + MEMORY.md 更新 | DONE |
| task-driver v8 レビューラウンド | Plan モード Round 2 相互レビュー追加。SKILL.md（手順 Round 1/Round 2 構造化 + Rule #18）+ team-templates.md（レビューテンプレート 3 件） | DONE |
| テスト機能リサーチ | テスト基盤調査（126 unit + 22 E2E + 31 Fakes + 56 Roborazzi）、Builder DSL 方針策定、リスク分析（Flaky LOW、E2E screenshot MEDIUM、日時統一 MEDIUM）、3 Phase ロードマップ作成 | DONE |
| セキュリティ分析 | OWASP Mobile Top 10 + 攻撃ベクター + APPI 準拠評価。成熟度 93/100。CRITICAL 1 (Firestore Rules), HIGH 5 (Export PII, Sync PII, Session timeout, Input validation, Biometric memory dump), MEDIUM 5。総工数 107h。3 Phase ロードマップ作成 | DONE |
| v9.0-sec Ph1 | データ保護 + Firestore Rules（ExceptionMasker/SecureFileDeleter、8 Exporter cache cleanup、Sync PII マスク、firestore.rules）。テスト 35 件追加。全テスト通過 | DONE |
| v9.0-test Ph1 | testing/ パッケージ新設（TestDataFixtures + TestBuilders 7モデル + ResultMatchers 10種）+ テスト 26 件。全テスト通過 | DONE |
| v9.0-test Ph2 | Mapper/Exporter テスト 16 ファイルのハードコード日時を TestDataFixtures に統一 + E2eTestBase Screenshot 自動保存。全テスト通過 | DONE |
| v9.0-test Ph3 | MedicationLogSyncerTest 新規 + HomeVM/CareRecipientVM エラーテスト + CLAUDE.md #22 追記。全テスト通過 | DONE |
| v10.0-tdd Ph1 | MainCoroutineRule + TestBuilders 拡充（aUser/aCareRecipient/aEmergencyContact/aUserSettings）+ テスト 18 件。全テスト通過 | DONE |
| v10.0-tdd Ph2 | RepositoryImpl テスト 3 ファイル修正（careRecipientId/createdBy + TestBuilders + ResultMatchers）。46 テスト通過 | DONE |
| v10.0-tdd Ph3A | 16 ViewModel テストを MainCoroutineRule に移行 + TestBuilders 置換。全テスト通過 | DONE |
| v10.0-tdd Ph3B | 9 テストファイル（UnconfinedTestDispatcher 5 + 残り 4）を MainCoroutineRule に移行 + TestBuilders 採用。全テスト通過 | DONE |
| v10.0-tdd Ph4 | 残り 8 RepositoryImpl テストに ResultMatchers 適用 + SyncResult matchers 3関数追加。全テスト通過 | DONE |
| v9.0 Ph1 Billing | Google Play Billing 7.1.1 基盤（BillingRepository + NoOp + DI + PurchaseEntity v21 + Mapper + テスト 30件）。全テスト通過 | DONE |
| v9.0 Ph2 PremiumGuard | PremiumFeatureGuard + NotificationCountDataSource + TaskReminderWorker 制限チェック + Settings 残り表示。テスト 22 件追加。全テスト通過 | DONE |
| v9.0 Ph3 | CareRecipient firestoreId 追加 + SyncWorker 保存 + Firestore Rules isOwner/isMember + DB v22 + テスト 15 件。全テスト通過 | DONE |
| v9.0 Ph4 | Member/Invitation ドメインモデル + Room Entity + DAO + Mapper + Repository + DI。DB v23（14 Entity）。テスト ~44 件追加。全テスト通過 | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v23 baseline, SQLCipher 4.6.1, fallbackToDestructiveMigration, 14 Entity (MemberEntity + InvitationEntity 追加) |
| Firebase | BOM 34.8.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics) + No-Op フォールバック |
| 同期 | ConfigDrivenEntitySyncer + Incremental Sync (updatedAt フィルター) |
| Paging 3 | Task/Note/HealthRecord(LIST): PagingSource, Medication: DB検索のみ, Calendar: 対象外 |
| テスト | JUnit4 + MockK + Turbine + Robolectric 4.16 + Roborazzi 1.58.0, StandardTestDispatcher + FakeRepository |
| セキュリティ | SQLCipher + EncryptedPrefs + backup除外 + Root検出 + 生体認証, 全体リスク LOW |
| v5.0 抽出済み | FormValidator, PhotoManager, HealthMetricsParser, CareNoteAddEditScaffold, Clock |
| エクスポート | HealthRecord + MedicationLog + Task + Note の CSV/PDF。FileProvider 経由。CsvExporter + PdfExporter パターン。Settings DataExportDialog で期間指定 |
| SKIP 判定 | BaseCrudRepository（ROI マイナス）, BaseAddEditViewModel（Kotlin VM 不適合）, CareNoteListScaffold（構造多様性高） |
| v8.0 戦略 | MVP-First: ホーム画面先行 + 段階的マルチユーザー。Firestore パスは既に CareRecipient ベース構造のため recipientId 追加は Entity 層のみ |
| 仕様書乖離 | excretionMemo 未実装（conditionNote と混同注意）。NoteComment + CalendarEvent recurrence は v8.1 Ph6 で解消済み |
| 仕様書検証 (v8.1 Ph6後) | ホーム画面 ✅、CareRecipient 4フィールド ✅、CalendarEvent type/completed ✅、recipientId ✅(Ph4)、createdBy ✅(Ph5)、Onboarding ✅(Ph5)、NoteComment ✅(Ph6)、CalendarEvent recurrence ✅(Ph6)。全仕様差異解消 |
| 対応不要判定 | 緊急連絡先カテゴリ（RelationshipType 6値 > 仕様3分類で互換性あり）、通知プレミアム制限（Billing 未実装で無意味）、家族招待は v9.0 先送り |
| セキュリティ分析 | 成熟度 93/100。SQLCipher+EncryptedPrefs+Root検出+生体認証=業界標準超。CRITICAL: Firestore Rules 欠落。要改善: Export cache PII, Sync PII log, Session timeout, Validator pattern, Biometric memory dump. APPI 技術面 ~70% |
| セキュリティ強化 (Ph1) | ExceptionMasker（PII ログマスク）、SecureFileDeleter（3-pass 上書き削除）、Firestore Rules（careRecipients/{uid} owner auth）、Export cache 1h TTL |
| セキュリティ強化 (Ph2A) | Session timeout user-configurable (1-60min, default 5min), PBKDF2WithHmacSHA256 derived key (100K iter, 256-bit), master passphrase zero-clear |
| 入力検証パターン | domain/validator/ (InputValidator, HealthRecordValidator, MedicationValidator, RecurrenceValidator, SettingsValidator) → UI 層 FormValidator/AuthValidators がラッパー |
| セキュリティ強化 (Ph2B) | domain/validator/ 集約パターン（InputValidator が基本検証、専用 Validator が AppConfig 参照で委譲）。android.util.Patterns → 純 Kotlin 正規表現。UiText.DynamicString 追加 |
| Billing | Google Play Billing 7.1.1 (billing-ktx), BillingAvailability + NoOpBillingRepository パターン (FirebaseAvailability 踏襲), PremiumStatus StateFlow, BillingModule DI 条件分岐 |
| セキュリティ強化 (Ph3) | DomainError.SecurityError 新設。RootDetectionChecker DI 登録。Root 時エクスポート/アップロードブロック（Settings/HealthRecords/Medication VM + FirebaseStorageRepo）。Root セッションタイムアウト 60秒。ProGuard WorkManager/Paging/Security-Crypto/Biometric keep ルール。APPI 準拠ドキュメント（SECURITY.md, DATA_RETENTION_POLICY.md） |

## ロードマップ (v9.0)

### Phase 1: Google Play Billing 基盤 - DONE

Google Play Billing Library 7.1.1 基盤構築。BillingRepository + BillingRepositoryImpl + NoOpBillingRepository + BillingModule DI + PremiumStatus/ProductInfo/BillingConnectionState ドメインモデル + PurchaseEntity (Room v21) + PurchaseDao + PurchaseMapper + BillingAvailability（Google Play Services チェック）+ AppConfig.Billing + ProGuard keep ルール。FakeBillingRepository + テスト 30 件（BillingRepositoryImplTest 10件 + NoOpBillingRepositoryTest 7件 + PurchaseMapperTest 10件 + BillingAvailabilityTest 3件）。全ビルド・テスト通過。
- 対象: `domain/model/PremiumStatus.kt`, `domain/model/ProductInfo.kt`, `domain/model/BillingConnectionState.kt`, `domain/repository/BillingRepository.kt`, `data/local/entity/PurchaseEntity.kt`, `data/local/dao/PurchaseDao.kt`, `data/mapper/PurchaseMapper.kt`, `data/repository/BillingRepositoryImpl.kt`, `data/repository/NoOpBillingRepository.kt`, `di/BillingAvailability.kt`, `di/BillingModule.kt`, `config/AppConfig.kt`, `data/local/CareNoteDatabase.kt` (v21), `di/DatabaseModule.kt`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/proguard-rules.pro`
- 依存: なし

### Phase 2: PremiumFeatureGuard + 通知制限 - DONE

PremiumFeatureGuard インターフェース + PremiumFeatureGuardImpl（BillingRepository + NotificationCountDataSource + Clock 依存）。TaskReminderWorker に制限チェック追加（無料ユーザー 1日3回、プレミアム無制限、服薬リマインダーは制限対象外）。SettingsScreen に残り通知回数表示。DI 登録。テスト 22 件追加（PremiumFeatureGuardImplTest 12件 + TaskReminderWorkerTest 6件 + SettingsViewModelTest 4件）。全ビルド・テスト通過。
- 対象: `domain/repository/PremiumFeatureGuard.kt` (新規), `data/local/NotificationCountDataSource.kt` (新規), `data/repository/PremiumFeatureGuardImpl.kt` (新規), `config/AppConfig.kt`, `di/AppModule.kt`, `data/worker/TaskReminderWorker.kt`, `ui/screens/settings/` (SettingsViewModel, SettingsScreen, NotificationSection), `res/values/strings.xml`, `res/values-en/strings.xml`
- テスト: PremiumFeatureGuardImplTest (12件) + TaskReminderWorkerTest (+6件) + SettingsViewModelTest (+4件) + FakePremiumFeatureGuard (新規)
- 依存: Phase 1

### Phase 1B: Billing サーバーサイド検証 (Cloud Functions) - PENDING
Google Play Developer API 経由のレシート検証を Cloud Functions で実装。
本番リリース前の必須要件。
- 対象: Cloud Functions (Node.js), Firestore の purchaseTokens コレクション
- 依存: Phase 1
- 注意: **Claude Code の守備範囲外**。手動またはデスクトップ版で実装。Firebase CLI + Node.js 環境が必要
- 手動作業: Firebase Functions デプロイ、サービスアカウント設定、Google Play Developer API 有効化

### Phase 3: 家族招待 — Firestore 構造移行 - DONE

CareRecipient に firestoreId: String? フィールド追加（Entity/Model/Mapper/DAO/Repository/ActiveCareRecipientProvider）。SyncWorker が Firestore の careRecipientId をローカル DB に保存。DB v22。Firestore Security Rules を isOwner/isMember/hasAccess ヘルパー関数に移行 + members サブコレクション追加（前方互換）。テスト 15 件追加（CareRecipientMapperTest 10件 + CareRecipientRepositoryImplTest 2件 + ActiveCareRecipientProviderImplTest 3件）。全ビルド・テスト通過。
- 対象: `data/local/entity/CareRecipientEntity.kt`, `domain/model/CareRecipient.kt`, `data/mapper/CareRecipientMapper.kt`, `data/local/dao/CareRecipientDao.kt`, `domain/repository/CareRecipientRepository.kt`, `data/repository/CareRecipientRepositoryImpl.kt`, `domain/repository/ActiveCareRecipientProvider.kt`, `data/repository/ActiveCareRecipientProviderImpl.kt`, `data/worker/SyncWorker.kt`, `data/local/CareNoteDatabase.kt` (v22), `firebase/firestore.rules`
- テスト: CareRecipientMapperTest (新規10件) + CareRecipientRepositoryImplTest (+2件) + ActiveCareRecipientProviderImplTest (+3件) + FakeCareRecipientRepository更新 + FakeActiveCareRecipientProvider更新 + TestBuilders更新
- 依存: Phase 2

### Phase 4: 家族招待 — データモデル + Room - DONE

Member/Invitation ドメインモデル、Room Entity、DAO、Mapper、Repository 実装。MemberRole (OWNER/MEMBER)、InvitationStatus (PENDING/ACCEPTED/REJECTED/EXPIRED)。DB v23。DI 統合（DatabaseModule + AppModule）。テスト ~44 件追加（MemberMapperTest、InvitationMapperTest、MemberRepositoryImplTest、InvitationRepositoryImplTest + FakeMemberRepository + FakeInvitationRepository + TestBuilders 拡張）。全ビルド・テスト通過。
- 対象: `domain/model/Member.kt` (新規), `domain/model/Invitation.kt` (新規), `data/local/entity/MemberEntity.kt` (新規), `data/local/entity/InvitationEntity.kt` (新規), `data/local/dao/MemberDao.kt` (新規), `data/local/dao/InvitationDao.kt` (新規), `data/mapper/MemberMapper.kt` (新規), `data/mapper/InvitationMapper.kt` (新規), `domain/repository/MemberRepository.kt` (新規), `domain/repository/InvitationRepository.kt` (新規), `data/repository/MemberRepositoryImpl.kt` (新規), `data/repository/InvitationRepositoryImpl.kt` (新規), `data/local/CareNoteDatabase.kt` (v23), `di/DatabaseModule.kt`, `di/AppModule.kt`
- テスト: MemberMapperTest + InvitationMapperTest + MemberRepositoryImplTest + InvitationRepositoryImplTest + FakeMemberRepository (新規) + FakeInvitationRepository (新規) + TestBuilders (aMember/aInvitation 追加)
- 依存: Phase 3

### Phase 5: 家族招待 — 招待 UI + 招待フロー - PENDING
招待画面 (InvitationScreen)、メンバー管理画面 (MemberManagementScreen) の実装。
App Links で招待コード共有 → 受諾 → メンバー追加フロー。
- 対象: ui/screens/invitation/, ui/screens/member/, ui/navigation/Screen.kt (新ルート追加), res/values/strings.xml (JP/EN), AndroidManifest.xml (App Links intent-filter)
- 依存: Phase 4
- 注意: Firebase Dynamic Links 廃止済み → App Links + Firebase Hosting 短縮 URL で代替
- 手動作業: Firebase Hosting 設定、App Links assetlinks.json 配置、ドメイン検証

### Phase 6: 統合テスト + E2E - PENDING
全新機能の統合テスト、E2E テスト、セキュリティレビュー。
- 対象: app/src/test/ (Unit), app/src/androidTest/ (E2E), Firestore Security Rules テスト
- 依存: Phase 1, 2, 3, 4, 5
- 注意: Billing テストは Google Play Console テスター設定が必要（実機テスト）。Security Rules テストは firebase-rules-unit-testing パッケージ

## スコープ外 / 将来

- **v9.0+**: FCM リモート通知（Cloud Functions / バックエンド構築が前提）
- **v9.0+**: Wear OS 対応（Horologist + Health Services、別モジュール必要）
- **v9.0+**: CSV データインポート（対象ユーザー適合性検証後）
- **手動**: Play Console メタデータ、Firestore Security Rules 確認、問い合わせメール確定
