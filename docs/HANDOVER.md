# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: v8.0 Phase 2 カレンダー拡張（type, completed） — DONE

CalendarEvent に `type: CalendarEventType` (HOSPITAL/VISIT/DAYSERVICE/OTHER) と `completed: Boolean` を追加。イベント種別アイコン + 完了チェック UI。DB migration v15→v16 (fallbackToDestructiveMigration)。全テスト通過。

### 実装内容
- **Data Layer**: `CalendarEventType` enum 新規作成、`CalendarEventEntity` に type/completed カラム追加、Mapper 更新、DB v16
- **UI Layer**: `CalendarEventCard` に種別アイコン + 完了チェックボックス追加、`CalendarViewModel.toggleCompleted()` 実装、`AddEditCalendarEventViewModel.updateType()` 実装、HomeScreen のカレンダーセクションに種別アイコン追加
- **i18n**: strings.xml (JP/EN) にイベント種別ラベル追加
- **Analytics**: `EVENT_CALENDAR_EVENT_COMPLETED` + `PARAM_EVENT_TYPE` を AppConfig.Analytics に追加
- **テスト**: CalendarViewModelTest に toggleCompleted 4 件追加、AddEditCalendarEventViewModelTest に type 関連 6 件追加

### 変更ファイル
- `domain/model/CalendarEventType.kt` (新規)
- `domain/model/CalendarEvent.kt` — type, completed フィールド追加
- `data/local/entity/CalendarEventEntity.kt` — type, completed カラム追加
- `data/mapper/CalendarEventMapper.kt` — type, completed マッピング
- `data/local/CareNoteDatabase.kt` — version 16
- `ui/screens/calendar/CalendarViewModel.kt` — toggleCompleted()
- `ui/screens/calendar/AddEditCalendarEventViewModel.kt` — updateType(), FormState.type
- `ui/screens/calendar/CalendarScreen.kt` — CalendarEventCard UI
- `ui/screens/home/HomeScreen.kt` — 種別アイコン表示
- `ui/preview/PreviewData.kt` — CalendarEventType サンプル
- `config/AppConfig.kt` — Analytics 定数
- `res/values/strings.xml`, `res/values-en/strings.xml` — i18n
- `test/.../CalendarViewModelTest.kt` — 4 件追加
- `test/.../AddEditCalendarEventViewModelTest.kt` — 6 件追加

## 次のアクション

- v8.0 Phase 3: テスト + CLAUDE.md 更新（Roborazzi + JaCoCo 80% 維持）

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

### 仕様書 vs 実装 乖離サマリー

重大（コンセプト影響）: 家族共有・マルチユーザー完全未実装、ホーム画面なし、全 Entity に recipientId/createdBy なし
中程度（機能差異）: CalendarEvent type/completed/recurrence 欠落、CareRecipient careLevel/medicalHistory/allergies 欠落、メモコメントなし、オンボーディングなし
良い方向の進化: ローカルファースト設計、SQLCipher、生体認証、Root 検出、CSV/PDF エクスポート、横断検索、ウィジェット

### v8.0 Phase 1: ホーム画面 + CareRecipient 拡張 - DONE

HomeViewModel (6 flows combine) + HomeScreen (5 sections) + CareRecipient 4 フィールド追加 + BottomNav 変更 + DB v15 + テスト 19 件追加。全 1494+ テスト通過。
- 対象: `ui/screens/home/`, `domain/model/CareRecipient.kt`, `data/local/entity/`, `ui/navigation/`, `config/AppConfig.kt`, `res/values/strings.xml`
- 依存: なし

### v8.0 Phase 2: カレンダー拡張（type, completed） - DONE

CalendarEvent に type (HOSPITAL/VISIT/DAYSERVICE/OTHER) と completed フラグを追加。イベント種別アイコン表示 + 完了チェック UI。DB v16。CalendarViewModelTest 4 件 + AddEditCalendarEventViewModelTest 6 件追加。全テスト通過。
- 対象: `domain/model/`, `data/local/entity/`, `data/mapper/`, `ui/screens/calendar/`, `ui/screens/home/`, `config/AppConfig.kt`, `res/values/strings.xml`
- 依存: Phase 1

### v8.0 Phase 3: テスト + CLAUDE.md 更新 - PENDING

Phase 1-2 の新機能に対する UT + Roborazzi + CLAUDE.md 更新。JaCoCo 80% 維持。
- 対象: `app/src/test/`, `CLAUDE.md`, `docs/HANDOVER.md`
- 依存: Phase 2

### v8.1 Phase 4: マルチユーザー基盤 — recipientId 追加 - PENDING

全 8 Entity に recipientId を追加。DAO に recipientId WHERE 句追加。Firestore 同期パスは既に CareRecipient ベース構造のため変更不要。DB migration v16→v17。3 サブフェーズに分割予定。
- 対象: `data/local/entity/`, `data/mapper/`, `data/local/dao/`, `data/repository/`, `di/`
- 依存: Phase 3（v8.0 リリース後）

### v8.1 Phase 5: createdBy 統一 + オンボーディング - PENDING

Note.authorId を createdBy に統一。HealthRecord/Task に createdBy 追加。オンボーディング画面実装。DB migration v17→v18。
- 対象: `domain/model/`, `data/local/entity/`, `ui/screens/onboarding/`, `ui/navigation/`
- 依存: Phase 4

### v8.1 Phase 6: メモコメント + カレンダー recurrence - PENDING

NoteComment モデル新規追加。CalendarEvent に recurrence, assignedTo, reminderEnabled 追加。RecurrenceExpander クラス抽出。
- 対象: `domain/model/`, `data/local/`, `data/repository/`, `ui/screens/notes/`, `ui/screens/calendar/`
- 依存: Phase 5

### v8.1 Phase 7: 家族招待 + テスト強化 - PENDING

家族招待フロー実装。全新機能の UT + E2E + Roborazzi。JaCoCo 80% + Detekt 0。CLAUDE.md 最終更新。
- 対象: `ui/screens/invitation/`, `domain/model/`, `app/src/test/`, `CLAUDE.md`
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

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v16 baseline, SQLCipher 4.6.1, fallbackToDestructiveMigration, 10 Entity |
| Firebase | BOM 34.8.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics) + No-Op フォールバック |
| 同期 | ConfigDrivenEntitySyncer + Incremental Sync (updatedAt フィルター) |
| Paging 3 | Task/Note/HealthRecord(LIST): PagingSource, Medication: DB検索のみ, Calendar: 対象外 |
| テスト | JUnit4 + MockK + Turbine + Robolectric 4.16 + Roborazzi 1.58.0, StandardTestDispatcher + FakeRepository |
| セキュリティ | SQLCipher + EncryptedPrefs + backup除外 + Root検出 + 生体認証, 全体リスク LOW |
| v5.0 抽出済み | FormValidator, PhotoManager, HealthMetricsParser, CareNoteAddEditScaffold, Clock |
| エクスポート | HealthRecord + MedicationLog + Task + Note の CSV/PDF。FileProvider 経由。CsvExporter + PdfExporter パターン。Settings DataExportDialog で期間指定 |
| SKIP 判定 | BaseCrudRepository（ROI マイナス）, BaseAddEditViewModel（Kotlin VM 不適合）, CareNoteListScaffold（構造多様性高） |
| v8.0 戦略 | MVP-First: ホーム画面先行 + 段階的マルチユーザー。Firestore パスは既に CareRecipient ベース構造のため recipientId 追加は Entity 層のみ |
| 仕様書乖離 | excretionMemo 未実装（conditionNote と混同注意）、Note.authorId のみ createdBy 相当、CalendarEvent recurrence 未実装（type/completed は v8.0 Ph2 で実装済み） |

## スコープ外 / 将来

- **v8.0+**: Google Play Billing（プレミアムサブスクリプション、サーバーサイド検証必須）
- **v8.0+**: FCM リモート通知（Cloud Functions / バックエンド構築が前提）
- **v8.0+**: Wear OS 対応（Horologist + Health Services、別モジュール必要）
- **v8.0+**: CSV データインポート（対象ユーザー適合性検証後）
- **手動**: Play Console メタデータ、Firestore Security Rules 確認、問い合わせメール確定
