# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: Phase 1 完了（Firestore Rules 修正 + ユニットテスト）

## 次のアクション

1. Phase 1B 本番デプロイ（手動: Firebase Console + Google Cloud Console 設定）
2. リリース APK の実機テスト実施（手動: 物理デバイス SDK 26-36）
3. 問い合わせメールアドレス確定（ビジネス判断: 現在プレースホルダー `support@carenote.app`）

### テスト結果 (Phase 1)

- **Billing テスト**: 14/14 PASS (playApiClient, purchaseRepository, verifyPurchase)
- **Firestore Rules テスト**: 14/14 PASS (認証, careRecipients アクセス制御, サブコレクション, careRecipientMembers, purchases)
- **合計**: 28/28 PASS
- **注意**: Rules テストは Firebase Emulator 必須 (`npm run test:rules` で Emulator 自動起動)

## 既知の問題

### 未解決（要対応）

- **[CRITICAL] Firestore sync 基盤欠損** — `careRecipientMembers` コレクションへの書き込みコードがアプリ全体・Cloud Functions のどこにも存在しない。SyncWorker の `getFirestoreCareRecipientId()` が常に失敗するため、全 Syncer（medication, note, healthRecord, calendarEvent, noteComment, medicationLog）が実質非機能。AcceptInvitation も Room DB にのみ保存し Firestore に書き込まない。Member/Invitation/CareRecipient の Syncer が SyncModule に未実装。Firestore sync を機能させるには初期セットアップフロー（ユーザー登録時に `careRecipients` + `careRecipientMembers` を Firestore に作成）が必要
- 問い合わせメールがプレースホルダー (`support@carenote.app`) — リリース前に実アドレス確定必要
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| DONE | Disc 20260219 | Firestore Rules `isOwner`/`isMember` → `isAuthorizedMember` 統一完了。`careRecipientMembers` Rules 追加。Rules ユニットテスト 14 ケース全 PASS |
| CRITICAL | Disc 20260219 | Firestore sync 基盤欠損: `careRecipientMembers` への write が存在せず、SyncWorker 全体が非機能。初期セットアップフロー実装が必要（スコープ外/将来に記載） |
| MEDIUM | v4.0 | Rate Limiting 未実装（API エンドポイント、バックエンド依存。Firebase コンソール設定で緩和可能） |
| LOW | Disc 20260219 | securityCrypto = "1.1.0-alpha06" alpha 版使用中。1.0.0 の既知バグ修正済みのため逆に安全（CVE なし確認済み） |
| LOW | Disc 20260219 | SyncMapping テーブルに careRecipientId カラムなし。複数対象者 Firestore sync 時に衝突リスク（v2.0 で対処） |
| LOW | v2.0 | FCM トークンのサーバー送信未実装（バックエンド前提） |
| LOW | v10.0-tdd | SettingsViewModelTest 1170 行（Detekt 対象外だが将来的に分割検討） |
| LOW | Detekt | Roborazzi スクリーンショット Windows/Linux フォントレンダリング差分（CI soft-fail 対応済み） |
| LOW | Detekt | SwipeToDismissItem deprecated API 警告（将来的な対応推奨） |
| LOW | CI | E2E テストがエミュレータ不安定で soft-fail（Linux KVM なし）。実機テストで代替 |
| LOW | Sec Ph3 | H-4: e.message コード衛生（DomainError 設計で UI 露出ゼロ、対応不要） |
| LOW | Disc Robo/1B | Roborazzi golden image は Linux 基準。Windows ローカルでの verify は常に差分が出る（soft-fail 維持） |
| LOW | Sec Ph3 | H-5: ExceptionMasker 拡張（既に Firebase 対応済み、YAGNI） |
| LOW | Disc OSV | biometric = "1.4.0-alpha05" alpha 版使用中。安定版リリース確認を別タスクとして検討 |
| LOW | Disc OSV | AGP テスト基盤 netty/protobuf は AGP メジャーアップデートで解消見込み。osv-scanner.toml クリーンアップを将来実施 |
| INFO | Detekt | Kotlin コンパイラ annotation-default-target 警告、テストコード型チェック警告（機能影響なし） |
| INFO | Sec Ph3 | M-1: Deep link App Links 移行（実ドメイン設定が前提、デプロイタスク） |

## PENDING 項目

### Phase 2: AppConfig.kt 分割 - PENDING

727行の AppConfig.kt を機能別に分割し、Detekt LargeClass 閾値（800行）超過を予防する。Phase 1 と並行実施可能。
- 対象ファイル:
  - `app/src/main/java/com/carenote/app/config/AppConfig.kt` (分割元)
  - `app/src/main/java/com/carenote/app/config/` (分割先: 複数ファイル)
- 依存: なし
- 信頼度: HIGH
- 工数: 1日

### Phase 3: 複数ケア対象者 UI 補完 - PENDING

DB/DAO/Repository 層は完成済み（全 Entity に careRecipientId あり、全 DAO にフィルタクエリ実装済み、ActiveCareRecipientProvider 実装済み）。残存作業は CareRecipientDao の LIMIT 1 解除、選択状態の DataStore 永続化、切替 UI 実装。ローカルのみ（Firestore sync 再設計は v2.0）。
- 対象ファイル:
  - `app/src/main/java/com/carenote/app/data/local/dao/CareRecipientDao.kt` (LIMIT 1 解除)
  - `app/src/main/java/com/carenote/app/data/repository/ActiveCareRecipientProviderImpl.kt` (DataStore 永続化)
  - `app/src/main/java/com/carenote/app/ui/screens/home/HomeScreen.kt` (対象者切替 UI)
  - 各 ViewModel (対象者切替の State 反映)
- 依存: Phase 1（Firestore Rules 修正が先）
- 信頼度: MEDIUM（Firestore sync は後続。ローカルのみの範囲では HIGH）
- 工数: 7-9日

### Phase 4: Firebase App Check 導入 - PENDING

Play Integrity ベースの App Check を導入し、Cloud Functions エンドポイントと Firestore のコスト保護を強化する。リリース後 2週間以内（P1）。Play Console 設定は手動作業。
- 対象ファイル:
  - `app/build.gradle.kts` (firebase-appcheck 依存追加)
  - `gradle/libs.versions.toml` (バージョン追加)
  - `app/src/main/java/com/carenote/app/di/FirebaseModule.kt` (App Check 初期化)
  - `app/proguard-rules.pro` (keep ルール追加)
- 依存: Phase 1
- 信頼度: MEDIUM（Play Console 手動設定が必要）
- 工数: 2-3日

### Phase 5: Offline First 強化 - PENDING

ConnectivityRepository の基盤を拡張し、同期失敗時の自動再試行キューと同期状態の常時 UI 表示を実装する。
- 対象ファイル:
  - `app/src/main/java/com/carenote/app/data/repository/` (SyncQueueRepository 新規)
  - `app/src/main/java/com/carenote/app/ui/components/` (同期状態 Snackbar)
  - `app/src/main/java/com/carenote/app/data/worker/SyncWorker.kt` (再試行ロジック拡張)
- 依存: Phase 3
- 信頼度: MEDIUM
- 工数: 3-4日

## 完了タスク

| Item | 概要 | Status |
|------|------|--------|
| Phase 1 | Firestore Rules `isOwner`/`isMember` → `isAuthorizedMember` 統一。`careRecipientMembers` Rules 追加。Rules ユニットテスト 14 ケース追加 | DONE |
| Phase 1B | Cloud Functions + PurchaseVerifier + BillingRepository 検証統合。品質チェック全 PASS（Build, Detekt, Unit Tests 2008/2008, Cloud Functions 14/14） | DONE |
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
| Detekt 修正 | CareRecipientScreen/ViewModel, AcceptInvitationViewModel メソッド分割（LongMethod, ComplexCondition, MaxLineLength） | DONE |
| Phase OSV | Firebase BOM 34.8.0→34.9.0 + osv-scanner.toml テスト基盤 CVE 除外（23件→0件） | DONE |
| Phase Robo | Roborazzi golden image を CI (Linux) 基準に更新。16ファイル更新（既存6修正 + 新規10追加） | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v25, SQLCipher 4.6.1, 13 Entity (TaskEntity 削除済み) |
| Firebase | BOM 34.9.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics, Functions) + No-Op フォールバック |
| Billing | Google Play Billing 7.1.1, BillingAvailability + NoOpBillingRepository + PurchaseVerifier パターン |
| DI 分割 | AppModule + RepositoryModule + ExporterModule + DatabaseModule + FirebaseModule + SyncModule + WorkerModule + BillingModule |
| セキュリティ | SQLCipher + EncryptedPrefs + Root検出 + 生体認証 + PBKDF2 + Session timeout + domain/validator/ + OSV-Scanner |
| テスト基盤 | MainCoroutineRule + TestBuilders (11モデル) + ResultMatchers (13種) + TestDataFixtures |
| Detekt | 1.23.7, maxIssues=0, Compose FunctionNaming 除外, LongParameterList functionThreshold=8 |
| CI | build-test + osv-scanner + e2e-test(soft-fail)。OSV-Scanner: PR/push/週次スケジュール + SARIF |
| ドキュメント構成 | CLAUDE.md (必須情報 ~9.5KB) + docs/ARCHITECTURE.md (詳細参照 ~17KB) |

## スコープ外 / 将来

- **FCM リモート通知**: Cloud Functions / バックエンド構築が前提。複数対象者対応（Phase 3）後に設計
- **Firestore 初期セットアップフロー**: ユーザー登録/ログイン時に `careRecipients` + `careRecipientMembers` を Firestore に作成する処理。AcceptInvitation の Firestore 書き込み。Member/Invitation/CareRecipient Syncer の SyncModule 追加。現状は sync 基盤が根本的に非機能（Disc 20260219 で発見）
- **Firestore Rules 全面再設計（v2.0）**: 複数対象者の Firestore sync 対応。SyncMapping に careRecipientId 追加、isOwner/isMember ロジック再設計
- **Wear OS 対応**: Horologist + Health Services、別モジュール必要
- **CSV データインポート**: 対象ユーザー適合性検証後
- **音声メモ**: Speech-to-Text 統合
- **介護保険認定書スキャン管理**: PDF 一元管理、期限切れアラート
- **CareNoteNavHost.kt 分割**: 686行、ルートビルダー関数抽出（Phase 2 完了後に検討）
- **Widget UX 改善**: タップでアクション（服薬記録等）追加
