# HANDOVER.md - CareNote Android

## セッションステータス: 進行中（OSV-Scanner 導入完了、脆弱性対応 + セキュリティ Phase 2 待ち）

## 次のアクション

1. **OSV-Scanner スキャン結果の脆弱性対応**（23件検出: 0 Critical, 7 High, 13 Medium, 1 Low, 2 Unknown。主要: protobuf-java 8.7, netty-codec-http2 8.2, netty-handler 7.5。全件修正可能）
2. **`/exec` でセキュリティ修正 Phase 1 を実行**
3. Phase 2 セキュリティ修正（Phase 1 完了後）
4. CI の workflow_dispatch で Roborazzi golden image 更新
5. Phase 1B: Billing サーバーサイド検証（Claude Code 守備範囲外）
6. リリース APK の実機テスト実施
7. 問い合わせメールアドレス確定（現在プレースホルダー `support@carenote.app`）

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

## PENDING 項目

### セキュリティ修正 Phase 1: データ保護 + 認可バイパス防止 - DONE
backup_rules DB exclude、AcceptInvitationViewModel メール検証/期限再チェック/重複チェック/トランザクション修正、DatabaseRecoveryHelper 例外細分化、テスト 4 件追加

### セキュリティ修正 Phase 2: 中規模改善 - PENDING

Phase 1 の防止策を補完する構造的改善。

- 対象ファイル:
  - `app/src/main/java/com/carenote/app/data/local/DatabaseRecoveryHelper.kt` — フルバックアップ + cache dir 保存
  - `app/src/main/java/com/carenote/app/di/DatabaseModule.kt` — provideDatabase() try-catch 追加
  - `app/src/main/java/com/carenote/app/ui/screens/carerecipient/CareRecipientViewModel.kt` — 6フィールド MAX_LENGTH バリデーション追加
  - `app/src/main/java/com/carenote/app/ui/screens/emergencycontact/AddEditEmergencyContactViewModel.kt` — memo MAX_LENGTH + 電話番号フォーマット検証
  - `app/src/main/java/com/carenote/app/ui/MainActivity.kt` — Biometric onError フォールバック
- テスト: 各 ViewModel テストにバリデーションケース追加、DatabaseRecoveryHelperTest にバックアップテスト追加
- 依存: Phase 1
- 信頼度: HIGH

### セキュリティ修正 Phase 3: 長期改善（再評価リスト） - PENDING

Phase 2 完了後に実施判断。優先度 LOW/MEDIUM の項目。

- 対象:
  - H-4: e.message コード衛生（LOW — UI 露出ゼロ確認済み）
  - H-5: ExceptionMasker Firebase/Network 層限定拡張（MEDIUM — 全面改修は YAGNI）
  - M-8: DatabaseEncryptionMigrator passphraseHex zero-clear
  - M-1: Deep link App Links 移行（実ドメイン設定が前提）
  - M-9: RootDetector 強化（Play Integrity API 検討）
  - M-5: ImageCompressor MIME/サイズ検証
  - H-2 続き: BiometricHelper DI 化
- 依存: Phase 2
- 信頼度: MEDIUM

### セキュリティレビュー重大度再評価（3 Expert 合意）

| 項目 | 元の評価 | 最終評価 | 根拠 |
|------|---------|---------|------|
| H-4 (e.message UI 露出) | HIGH | **LOW** | ErrorDisplay は DomainError 型で固定メッセージ表示。HomeUiState.error は HomeScreen で未参照。ExportState.Error も未表示。全員一致 |
| H-5 (ExceptionMasker 60+箇所) | HIGH | **MEDIUM** | DomainError は Throwable ではなく ExceptionMasker 不要。Firebase/Network 層のみ対応で十分。全員一致 |
| H-2 (Biometric バイパス) | HIGH | **MEDIUM** | canAuthenticate フォールバックは正しい設計。onError のみ UX 問題。全員一致 |
| H-6 (トークン平文保存) | HIGH | **MEDIUM** | SQLCipher 暗号化 DB 内。ハッシュ化は実装コスト過大。期限再チェックのみ Phase 1 |

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
| Detekt 全修正 | 367→0 issues。AppModule 分割、CsvUtils 抽出、画面ヘルパー分割 | DONE |
| CI グリーン化 | Detekt 1.23.7、workflow_dispatch、screenshot soft-fail | DONE |
| Task→CalendarEvent | DB v23→v25、80ファイル変更。CalendarEventType.TASK 統合 | DONE |
| Phase 1 | BillingRepositoryImpl debugMessage 漏洩修正 + Dead Code 除去 | DONE |
| Phase 2 | fallbackToDestructiveMigration 削除 + v25 ベースライン化 | DONE |
| Phase 3 | CalendarEventReminderWorker + Scheduler + 通知チャンネル + テスト | DONE |
| Phase 4 | CalendarEventReminderSection + CalendarEventFormFields 画面分割 | DONE |
| Phase 5 | ConnectivityRepository + OfflineIndicator バナー | DONE |
| Phase 6 | BillingRepository.launchBillingFlow + PremiumSection + テスト 6件 | DONE |
| Phase 7 | biometric 1.4.0-alpha05 + HomeScreen クリック遷移 + スライド/フェードアニメーション | DONE |
| Phase 8 | CI グリーン化: Detekt violations + E2E import/Hilt binding 修正 + e2e-test soft-fail | DONE |
| Sec Phase 1 | データ保護 + 認可バイパス防止（backup DB exclude, email検証, 期限TOCTOU, 例外細分化） | DONE |
| CLAUDE.md 軽量化 | CLAUDE.md 29KB→~9.5KB。詳細参照情報を docs/ARCHITECTURE.md (~17KB) に分離 | DONE |
| OSV-Scanner 導入 | OSV-Scanner v2 + Gradle dependency locking + CI workflow + Claude Code hook | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v25, SQLCipher 4.6.1, 13 Entity (TaskEntity 削除済み) |
| Firebase | BOM 34.8.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics) + No-Op フォールバック |
| Billing | Google Play Billing 7.1.1, BillingAvailability + NoOpBillingRepository パターン |
| DI 分割 | AppModule + RepositoryModule + ExporterModule + DatabaseModule + FirebaseModule + SyncModule + WorkerModule + BillingModule |
| セキュリティ | SQLCipher + EncryptedPrefs + Root検出 + 生体認証 + PBKDF2 + Session timeout + domain/validator/ |
| テスト基盤 | MainCoroutineRule + TestBuilders (11モデル) + ResultMatchers (13種) + TestDataFixtures |
| エクスポート | HealthRecord/MedicationLog/Task/Note CSV/PDF + CsvUtils 共通ヘルパー |
| Detekt | 1.23.7, maxIssues=0, Compose FunctionNaming 除外, LongParameterList functionThreshold=8 |
| CI | build-test: Build + UnitTest + Coverage + Screenshot(soft-fail) + Detekt + E2E compile。e2e-test: soft-fail（エミュレータ不安定） |
| Task→CalendarEvent | CalendarEventType.TASK + isTask computed property + validate() + TaskFields.kt 分離 + 画面分割 |
| Timeline | TimelineFilterType enum + FAB→AddCalendarEvent(type=TASK) 遷移 + フィルタチップ |
| ドキュメント構成 | CLAUDE.md (必須情報 ~9.5KB) + docs/ARCHITECTURE.md (詳細参照 ~17KB) |
| 画面遷移 | NavHost デフォルト: slideInHorizontally/slideOutHorizontally 300ms。Bottom Nav タブ: fadeIn/fadeOut 300ms |

## スコープ外 / 将来

- **FCM リモート通知**: Cloud Functions / バックエンド構築が前提
- **Wear OS 対応**: Horologist + Health Services、別モジュール必要
- **CSV データインポート**: 対象ユーザー適合性検証後
- **Firebase App Check**: 導入推奨（PII 保護強化）
- **SettingsViewModel 分割**: ユーザー価値ゼロ。@Suppress("TooManyFunctions") で現状問題なし
