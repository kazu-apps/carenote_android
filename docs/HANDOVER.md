# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: v2.2 リファクタリング + Mapper ADR 全完了 (Item 82-101)

v2.0 Firebase 統合、v2.1 セキュリティ強化、v2.2 TDD リファクタリングの全項目が完了。
Item 101 ADR 分析の結果、Mapper 統合は不要と判定（Item 102 はスキップ）。

## 次のアクション

1. **リリース準備**:
   - リリース APK の実機テスト
   - Google Play Console へのアップロード準備
   - 問い合わせメールアドレスの確定（現在プレースホルダー）

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (carenote.app.support@gmail.com) — リリース前に確認
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | M-5 | Room スキーマ JSON がコミット済み（プライベートリポジトリでは許容） |
| MEDIUM | Item 30 | ValidationUtils.kt が未使用のデッドコード（本番インポートなし） |
| MEDIUM | Item 32 | JaCoCo `**/util/*` 除外が広範囲（テストは存在） |
| MEDIUM | Item 31 | テスト品質: Mapper ラウンドトリップ不完全、Repository Turbine 未使用、ViewModel Loading→Success テスト欠落 |
| LOW | L-4 | 全 DAO が OnConflictStrategy.REPLACE 使用（マルチデバイス同期リスク） |
| LOW | Item 99 | FCM リモート通知の受信処理が未実装（`onMessageReceived` がログのみ） |
| LOW | Item 99 | FCM トークンのサーバー送信が未実装（`onNewToken` がログのみ） |
| LOW | Item 100 | 個別 Screen ファイルの UI ハードコード値が未置換（共有コンポーネントのみ完了） |
| INFO | — | 削除確認ダイアログが UI から到達不可（スワイプ削除の準備） |
| INFO | — | Flow `.catch` が欠落（Room Flow は安定、低リスク） |

## PENDING 項目

なし（全項目完了）

---

## 完了タスク

| Item | 概要 | Status |
|------|------|--------|
| 1-4 | Clean Architecture + 服薬管理 + メモ | DONE |
| 5 | 健康記録（domain/data/VM/UI/チャート） | DONE |
| 6 | カレンダー（月表示 + イベント管理） | DONE |
| 7 | タスク管理（フィルター + 完了トグル） | DONE |
| 8-17 | リリース準備（設定、移行、アイコン、スプラッシュ、A11y、E2E、署名、ProGuard、法的文書、ストア） | DONE |
| 18-21 | 品質改善（バリデーション i18n、A11y WCAG AA、法的アセット、ダークモード） | DONE |
| 22-32 | コードレビュー（基盤、DB、全機能、テスト、ビルド） | DONE |
| 33-38 | テスト強化（Fake shouldFail、StandardTestDispatcher、VM 拡張、E2E インフラ） | DONE |
| 39-45 | コード品質（AppConfig 集約、Snackbar/バリデーション i18n、Screen 分割、リネーム） | DONE |
| 46-53 | セキュリティ修正（SQLCipher、EncryptedPrefs、backup除外、networkConfig、PII削除） | DONE |
| 55-61 | v2.0 Phase 1-2: Firebase Auth（SDK、認証 UI、ナビゲーション、テスト） | DONE |
| 62-68 | v2.0 Phase 3: Firestore 同期（スキーマ、Mapper、Syncer、Worker、UI、テスト） | DONE |
| 69-72 | v2.0 Phase 4: FCM（通知チャンネル、服薬リマインダー、テスト） | DONE |
| 73-78 | v2.0 Phase 5: Crashlytics + ProGuard + E2E + コードレビュー + CLAUDE.md | DONE |
| 79-81 | v2.1 セキュリティ強化（PII マスク、メール検証、パスワード 8文字） | DONE |
| 82-84 | v2.2 Phase 1: レガシー保護テスト（RemoteMapper 150、EntitySyncer 20、SyncRepo 20） | DONE |
| 85-88 | v2.2 Phase 2: Syncer TDD（SyncerConfig + ConfigDrivenEntitySyncer、5 Syncer 削除、~335行削減） | DONE |
| 89-92 | v2.2 Phase 3a: SettingsScreen TDD 分割（462→193行、SettingsDialogState sealed class） | DONE |
| 93-94 | v2.2 Phase 3b: AuthViewModel TDD 分割（316→89行、AuthValidators + 3 FormHandler） | DONE |
| 95-96 | v2.2 Phase 3c: SettingsViewModel TDD 整理（205→147行、updateSetting() 汎用化） | DONE |
| 97-100 | v2.2 Phase 4: コード品質（キー定数整理、Premium 削除、TODO 整理、UI 定数 AppConfig 移行） | DONE |
| 101 | Mapper 統合 ADR 作成（Option C: 統合しない を採択、`docs/ADR-002-UNIFIED-MAPPER.md`） | DONE |
| 102 | Mapper 統合実装 — ADR-002 により不要と判定（スキップ） | SKIP |

## セキュリティレビュー結果

| カテゴリ | ステータス | 詳細 |
|---------|----------|------|
| CRITICAL | 0 | 重大な脆弱性なし |
| HIGH | 0 | H-1 PII ログ (Item 79 完了), H-2 メール検証 (Item 80 完了) |
| MEDIUM | 3 | Rate Limiting, 暗号化破損時復旧, ProGuard |
| LOW | 1 | FCM トークン管理（サーバー側実装待ち） |
| **全体リスクレベル** | **LOW** | HIGH 問題は全て対応完了 |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v7, SQLCipher 4.6.1 暗号化, sync_mappings テーブル追加 |
| DB キー保存 | EncryptedSharedPreferences (Android Keystore AES256_GCM) |
| 設定保存 | EncryptedSharedPreferences (`carenote_settings_prefs`) |
| バックアップ除外 | DB, DB パスフレーズ prefs, 設定 prefs |
| Firebase | BOM 33.7.0 (Auth, Firestore, Messaging, Crashlytics, Analytics) |
| Firebase プラグイン | google-services.json 存在時のみ適用（条件付き） |
| 同期パターン | ConfigDrivenEntitySyncer + SyncerConfig（MedicationLogSyncer のみカスタム） |
| Worker | SyncWorker (15分定期), MedicationReminderWorker (指定時刻) |
| E2E テスト | TestFirebaseModule + TestDatabaseModule で Fake 実装に置換 |
| エラー i18n | `UiText.Resource` / `UiText.ResourceWithArgs` sealed class |
| Snackbar i18n | `SnackbarEvent` sealed interface (WithResId / WithString) |
| テーマカラー | `CareNoteColors.current.xxxColor`（ハードコード Color() 禁止） |
| 定数 | `AppConfig` オブジェクト（マジックナンバー禁止、UI 定数含む） |
| Mapper 設計 | Local/Remote 分離維持（ADR-002 で統合しない判定） |
| Enum パース | try-catch + フォールバック（NoteMapper, HealthRecordMapper, TaskMapper） |
| テストパターン | StandardTestDispatcher + Turbine + FakeRepository (MutableStateFlow) |
| Robolectric | 4.14.1（Android SDK シャドウ、Compose UI Test） |

## スコープ外 / 将来

- **v3.0**: Cloud Storage（写真保存）, Google Play Billing（プレミアムサブスクリプション）
- **手動**: スクリーンショット、フィーチャーグラフィック、プライバシーポリシー Web ホスティング
- **スキップ**: LegalDocumentScreen テスト（純粋な表示、ロジックなし）
