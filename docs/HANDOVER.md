# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: Timeline フィルタ/FAB/タスク完了トグル — 全 Phase 完了

## 次のアクション

1. E2E テスト手動実行（エミュレータ必要）: `./gradlew.bat connectedDebugAndroidTest`
2. 新機能の要件定義・ロードマップ作成

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
| v9.0 Ph1-6 | Billing 基盤 + Member/Invitation + 招待 UI/E2E + DB v23 | DONE |
| CLAUDE.md | v9.0 反映（DB v23, 14テーブル, 25モデル, 30リポジトリ, 18 E2E） | DONE |
| Detekt 全修正 | 367→0 issues。87 ファイル。AppModule 分割、CsvUtils 抽出、画面ヘルパー分割 | DONE |
| CI グリーン化 | Detekt 1.23.7、workflow_dispatch、screenshot soft-fail、PR #4 マージ | DONE |
| リマインダー修正 | calculateDelay + Clock injection + plusDays(1)。27テスト新規 | DONE |
| タブレット修正 | TextButton → clickable 化 + i18n + Compose UI テスト 17件 | DONE |
| GPP アップグレード | GPP 3.10.1→4.0.0 + api-key.json セキュリティ修正 | DONE |
| Task→CalendarEvent 統合 | CalendarEvent 拡張→Task 削除→UI 統合→全参照除去→E2E 修正。DB v23→v25、80ファイル変更 | DONE |
| Timeline フィルタ/FAB | TimelineFilterType + フィルタUI + FAB→タスク追加遷移 + route type パラメータ + 21 テスト | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v25, SQLCipher 4.6.1, fallbackToDestructiveMigration, 13 Entity (TaskEntity 削除済み) |
| Firebase | BOM 34.8.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics) + No-Op フォールバック |
| Billing | Google Play Billing 7.1.1, BillingAvailability + NoOpBillingRepository パターン |
| DI 分割 | AppModule + RepositoryModule + ExporterModule + DatabaseModule + FirebaseModule + SyncModule + WorkerModule + BillingModule |
| セキュリティ | SQLCipher + EncryptedPrefs + Root検出 + 生体認証 + PBKDF2 + Session timeout + domain/validator/ |
| テスト基盤 | MainCoroutineRule + TestBuilders (11モデル) + ResultMatchers (13種) + TestDataFixtures |
| エクスポート | HealthRecord/MedicationLog/Task/Note CSV/PDF + CsvUtils 共通ヘルパー |
| Detekt | 1.23.7, maxIssues=0, Compose FunctionNaming 除外, LongParameterList functionThreshold=8 |
| Task→CalendarEvent | CalendarEventType.TASK + isTask computed property + validate() + TaskFields.kt 分離。Screen.AddTask/EditTask はリダイレクト用に維持 |
| Timeline | TimelineFilterType enum + FAB→AddCalendarEvent(type=TASK) 遷移 + フィルタチップ + Checkbox 完了トグル |

## スコープ外 / 将来

- **FCM リモート通知**: Cloud Functions / バックエンド構築が前提
- **Wear OS 対応**: Horologist + Health Services、別モジュール必要
- **CSV データインポート**: 対象ユーザー適合性検証後
