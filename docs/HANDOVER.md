# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: v9.0-sec Phase 2A 完了

Session タイムアウト user-configurable（1-60分、デフォルト5分）、PBKDF2WithHmacSHA256 derived key（100K iterations, 256-bit）、master passphrase ゼロクリア。テスト ~16 件追加。全ビルド・テスト通過。

## 次のアクション

- セキュリティ改善 Phase 2B 実行（入力検証パターン統一）
- v9.0 計画策定（家族招待フロー、Google Play Billing、通知制限）

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (`support@carenote.app`) — リリース前に実アドレス確定必要
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | v4.0 | Rate Limiting 未実装（API エンドポイント、バックエンド依存） |
| LOW | v2.0 | FCM トークンのサーバー送信未実装（バックエンド前提） |

## ロードマップ

### v9.0-sec Phase 1: データ保護 + Firestore Rules - DONE

Firestore Security Rules 新規作成、ExceptionMasker/SecureFileDeleter 新規ユーティリティ、8 Exporter キャッシュ上書き削除、EntitySyncer/MedicationLogSyncer/FirebaseStorageRepositoryImpl PII ログマスク。テスト 35 件追加。全ビルド・テスト通過。

### v9.0-sec Phase 2A: Session タイムアウト + Derived Key - DONE

Session タイムアウト user-configurable（1-60分、デフォルト5分）、PBKDF2WithHmacSHA256 derived key（100K iterations, 256-bit）、master passphrase ゼロクリア。テスト ~16 件追加。全ビルド・テスト通過。
- 対象: `config/AppConfig.kt`, `domain/model/UserSettings.kt`, `data/local/SettingsDataSource.kt`, `domain/repository/SettingsRepository.kt`, `data/repository/SettingsRepositoryImpl.kt`, `ui/MainActivity.kt`, `data/local/DatabasePassphraseManager.kt`, `ui/screens/settings/SettingsViewModel.kt`, `res/values/strings.xml`, `res/values-en/strings.xml`
- テスト: DatabasePassphraseManagerTest (8件) + SettingsRepositoryImplTest (5件) + SettingsViewModelTest (3件)
- 依存: Phase 1

### v9.0-sec Phase 2B: 入力検証パターン統一 - PENDING

既存 FormValidator/AuthValidators を domain/validator/ に拡張。外部ライブラリ不要。6-8 AddEdit ViewModel の入力検証パターンを統一。
- 対象: `domain/validator/` (新規), `ui/viewmodel/` (6-8 AddEdit ViewModel)
- 依存: Phase 2A

### v9.0-sec Phase 3: バイナリ保護 + APPI 準拠 - PENDING

ProGuard/R8 難読化ルール強化、FileProvider grantUriPermissions 制限、Root 検出時の機密データアクセス制限オプション。APPI（個人情報保護法）準拠ドキュメント整備（SECURITY.md, DATA_RETENTION_POLICY.md）。
- 対象: `app/proguard-rules.pro`, `app/src/main/res/xml/file_paths.xml`, `ui/util/RootDetector.kt`, `ui/MainActivity.kt`, `docs/SECURITY.md` (新規), `docs/DATA_RETENTION_POLICY.md` (新規)
- 依存: Phase 1, Phase 2A, Phase 2B

### v9.0-test Phase 1: テストユーティリティ基盤構築 - PENDING

テストデータ生成の重複削減と品質向上のための共通ユーティリティを新設。Builder DSL（Medication/Note/Task/HealthRecord）、ResultMatchers、FlowAssertions（Turbine ラッパー）、TestDataFixtures（FakeClock 統合）を testing/ パッケージに配置。
- 対象: `app/src/test/java/com/carenote/app/testing/` (新規 7-8 ファイル)
- 依存: なし

### v9.0-test Phase 2: テストデータ統一 + E2E デバッグ改善 - PENDING

Mapper/Exporter テスト 16 件のハードコード日時を Phase 1 の Fixtures/FakeClock に統一。E2E テスト失敗時の Screenshot 自動保存機能を追加。ViewModel テストの Dispatcher セットアップ重複を ViewModelTestBase に抽出。
- 対象: `app/src/test/` (変更 12-15 ファイル)、`app/src/androidTest/e2e/` (変更 2 ファイル)
- 依存: Phase 1

### v9.0-test Phase 3: カバレッジ向上 + ドキュメント - PENDING

Syncer 具象テスト追加（MedicationSyncer/NoteSyncer 等）、PagingSource ユニットテスト追加、ViewModel エラーシナリオテスト追加。CLAUDE.md に落とし穴 #22（テスト開発 Best Practices）を追記。
- 対象: `app/src/test/` (新規/変更 8-10 ファイル)、`CLAUDE.md`
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
| task-driver v8 | SKILL.md + team-templates.md 全面書き換え（TeamCreate ハイブリッド）+ CLAUDE.md sub-agent-patterns 原則追加 + MEMORY.md 更新 | DONE |
| task-driver v8 レビューラウンド | Plan モード Round 2 相互レビュー追加。SKILL.md（手順 Round 1/Round 2 構造化 + Rule #18）+ team-templates.md（レビューテンプレート 3 件） | DONE |
| テスト機能リサーチ | テスト基盤調査（126 unit + 22 E2E + 31 Fakes + 56 Roborazzi）、Builder DSL 方針策定、リスク分析（Flaky LOW、E2E screenshot MEDIUM、日時統一 MEDIUM）、3 Phase ロードマップ作成 | DONE |
| セキュリティ分析 | OWASP Mobile Top 10 + 攻撃ベクター + APPI 準拠評価。成熟度 93/100。CRITICAL 1 (Firestore Rules), HIGH 5 (Export PII, Sync PII, Session timeout, Input validation, Biometric memory dump), MEDIUM 5。総工数 107h。3 Phase ロードマップ作成 | DONE |
| v9.0-sec Ph1 | データ保護 + Firestore Rules（ExceptionMasker/SecureFileDeleter、8 Exporter cache cleanup、Sync PII マスク、firestore.rules）。テスト 35 件追加。全テスト通過 | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v19 baseline, SQLCipher 4.6.1, fallbackToDestructiveMigration, 11 Entity |
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

## v9.0 計画（v8.1 完了後）

- **家族招待フロー**: Member/Invitation データモデル、Firestore Security Rules、招待 UI（v8.1 Phase 7 から先送り。Firestore インテグレーション + バックエンド検証が複雑なため分離）
- **Google Play Billing**: プレミアムサブスクリプション + 通知プレミアム制限（サーバーサイド検証必須）
- **通知制限**: 無料/プレミアムの通知回数制限（Billing 実装と同時に PremiumFeatureGuard として一括実装）

## スコープ外 / 将来

- **v9.0+**: FCM リモート通知（Cloud Functions / バックエンド構築が前提）
- **v9.0+**: Wear OS 対応（Horologist + Health Services、別モジュール必要）
- **v9.0+**: CSV データインポート（対象ユーザー適合性検証後）
- **手動**: Play Console メタデータ、Firestore Security Rules 確認、問い合わせメール確定
