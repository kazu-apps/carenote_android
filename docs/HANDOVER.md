# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: セキュリティ修正 Phase 3 完了

## 次のアクション

1. **OSV-Scanner 脆弱性対応**（23件: 7 High, 13 Medium, 1 Low, 2 Unknown。主要: protobuf-java 8.7, netty-codec-http2 8.2, netty-handler 7.5。全件トランジティブ依存、Firebase BOM 更新で解消見込み）
2. CI の workflow_dispatch で Roborazzi golden image 更新
3. Phase 1B: Billing サーバーサイド検証（Claude Code 守備範囲外）
4. リリース APK の実機テスト実施
5. 問い合わせメールアドレス確定（現在プレースホルダー `support@carenote.app`）

## 既知の問題

### 未解決（要対応）

- 問い合わせメールがプレースホルダー (`support@carenote.app`) — リリース前に実アドレス確定必要
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | v4.0 | Rate Limiting 未実装（API エンドポイント、バックエンド依存。Firebase コンソール設定で緩和可能） |
| LOW | v2.0 | FCM トークンのサーバー送信未実装（バックエンド前提） |
| LOW | v10.0-tdd | SettingsViewModelTest 1170 行（Detekt 対象外だが将来的に分割検討） |
| LOW | Detekt | Roborazzi スクリーンショット Windows/Linux フォントレンダリング差分（CI soft-fail 対応済み） |
| LOW | Detekt | SwipeToDismissItem deprecated API 警告（将来的な対応推奨） |
| LOW | CI | E2E テストがエミュレータ不安定で soft-fail（Linux KVM なし）。実機テストで代替 |
| INFO | Detekt | Kotlin コンパイラ annotation-default-target 警告、テストコード型チェック警告（機能影響なし） |
| LOW | Sec Ph3 | H-4: e.message コード衛生（DomainError 設計で UI 露出ゼロ、対応不要） |
| LOW | Sec Ph3 | H-5: ExceptionMasker 拡張（既に Firebase 対応済み、YAGNI） |
| INFO | Sec Ph3 | M-1: Deep link App Links 移行（実ドメイン設定が前提、デプロイタスク） |

## PENDING 項目

### Phase 1B: Billing サーバーサイド検証 (Cloud Functions) - PENDING

Google Play Developer API 経由のレシート検証を Cloud Functions で実装。本番リリース前の必須要件。
- 種別: 実装
- 対象: Cloud Functions (Node.js), Firestore の purchaseTokens コレクション
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
| Detekt 全修正 | 367→0 issues。AppModule 分割、CsvUtils 抽出、画面ヘルパー分割 | DONE |
| CI グリーン化 | Detekt 1.23.7、workflow_dispatch、screenshot soft-fail | DONE |
| Task→CalendarEvent | DB v23→v25、80ファイル変更。CalendarEventType.TASK 統合 | DONE |
| Phase 1-8 | Billing 修正、DB v25 ベースライン化、CalendarEventReminder、画面分割、Connectivity、launchBillingFlow、biometric、CI グリーン化 | DONE |
| Sec Phase 1 | データ保護 + 認可バイパス防止（backup DB exclude, email検証, 期限TOCTOU, 例外細分化） | DONE |
| CLAUDE.md 軽量化 | CLAUDE.md 29KB→~9.5KB。詳細を docs/ARCHITECTURE.md (~17KB) に分離 | DONE |
| OSV-Scanner 導入 | OSV-Scanner v2 + Gradle dependency locking + CI workflow + Claude Code hook | DONE |
| Sec Phase 2 | 入力バリデーション強化 + Biometric エラーハンドリング + DB リカバリバックアップ | DONE |
| Sec Phase 3 | BiometricHelper DI + passphraseHex zero-clear + RootDetector 強化 + ImageCompressor 検証 | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v25, SQLCipher 4.6.1, 13 Entity (TaskEntity 削除済み) |
| Firebase | BOM 34.8.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics) + No-Op フォールバック |
| Billing | Google Play Billing 7.1.1, BillingAvailability + NoOpBillingRepository パターン |
| DI 分割 | AppModule + RepositoryModule + ExporterModule + DatabaseModule + FirebaseModule + SyncModule + WorkerModule + BillingModule |
| セキュリティ | SQLCipher + EncryptedPrefs + Root検出 + 生体認証 + PBKDF2 + Session timeout + domain/validator/ + OSV-Scanner |
| テスト基盤 | MainCoroutineRule + TestBuilders (11モデル) + ResultMatchers (13種) + TestDataFixtures |
| Detekt | 1.23.7, maxIssues=0, Compose FunctionNaming 除外, LongParameterList functionThreshold=8 |
| CI | build-test + osv-scanner + e2e-test(soft-fail)。OSV-Scanner: PR/push/週次スケジュール + SARIF |
| ドキュメント構成 | CLAUDE.md (必須情報 ~9.5KB) + docs/ARCHITECTURE.md (詳細参照 ~17KB) |

## スコープ外 / 将来

- **FCM リモート通知**: Cloud Functions / バックエンド構築が前提
- **Wear OS 対応**: Horologist + Health Services、別モジュール必要
- **CSV データインポート**: 対象ユーザー適合性検証後
- **Firebase App Check**: 導入推奨（PII 保護強化）
