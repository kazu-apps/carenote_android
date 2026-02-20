# HANDOVER.md - CareNote Android

## セッションステータス: 進行中

## 現在のタスク: Phase 3 Firestore 初期セットアップフロー実装完了

## 次のアクション

1. `/exec` で Phase 0 から順次実行
2. Phase 0 完了 — applicationId は com.carenote.app に確定
3. Phase 1 (Billing UI) は無料版先行リリースなら SKIP 可能
4. Firebase App Check 手動設定（Firebase Console + Google Cloud Console）
5. リリース APK の実機テスト実施（手動: 物理デバイス SDK 26-36）
6. 問い合わせメールアドレス確定済み (`support-carenote@ks-apps.org`)

## 既知の問題

### 未解決（要対応）

- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | v4.0 | Rate Limiting 未実装（API エンドポイント、バックエンド依存。Firebase コンソール設定で緩和可能） |
| LOW | Disc 20260219 | securityCrypto = "1.1.0-alpha06" alpha 版使用中。CVE なし確認済み |
| LOW | Disc 20260219 | SyncMapping に careRecipientId カラムなし。複数対象者 sync 時に衝突リスク（v2.0 で対処） |
| LOW | v2.0 | FCM トークンのサーバー送信未実装（バックエンド前提） |
| LOW | v10.0-tdd | SettingsViewModelTest 1170 行（将来的に分割検討） |
| LOW | Detekt | Roborazzi Windows/Linux フォントレンダリング差分（CI soft-fail 対応済み） |
| LOW | Detekt | SwipeToDismissItem deprecated API 警告 |
| LOW | CI | E2E テストがエミュレータ不安定で soft-fail。実機テストで代替 |
| LOW | Disc OSV | biometric = "1.4.0-alpha05" alpha 版。安定版リリース確認を別タスクとして検討 |
| LOW | Disc OSV | AGP テスト基盤 netty/protobuf は AGP メジャーアップデートで解消見込み |
| LOW | Dir | ui/common/UiText.kt → ui/util/ 移動は ROI 低のため延期（16+ ファイル変更） |
| LOW | Dir | テスト Fake 重複（test/ vs androidTest/）は DI アノテーション差異のため統合見送り |
| INFO | Detekt | Kotlin コンパイラ annotation-default-target 警告（機能影響なし） |
| INFO | Sec Ph3 | Deep link App Links 移行（実ドメイン設定が前提、デプロイタスク） |
| LOW | Disc 20260219b | オンボーディングがシングル画面。機能紹介・権限説明なし |
| LOW | Disc 20260219b | Widget タップが常にアプリトップ起動（個別ディープリンクなし） |
| LOW | Disc 20260219b | android:allowBackup="true"。SharedPreferences がバックアップ対象（DB は除外済み） |

## ロードマップ

戦略: 無料版 MVP 先行リリース → Billing 追加 → Sync 基盤

### Phase 0: applicationId 修正 + リリース準備 - DONE

applicationId の仮文字列 `original` を削除し、正式な ID に変更。1行変更だが公開後は変更不可のため最優先。
- 対象ファイル:
  - `app/build.gradle.kts` (applicationId 修正)
- 依存: なし
- 信頼度: HIGH
- 備考: 正式 applicationId はユーザーが決定（ビジネス判断）

### Phase 1: Billing UI 実装 (BillingScreen + BillingViewModel) - DONE

プレミアム購入 UI を実装。BillingRepositoryImpl は実装済みのため、UI + ViewModel + isPremium 反映が主な作業。
- 対象ファイル:
  - `ui/screens/billing/BillingScreen.kt` (新規)
  - `ui/screens/billing/BillingViewModel.kt` (新規)
  - `ui/navigation/Screen.kt` (Billing ルート追加)
  - `ui/navigation/CareNoteNavHost.kt` (Billing ナビゲーション追加)
  - `data/mapper/UserMapper.kt` (isPremium ハードコード修正)
  - `ui/screens/settings/SettingsScreen.kt` (プレミアム購入への導線)
  - テストファイル (BillingViewModelTest.kt 新規)
- 依存: Phase 0
- 信頼度: MEDIUM
- 備考: 無料版先行リリースなら SKIP 可能。収益化を急ぐ場合のみ必要

### Phase 2: 大きなファイル分割 (HomeScreen + CareNoteNavHost) - DONE

HomeScreen.kt (706→256行: components/HomeTopBar.kt + sections/HomeSections.kt 抽出)、CareNoteNavHost.kt (701→57行: routes/BottomNavRoutes.kt + ContentRoutes.kt + SettingsRoutes.kt + AuthRoutes.kt 抽出)

### Phase 3: Firestore 初期セットアップフロー - DONE

SyncWorker 駆動の自動セットアップ。getCareRecipientId が null の場合、ローカル CareRecipient から Firestore の careRecipientMembers + careRecipients ドキュメントを自動作成。8ファイル変更（5 src + 3 test）

## 完了タスク

| Item | 概要 | Status |
|------|------|--------|
| v1.0-v2.2 | Clean Architecture + 5機能 + Firebase Auth/Firestore/FCM/Crashlytics + TDD リファクタリング | DONE |
| v3.0-v4.0 | バグ修正 + リマインダー + CI/CD + Paging 3 + Roborazzi + Widget + Root 検出 | DONE |
| v5.0-v7.0 | TDD リファクタリング + Root ダイアログ + Analytics + ProGuard + エクスポート(CSV/PDF) | DONE |
| v8.0-v8.1 | ホーム画面 + CareRecipient 拡張 + CalendarEvent 統合 + NoteComment + recurrence | DONE |
| v9.0 | Billing 基盤 + Member/Invitation + 招待 UI/E2E + DB v23 | DONE |
| v9.0-sec/test | Firestore Rules + Session timeout + PBKDF2 + TestDataFixtures + ResultMatchers | DONE |
| v10.0-tdd | MainCoroutineRule + ViewModel テスト移行 + ResultMatchers 全面採用 | DONE |
| Detekt/CI | 367→0 issues + CI グリーン化 + Detekt 1.23.7 | DONE |
| Task→CalendarEvent | DB v23→v25、80ファイル変更。CalendarEventType.TASK 統合 | DONE |
| Phase 1-8 | Billing 修正 + DB v25 ベースライン + CalendarEventReminder + Connectivity + biometric | DONE |
| Sec Phase 1-3 | データ保護 + 入力バリデーション + BiometricHelper DI + RootDetector 強化 | DONE |
| CLAUDE.md 軽量化 | 29KB→~9.5KB。詳細を docs/ARCHITECTURE.md に分離 | DONE |
| OSV-Scanner | OSV-Scanner v2 + dependency locking + CI workflow + Firebase BOM 34.9.0 | DONE |
| Roborazzi | golden image を CI (Linux) 基準に更新。16ファイル | DONE |
| AppConfig 分割 | AppConfig.kt を 7 ファイルに分割（delegation facade パターン） | DONE |
| 複数ケア対象者 | DAO 全件取得 + SharedPreferences 永続化 + HomeScreen 切替 UI | DONE |
| Phase 1 (Rules) | Firestore Rules isAuthorizedMember 統一 + careRecipientMembers Rules + 14 テスト | DONE |
| Phase 1B | Cloud Functions + PurchaseVerifier + BillingRepository 検証統合 | DONE |
| Dir Phase 0-3 | key.properties 確認 + .gitignore 整理 + osv-scanner.toml 統合 + fastlane 削除 | DONE |
| Dir Phase 4 | Firebase App Check (Play Integrity) 導入。debug スキップ、release のみ | DONE |
| Dir Phase 5 | Offline First 強化: 同期 Snackbar UI、lastSyncTime バグ修正、接続復帰即時同期 | DONE |
| firebase-tools | functions/ の firebase-tools 13.x→15.6.0 更新（devDependency のみ） | DONE |
| Phase 0 | applicationId 修正 (com.carenote.original.app → com.carenote.app)。7ファイル変更 | DONE |
| Phase 1 (Billing UI) | BillingScreen + BillingViewModel + SettingsScreen 簡略化 + UserMapper isPremium 動的化 | DONE |
| Phase 2 | HomeScreen.kt + CareNoteNavHost.kt 分割。706→256行、701→57行。6新規ファイル | DONE |
| Phase 3 (Firestore Setup) | SyncWorker 駆動の Firestore 初期セットアップフロー。setupInitialCareRecipient 新設 | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v25, SQLCipher 4.6.1, 13 Entity (TaskEntity 削除済み) |
| Firebase | BOM 34.9.0 (Auth, Firestore, Messaging, Crashlytics, Storage, Analytics, Functions) + No-Op フォールバック |
| Billing | Google Play Billing 7.1.1, BillingAvailability + NoOpBillingRepository + PurchaseVerifier パターン |
| DI 分割 | AppModule + RepositoryModule + ExporterModule + DatabaseModule + FirebaseModule + SyncModule + WorkerModule + BillingModule |
| セキュリティ | SQLCipher + EncryptedPrefs + Root検出 + 生体認証 + PBKDF2 + Session timeout + domain/validator/ + OSV-Scanner + App Check |
| テスト基盤 | MainCoroutineRule + TestBuilders (11モデル) + ResultMatchers (13種) + TestDataFixtures |
| Detekt | 1.23.7, maxIssues=0, Compose FunctionNaming 除外, LongParameterList functionThreshold=8 |
| CI | build-test + osv-scanner + e2e-test(soft-fail)。OSV-Scanner: PR/push/週次スケジュール + SARIF |
| ドキュメント構成 | CLAUDE.md (必須情報 ~9.5KB) + docs/ARCHITECTURE.md (詳細参照 ~17KB) |
| Offline First | SyncStatusViewModel + Snackbar UI + 接続復帰時 triggerImmediateSync + lastSyncTime PartialSuccess ガード |

## スコープ外 / 将来

- **Firestore orphaned membership cleanup**: setupInitialCareRecipient の二段階書き込みで careRecipients 作成が失敗した場合の orphaned membership ドキュメントのクリーンアップ
- **Firestore Rules 全面再設計（v2.0）**: 複数対象者の Firestore sync 対応。SyncMapping に careRecipientId 追加
- **FCM リモート通知**: Cloud Functions / バックエンド構築が前提
- **Wear OS 対応**: Horologist + Health Services、別モジュール必要
- **CSV データインポート**: 対象ユーザー適合性検証後
- **音声メモ**: Speech-to-Text 統合
- **介護保険認定書スキャン管理**: PDF 一元管理、期限切れアラート
- **Widget UX 改善**: タップでアクション（服薬記録等）追加
