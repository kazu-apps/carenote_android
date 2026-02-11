# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: v6.0 Phase 2 E2E テスト拡充 — DONE

Critical Path 統合テスト、編集フロー、削除フロー、バリデーションエッジケースの E2E テストを4ファイル・17テスト追加。

## 次のアクション

1. v6.0 Phase 3: Firebase Analytics 導入
2. v6.0 Phase 4: パフォーマンス最適化
3. v6.0 Phase 5: CLAUDE.md 包括更新 + ドキュメント整備

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (`support@carenote.app`) — リリース前に実アドレス確定必要
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | v4.0 | Rate Limiting 未実装（API エンドポイント） |
| MEDIUM | v4.0 | ProGuard ルールの網羅性検証未実施 |
| LOW | v2.0 | FCM トークンのサーバー送信未実装（バックエンド前提、v7.0 スコープ） |

## PENDING 項目

### v6.0 Phase 2: E2E テスト拡充 - DONE

4ファイル・17テスト追加。統合 CRUD フロー、編集フロー（Medication/Calendar/Tasks/HealthRecords）、削除フロー（SwipeToDismiss + ConfirmDialog）、バリデーションエッジケース（6パターン）をカバー。
- 新規: `CriticalPathFlowTest.kt`(3), `EditFlowTest.kt`(4), `DeleteFlowTest.kt`(4), `ValidationFlowTest.kt`(6)
- 既存ファイル変更なし。`E2eTestBase` ヘルパーをそのまま再利用

### v6.0 Phase 3: Firebase Analytics 導入 - PENDING

Firebase Analytics SDK 統合。画面遷移トラッキング、主要アクションのイベント送信。AnalyticsRepository インターフェース + 実装 + No-Op フォールバック。
- 対象: `di/`, `domain/repository/`, `data/repository/`, `ui/screens/`, `libs.versions.toml`
- 依存: なし

### v6.0 Phase 4: パフォーマンス最適化 - PENDING

Macrobenchmark テスト作成。LazyColumn `key` 監査。Room クエリプラン確認。不要な recomposition 検出・修正。
- 対象: `benchmark/`, `ui/screens/`, `data/local/dao/`
- 依存: Phase 1 完了後（Baseline Profile 生成後が望ましい）

### v6.0 Phase 5: CLAUDE.md 包括更新 + ドキュメント整備 - PENDING

v6.0 の全変更を CLAUDE.md に反映。HANDOVER.md の完了済み項目を圧縮。
- 対象: `CLAUDE.md`, `docs/HANDOVER.md`
- 依存: Phase 1-4 完了後
- **スキップ**: LegalDocumentScreen テスト（純粋な表示、ロジックなし）

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
| v6.0 Ph1 | Root ダイアログ改善（続ける/終了）+ 問い合わせ機能 + RELEASE_CHECKLIST | DONE |
| v6.0 Ph2 | E2E テスト拡充（CRUD/Edit/Delete/Validation 17テスト） | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v14 baseline, SQLCipher 4.6.1, fallbackToDestructiveMigration, 10 Entity |
| Firebase | BOM 34.8.0 (Auth, Firestore, Messaging, Crashlytics, Storage) + No-Op フォールバック |
| 同期 | ConfigDrivenEntitySyncer + Incremental Sync (updatedAt フィルター) |
| Paging 3 | Task/Note/HealthRecord(LIST): PagingSource, Medication: DB検索のみ, Calendar: 対象外 |
| テスト | JUnit4 + MockK + Turbine + Robolectric 4.16 + Roborazzi 1.58.0, StandardTestDispatcher + FakeRepository |
| セキュリティ | SQLCipher + EncryptedPrefs + backup除外 + Root検出 + 生体認証, 全体リスク LOW |
| v5.0 抽出済み | FormValidator, PhotoManager, HealthMetricsParser, CareNoteAddEditScaffold, Clock |
| SKIP 判定 | BaseCrudRepository（ROI マイナス）, BaseAddEditViewModel（Kotlin VM 不適合）, CareNoteListScaffold（構造多様性高） |

## スコープ外 / 将来

- **v7.0+**: Google Play Billing（プレミアムサブスクリプション）
- **v7.0+**: FCM リモート通知（Cloud Functions / バックエンド構築が前提）
- **v7.0+**: Wear OS 対応（Horologist + Health Services）
- **手動**: Play Console メタデータ、Firestore Security Rules 確認、問い合わせメール確定
