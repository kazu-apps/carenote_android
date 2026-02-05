# HANDOVER.md - CareNote Android

## セッションステータス: 進行中

## 現在のタスク: Item 97 SettingsDataSource キー定数の整理

### Item 88: 5 標準 Syncer を ConfigDrivenEntitySyncer に置換

5 つの標準 Syncer（MedicationSyncer, NoteSyncer, HealthRecordSyncer, CalendarEventSyncer, TaskSyncer）を
`ConfigDrivenEntitySyncer` + `SyncerConfig` ベースに置き換え、約335行のボイラープレートを削除。
`MedicationLogSyncer` はカスタムロジック（サブコレクション同期）があるため変更なし。

### 変更ファイル
| ファイル | 操作 | 内容 |
|---------|------|------|
| `di/SyncModule.kt` | 変更 | 5 つの `@Provides` + `@Named` ファクトリ関数追加、`EntitySyncer<*, *>` 型で提供 |
| `data/repository/FirestoreSyncRepositoryImpl.kt` | 変更 | 具体 Syncer 型 → `EntitySyncer<*, *>` + `@Named` に変更 |
| `data/repository/sync/MedicationSyncer.kt` | 削除 | ConfigDrivenEntitySyncer に置換 |
| `data/repository/sync/NoteSyncer.kt` | 削除 | ConfigDrivenEntitySyncer に置換 |
| `data/repository/sync/HealthRecordSyncer.kt` | 削除 | ConfigDrivenEntitySyncer に置換 |
| `data/repository/sync/CalendarEventSyncer.kt` | 削除 | ConfigDrivenEntitySyncer に置換 |
| `data/repository/sync/TaskSyncer.kt` | 削除 | ConfigDrivenEntitySyncer に置換 |
| `test/.../FirestoreSyncRepositoryImplTest.kt` | 変更 | 具体 Syncer mock → `EntitySyncer<*, *>` mock に更新 |

### テスト結果
- `assembleDebug`: BUILD SUCCESSFUL
- `FirestoreSyncRepositoryImplTest`: 20テスト全パス
- `EntitySyncerTest`: 全パス
- `SyncerConfigTest`: 12テスト全パス
- `testDebugUnitTest` (全テスト): BUILD SUCCESSFUL

### セキュリティレビュー結果サマリー

| カテゴリ | ステータス | 詳細 |
|---------|----------|------|
| CRITICAL | 0 | 重大な脆弱性なし |
| HIGH | 0 | H-1 PII ログ漏洩 (Item 79 完了), H-2 メール検証 (Item 80 完了) |
| MEDIUM | 3 | Rate Limiting, 暗号化破損時復旧, ProGuard |
| LOW | 1 | FCM トークン管理（サーバー側実装待ち） |
| **全体リスクレベル** | **LOW** | HIGH 問題は全て対応完了 |

### 良好な点
- SQLCipher によるデータベース暗号化
- EncryptedSharedPreferences による設定データ保護
- 一貫した入力バリデーション（全 ViewModel）
- Firestore Security Rules による認可制御
- ネットワーク通信の HTTPS 強制
- バックアップからの機密データ除外

### v2.1 ロードマップ追加
- Item 79: ログへの PII 漏洩対策 (HIGH)
- Item 80: メール検証状態チェック追加 (HIGH)
- Item 81: パスワード強度要件強化 (LOW)

## 次のアクション

1. **v2.2 リファクタリング Phase 3**: `/task-exec` で Item 89 (SettingsSection テスト) から順次実行
2. **リリース準備**:
   - リリース APK の実機テスト
   - Google Play Console へのアップロード準備

## 既知の問題

### 未解決（要対応）

**セキュリティ (v2.1 で対応予定):**
- （なし — Item 80 で対応完了）

**その他:**
- 問い合わせメールがプレースホルダー (carenote.app.support@gmail.com) — リリース前に確認
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | M-5 | Room スキーマ JSON がコミット済み（プライベートリポジトリでは許容） |
| MEDIUM | Item 30 | ValidationUtils.kt が未使用のデッドコード（本番インポートなし） |
| MEDIUM | Item 32 | WorkManager/HiltWork の事前実装（v2.0 通知用） |
| MEDIUM | Item 32 | JaCoCo `**/util/*` 除外が広範囲（テストは存在） |
| MEDIUM | Item 31 | テスト品質: Mapper ラウンドトリップ不完全、Repository Turbine 未使用、ViewModel Loading→Success テスト欠落 |
| LOW | L-4 | 全 DAO が OnConflictStrategy.REPLACE 使用（マルチデバイス同期リスク） |
| LOW | Item 32 | POST_NOTIFICATIONS の事前宣言（無害） |
| LOW | Item 32 | 2024年後半の依存関係（CVE なし、一貫性あり） |
| LOW | Item 99 | FCM リモート通知の受信処理が未実装（`onMessageReceived` がログのみ） |
| LOW | Item 99 | FCM トークンのサーバー送信が未実装（`onNewToken` がログのみ） |
| LOW | Item 100 | 個別 Screen ファイルの UI ハードコード値が未置換（共有コンポーネントのみ完了） |
| INFO | — | 削除確認ダイアログが UI から到達不可（スワイプ削除の準備） |
| INFO | — | Flow `.catch` が欠落（Room Flow は安定、低リスク） |

## PENDING 項目

### Item 53: デバッグログから PII を削除 - DONE
`AddMedicationViewModel` で服薬名をログ出力している箇所を ID のみに変更。L-2 の対応。
- 種別: 実装
- 対象: `AddMedicationViewModel.kt`
- ファイル数: 1
- セキュリティ ID: L-2

---

---

## v2.0 ロードマップ

v2.0 は以下の 5 つのフェーズで構成。各フェーズは依存関係に従って順次実行。

### フェーズ 1: Firebase プロジェクト設定

---

### Item 55: Firebase SDK 依存関係追加 - DONE
Firebase BOM と必要な SDK を gradle に追加。google-services プラグイン設定。
- 種別: 実装
- 対象: `build.gradle.kts` (project/app), `libs.versions.toml`
- ファイル数: 3
- 依存: なし

---

### Item 56: google-services.json 設定ガイド - DONE
Firebase Console でプロジェクト作成し、google-services.json を配置する手順を文書化。
- 種別: 調査 + ドキュメント
- 対象: `docs/FIREBASE_SETUP.md`
- ファイル数: 1
- 依存: Item 55 完了後
- 注記: google-services.json は既に .gitignore に含まれていた（追加作業不要）

---

### フェーズ 2: Firebase Auth（認証）

---

### Item 57: AuthRepository インターフェース定義 - DONE
Firebase Auth 用の Repository インターフェースを domain 層に追加。
- 種別: 実装
- 対象: `domain/repository/AuthRepository.kt`
- ファイル数: 1
- 依存: Item 55 完了後
- 注記: User.kt は既存で十分（変更不要）

---

### Item 58: FirebaseAuthRepositoryImpl 実装 - DONE
AuthRepository の Firebase 実装と FirebaseModule (Hilt DI)。
- 種別: 実装
- 対象: `data/repository/FirebaseAuthRepositoryImpl.kt`, `di/FirebaseModule.kt`, `data/mapper/UserMapper.kt`
- ファイル数: 3
- 依存: Item 57 完了後
- 追加: `kotlinx-coroutines-play-services` 依存関係 (Firebase Task → suspend 変換用)

---

### Item 59: LoginScreen / RegisterScreen 実装 - DONE
ログイン・新規登録 UI と ViewModel。メールアドレス + パスワード認証。
- 種別: 実装
- 対象: `ui/screens/auth/LoginScreen.kt`, `RegisterScreen.kt`, `ForgotPasswordScreen.kt`, `AuthViewModel.kt`
- ファイル数: 4
- 依存: Item 58 完了後
- 追加変更: `AppConfig.Auth`, `CareNoteTextField` (visualTransformation), `Screen.kt`, `CareNoteNavHost.kt`, `strings.xml` (JP/EN)

---

### Item 60: 認証フロー ナビゲーション統合 - DONE
未認証時はログイン画面へリダイレクト。認証状態を観察して画面遷移を制御。
- 種別: 実装
- 対象: `ui/navigation/CareNoteNavHost.kt`, `Screen.kt`, `MainActivity.kt`
- ファイル数: 3
- 依存: Item 59 完了後

---

### Item 61: Auth ユニットテスト - DONE
AuthRepository の Fake 実装と ViewModel テスト。47テスト作成。Robolectric 追加。
- 種別: テスト
- 対象: `test/.../fakes/FakeAuthRepository.kt`, `AuthViewModelTest.kt`
- ファイル数: 2 + gradle 設定 2
- 依存: Item 60 完了後
- 注記: カバレッジ 73%（プロジェクト全体）— 閾値 80% は他機能のテスト追加で対応

---

### フェーズ 3: Cloud Firestore（データ同期）

---

### Item 62: Firestore ドキュメント設計 - DONE
各エンティティの Firestore コレクション/ドキュメント構造を設計。被介護者中心のサブコレクション構造、ID マッピング、Security Rules、複合インデックス定義。
- 種別: 調査 + ドキュメント
- 対象: `docs/FIRESTORE_SCHEMA.md`
- ファイル数: 1
- 依存: Item 58 完了後

---

### Item 63: Remote Mapper 実装 - DONE
Firestore Document ↔ Domain Model のマッパー群。SyncMetadata, RemoteMapper インターフェース, FirestoreTimestampConverter, 6つの Mapper 実装。
- 種別: 実装
- 対象: `data/remote/model/`, `data/mapper/remote/`
- ファイル数: 9
- 依存: Item 62 完了後

---

### Item 64: SyncRepository インターフェース定義 - DONE
ローカル ↔ クラウド同期用の Repository インターフェース。SyncResult, SyncState sealed class 含む。
- 種別: 実装
- 対象: `domain/repository/SyncRepository.kt`, `domain/common/SyncResult.kt`, `domain/common/SyncState.kt`
- ファイル数: 3
- 依存: Item 62 完了後

---

### Item 65: FirestoreSyncRepositoryImpl 実装 - DONE
SyncRepository の Firestore 実装。ID マッピング、EntitySyncer 抽象クラス、6 Syncer、競合解決ロジック含む。
- 種別: 実装
- 対象: `data/local/entity/SyncMappingEntity.kt`, `data/local/dao/SyncMappingDao.kt`, `data/local/migration/Migrations.kt`, `data/local/CareNoteDatabase.kt`, `di/DatabaseModule.kt`, `di/FirebaseModule.kt`, `data/local/SettingsDataSource.kt`, `config/AppConfig.kt`, `data/repository/sync/*.kt`, `data/repository/FirestoreSyncRepositoryImpl.kt`, `di/SyncModule.kt`
- ファイル数: 14 (新規 11, 変更 3)
- 依存: Item 63, 64 完了後

---

### Item 66: バックグラウンド同期 Worker - DONE
WorkManager で定期的にクラウド同期を実行。
- 種別: 実装
- 対象: `data/worker/SyncWorker.kt`, `data/worker/SyncWorkScheduler.kt`, `di/WorkerModule.kt`, `config/AppConfig.kt`
- ファイル数: 4
- 依存: Item 65 完了後

---

### Item 67: 同期 UI（設定画面拡張） - DONE
設定画面に同期オン/オフ、手動同期ボタン、最終同期日時表示を追加。
- 種別: 実装
- 対象: `ui/screens/settings/SettingsScreen.kt`, `SettingsViewModel.kt`, `strings.xml` (JP/EN), `UserSettings.kt`, `SettingsDataSource.kt`, `SettingsRepository.kt`, `SettingsRepositoryImpl.kt`, `AuthViewModel.kt`, `ClickablePreference.kt`, `SwitchPreference.kt`
- ファイル数: 12
- 依存: Item 66 完了後

---

### Item 68: Firestore 同期テスト - DONE
SyncRepository の Fake 実装と SyncWorkerTest。SyncWorkSchedulerInterface 導入でテスト時の WorkManager 依存を排除。
- 種別: テスト
- 対象: `test/.../fakes/FakeSyncRepository.kt`, `FakeSyncWorkScheduler.kt`, `SyncWorkerTest.kt`, `data/worker/SyncWorkSchedulerInterface.kt`
- ファイル数: 11 (新規 4, 変更 7)
- 依存: Item 67 完了後

---

### フェーズ 4: Firebase Cloud Messaging（プッシュ通知）

---

### Item 69: FCM 依存関係と基本設定 - DONE
firebase-messaging を追加し、FirebaseMessagingService を実装。
- 種別: 実装
- 対象: `config/AppConfig.kt`, `di/FirebaseModule.kt`, `data/service/CareNoteMessagingService.kt`, `AndroidManifest.xml`
- ファイル数: 4
- 依存: Item 55 完了後

---

### Item 70: 通知チャンネル設定 - DONE
Android 8+ 向け NotificationChannel 作成と NotificationHelper ユーティリティ。
- 種別: 実装
- 対象: `config/AppConfig.kt`, `strings.xml` (JP/EN), `ui/util/NotificationHelper.kt`, `CareNoteApplication.kt`
- ファイル数: 5
- 依存: Item 69 完了後

---

### Item 71: 服薬リマインダー通知 - DONE
設定された服薬時間に WorkManager からローカル通知を発行。
- 種別: 実装
- 対象: `data/worker/MedicationReminderWorker.kt`, `MedicationReminderScheduler.kt`, `ui/util/NotificationHelper.kt`, `res/drawable/ic_notification_medication.xml`, `strings.xml` (JP/EN)
- ファイル数: 7
- 依存: Item 70 完了後

---

### Item 72: 通知テスト - DONE
NotificationHelper と MedicationReminderWorker のユニットテスト。FakeNotificationHelper を追加し、定数・計算ロジック・おやすみ時間判定をテスト。
- 種別: テスト
- 対象: `test/.../fakes/FakeNotificationHelper.kt`, `NotificationHelperTest.kt`, `MedicationReminderWorkerTest.kt`
- ファイル数: 3 + gradle 変更 1
- 依存: Item 71 完了後
- 結果: 44 テスト追加（MedicationReminderWorkerTest 32、NotificationHelperTest 12）

---

### フェーズ 5: Firebase Crashlytics + リリース品質

---

### Item 73: Crashlytics 依存関係と設定 - DONE
firebase-crashlytics を追加し、build.gradle に crashlytics プラグイン設定。
- 種別: 実装
- 対象: `build.gradle.kts` (project/app), `libs.versions.toml`
- ファイル数: 3
- 依存: Item 55 完了後
- 注記: Item 55 で既に設定済みのため、確認のみで完了

---

### Item 74: CrashlyticsTree 実装 (Timber + Crashlytics) - DONE
リリースビルドで Timber ログを Crashlytics に送信する Tree 実装。L-1 対応。
- 種別: 実装
- 対象: `ui/util/CrashlyticsTree.kt`, `CareNoteApplication.kt`
- ファイル数: 2
- 依存: Item 73 完了後
- セキュリティ ID: L-1
- 結果: WARN 以上のログを Crashlytics に送信、例外は recordException() で記録

---

### Item 75: ProGuard ルール更新 - DONE
Firebase SDK 用の ProGuard keep ルールを追加。Crashlytics スタックトレース可読化、Firestore モデル保持、FCM サービス保持。
- 種別: 実装
- 対象: `app/proguard-rules.pro`
- ファイル数: 1
- 依存: Item 74 完了後
- 追加ルール: SourceFile/LineNumberTable 属性保持, Remote Model keep, MessagingService keep, Firebase/GMS dontwarn

---

### Item 76: v2.0 E2E テスト拡張 - DONE
認証フロー、同期フローの E2E テストを追加。Firebase 依存を TestFirebaseModule で Fake 実装に置換。
- 種別: テスト
- 対象: `androidTest/.../fakes/` (3), `di/TestFirebaseModule.kt`, `e2e/E2eTestBase.kt`, `e2e/AuthFlowTest.kt`, `e2e/SyncFlowTest.kt`
- ファイル数: 7 (新規 6, 変更 1)
- 依存: Item 68, 72, 75 完了後
- テストケース: AuthFlowTest 5, SyncFlowTest 6

---

### Item 77: v2.0 コードレビュー - DONE
Firebase 統合全体のセキュリティ・品質レビュー実施。PII ログ漏洩修正（L-2）、MigrationsTest.kt 更新。
- 種別: コードレビュー
- 対象: 全 Firebase 関連ファイル（61ファイル対象、3ファイル修正）
- 修正内容:
  - `FirebaseAuthRepositoryImpl.kt`: PII ログ 7箇所削除
  - `AuthViewModel.kt`: PII ログ 2箇所削除
  - `MigrationsTest.kt`: MIGRATION_6_7 対応（テスト更新 + 新規テスト追加）
- 依存: Item 76 完了後

---

### Item 78: CLAUDE.md v2.0 更新 - DONE
Firebase 関連の技術スタック、パターン、注意事項を CLAUDE.md に追記。
- 種別: ドキュメント
- 対象: `CLAUDE.md`
- ファイル数: 1
- 依存: Item 77 完了後
- 追加内容: Firebase 統合セクション、Worker パターン、PII ログ禁止ルール、よくある落とし穴 4項目

---

## v2.1 セキュリティ強化ロードマップ

セキュリティレビュー（2026-02-04）で発見された問題に対応。

### Item 79: ログへの PII 漏洩対策 - DONE
Firebase Auth エラーメッセージからメールアドレス等の PII をマスク処理。
- 種別: 実装
- 重要度: HIGH
- 対象: `FirebaseAuthRepositoryImpl.kt`
- ファイル数: 1
- 内容: `sanitizeErrorMessage()` 関数で email パターンを `[EMAIL]` に置換

---

### Item 80: メール検証状態チェック追加 - DONE
ログイン時に `isEmailVerified` をチェックし、未検証ユーザーに警告 Snackbar 表示。
- 種別: 実装
- 重要度: HIGH
- 対象: `User.kt`, `UserMapper.kt`, `AuthViewModel.kt`, `strings.xml` (JP/EN), `FakeAuthRepository.kt`, `AuthViewModelTest.kt`
- ファイル数: 7
- 内容: 未検証の場合は Snackbar で警告表示、機能制限は将来検討

---

### Item 81: パスワード強度要件強化 - DONE
最小パスワード長を 6 → 8 文字に変更。
- 種別: 実装
- 重要度: LOW
- 対象: `AppConfig.kt`, `strings.xml` (JP/EN)
- ファイル数: 3
- 内容: NIST SP 800-63B ガイドライン準拠

---

## v2.2 リファクタリングロードマップ (TDD)

v2.1 リリース後の安定期に実施する技術的負債解消。**TDD (テスト駆動開発)** を適用。

### TDD ワークフロー

各リファクタ項目は以下のサイクルで実行:

```
1. RED:    新しいインターフェース/クラスのテストを先に書く（失敗）
2. GREEN:  テストが通る最小限の実装を書く
3. REFACTOR: コードを整理し、既存テストが全てパスすることを確認
```

### 発見された問題サマリー

| カテゴリ | 件数 | 内容 |
|---------|------|------|
| コード重複 | 7 | Syncer クラスの構造的重複（67行 × 6 = 約400行の重複） |
| 巨大ファイル | 4 | SettingsScreen 462行, AddEditCalendarEventScreen 387行 等 |
| 複雑なクラス | 2 | AuthViewModel 316行（3フォーム管理）, SettingsViewModel 205行 |
| テスト不足 | 3領域 | RemoteMapper 8ファイル, EntitySyncer 7ファイル, FirestoreSyncRepositoryImpl |
| 未使用コード | 2 | AppConfig.Premium, TODO コメント |
| ハードコード | 2 | SettingsDataSource キー, UI padding/size |

---

### フェーズ 1: レガシーコード保護 (Low Risk)

**目的**: 既存コードにテストを追加し、リファクタリングの安全網を構築

> 注: Phase 1 は「既存コードへのテスト追加」であり、厳密なTDDではない。
> これはレガシーコード改善パターン（Michael Feathers の Working Effectively with Legacy Code）に従う。

---

### Item 82: RemoteMapper ユニットテスト追加 - DONE
Firestore ↔ Domain 変換のテストを追加し、Item 63 の品質を担保。
- 種別: テスト (レガシー保護)
- 対象: `test/.../data/mapper/remote/` (7ファイル新規)
- ファイル数: 7
- 依存: なし
- テスト項目:
  - toDomain: 正常ケース、必須フィールド欠落、不正な Enum 値
  - toRemote: 全フィールド変換、syncMetadata 有無
  - extractSyncMetadata: 正常/異常ケース
  - ラウンドトリップ: domain → remote → domain
- 結果: 150テスト追加（FirestoreTimestampConverter 21、各 Mapper 約20テスト）

---

### Item 83: EntitySyncer ユニットテスト追加 - DONE
同期基底クラスのテンプレートメソッドをテスト。FakeSyncMappingDao + TestEntitySyncer 作成。
- 種別: テスト (レガシー保護)
- 対象: `test/.../fakes/FakeSyncMappingDao.kt`, `test/.../data/repository/sync/TestEntitySyncer.kt`, `EntitySyncerTest.kt`
- ファイル数: 3
- 依存: Item 82 完了後
- テスト項目:
  - sync(): Success, PartialSuccess, Failure パス
  - pushLocalChanges(): アップロード成功/失敗、lastSyncTime 判定
  - mergeResults(): 各組み合わせパターン
  - mapException(): 例外→DomainError 変換
- 結果: 20テスト追加

---

### Item 84: FirestoreSyncRepositoryImpl テスト追加 - DONE
同期リポジトリ全体のテスト。6つの EntitySyncer を MockK でモック化。
- 種別: テスト (レガシー保護)
- 対象: `test/.../data/repository/FirestoreSyncRepositoryImplTest.kt`
- ファイル数: 1
- 依存: Item 83 完了後
- テスト項目: syncAll(), 個別 sync メソッド, syncMedicationLogs, pushLocalChanges, pullRemoteChanges, syncState Flow
- 結果: 20テスト追加

---

### フェーズ 2: Syncer 重複解消 (TDD)

**目的**: 6 つの Syncer 実装の重複コードを解消し、保守性を向上

---

### Item 85: SyncerConfig テスト作成 (RED) - DONE
SyncerConfig data class のインターフェースを設計し、テストを先に書く。
- 種別: テスト (TDD - RED)
- 対象: `test/.../data/repository/sync/SyncerConfigTest.kt` (新規)
- ファイル数: 1
- 依存: Item 83 完了後
- TDD サイクル:
  1. **RED**: SyncerConfig の期待する振る舞いをテストで定義
  2. テストは失敗する（実装がないため）
- 結果: 12テスト追加。コンパイルエラーで RED 状態確認済み

---

### Item 86: SyncerConfig 実装 (GREEN) - DONE
テストが通る最小限の SyncerConfig を実装。
- 種別: 実装 (TDD - GREEN)
- 対象: `data/repository/sync/SyncerConfig.kt`, `data/repository/sync/ConfigDrivenEntitySyncer.kt` (新規)
- ファイル数: 2
- 依存: Item 85 完了後
- TDD サイクル:
  1. **GREEN**: Item 85 のテストが通る実装を書く
  2. 全テストがパスすることを確認
- 結果: SyncerConfig (data class, 13プロパティ) + ConfigDrivenEntitySyncer (EntitySyncer 委譲) 作成。12テスト全パス

---

### Item 87: EntitySyncer 委譲パターン適用 (REFACTOR) - DONE
設計判断で EntitySyncer 変更不要と判定。ConfigDrivenEntitySyncer (Item 86) が委譲パターンを実現済み。
- 種別: リファクタ (TDD - REFACTOR) → アーキテクチャ判断
- 対象: `data/repository/sync/EntitySyncer.kt` (変更なし)
- ファイル数: 0
- 依存: Item 86 完了後
- 判定理由:
  1. ConfigDrivenEntitySyncer が EntitySyncer を継承し SyncerConfig 委譲を実現済み
  2. EntitySyncer 変更は MedicationLogSyncer（カスタムロジック）に影響
  3. Open/Closed Principle: 基底クラス変更不要、拡張で対応

---

### Item 88: 個別 Syncer 簡素化 - DONE
5 つの標準 Syncer を ConfigDrivenEntitySyncer + SyncerConfig に置換し削除。MedicationLogSyncer は変更なし。
- 種別: リファクタ (TDD - REFACTOR)
- 対象: `SyncModule.kt`, `FirestoreSyncRepositoryImpl.kt`, 5 Syncer ファイル削除
- ファイル数: 変更 3, 削除 5
- 依存: Item 87 完了後
- TDD サイクル:
  1. **REFACTOR**: Item 83-84 のテスト（EntitySyncerTest 20, SyncerConfigTest 12, FirestoreSyncRepositoryImplTest 20）が全てパス
  2. 削減量: 約335行（5ファイル × 67行）

---

### フェーズ 3: 巨大ファイル分割 (TDD)

**目的**: 400行超のファイルを分割し、可読性と保守性を向上

---

### Item 89: SettingsSection コンポーネントテスト作成 (RED) - DONE
分割後の各セクションコンポーネントのテストを先に書く。
- 種別: テスト (TDD - RED)
- 対象: `test/.../ui/screens/settings/sections/SettingsSectionsTest.kt` (新規), `build.gradle.kts` (変更)
- ファイル数: 2
- 依存: なし
- TDD サイクル:
  1. **RED**: ThemeSection, SyncSection, NotificationSection, HealthThresholdSection, MedicationTimeSection, AppInfoSection のテストを定義
  2. Compose UI Test (Robolectric) で各セクションの表示・操作をテスト
- 結果: 21テスト追加。6セクション全てが Unresolved reference でコンパイルエラー（RED 確認済み）
- 追加依存: `testImplementation(compose-bom)`, `testImplementation(compose-ui-test-junit4)`

---

### Item 90: SettingsScreen セクション分割 (GREEN/REFACTOR) - DONE
462行の SettingsScreen を機能セクションごとに分割。348行に削減。
- 種別: 実装 + リファクタ (TDD - GREEN/REFACTOR)
- 対象: `ui/screens/settings/SettingsScreen.kt`, `sections/*.kt` (6ファイル新規)
- ファイル数: 7 新規 + 2 変更
- 依存: Item 89 完了後
- TDD サイクル:
  1. **GREEN**: Item 89 のテストが通るセクションコンポーネントを実装
  2. **REFACTOR**: SettingsScreen から各セクションを抽出
- 追加変更:
  - `build.gradle.kts`: `testOptions.unitTests.isIncludeAndroidResources = true` 追加（Robolectric + Compose UI テスト必須）
  - `SettingsSectionsTest.kt`: `@Config(qualifiers = "ja")` 追加、あいまいな文字列マッチャー修正
- 結果: 21テスト全パス (GREEN)、全テスト回帰なし

---

### Item 91: SettingsDialogState テスト作成 (RED) - DONE
ダイアログ状態管理の sealed class をテストで設計。
- 種別: テスト (TDD - RED)
- 対象: `test/.../ui/screens/settings/SettingsDialogStateTest.kt` (新規)
- ファイル数: 1
- 依存: Item 90 完了後
- TDD サイクル:
  1. **RED**: 12 状態 (None + 11 ダイアログ) の sealed class + ヘルパー拡張関数 (isTimePicker, isNumberInput, isConfirm) をテストで定義
- 結果: 10テスト追加。`Unresolved reference 'SettingsDialogState'` コンパイルエラーで RED 確認済み

---

### Item 92: SettingsScreen ダイアログ分離 (GREEN/REFACTOR) - DONE
ダイアログ状態を sealed class (12状態) に統合し、表示ロジックを SettingsDialogs に分離。
- 種別: 実装 + リファクタ (TDD - GREEN/REFACTOR)
- 対象: `SettingsDialogState.kt`, `dialogs/SettingsDialogs.kt` (新規), `SettingsScreen.kt` (変更)
- ファイル数: 2 新規 + 1 変更
- 依存: Item 91 完了後
- TDD サイクル:
  1. **GREEN**: SettingsDialogState sealed class + 3 拡張関数 (isTimePicker, isNumberInput, isConfirm) → 10テスト全パス
  2. **REFACTOR**: 11 boolean → 1 SettingsDialogState、ダイアログ表示ロジックを SettingsDialogs に抽出
- 結果: SettingsScreen 348→193行（約155行削減）、全テスト回帰なし（SettingsDialogStateTest 10, SettingsSectionsTest 21, SettingsViewModelTest 18）

---

### Item 93: AuthFormHandler テスト作成 (RED) - DONE
分割後のフォームハンドラーのテストを先に書く。
- 種別: テスト (TDD - RED)
- 対象: `test/.../ui/screens/auth/LoginFormHandlerTest.kt`, `RegisterFormHandlerTest.kt`, `ForgotPasswordFormHandlerTest.kt`, `AuthValidatorsTest.kt` (新規)
- ファイル数: 4
- 依存: なし
- TDD サイクル:
  1. **RED**: 各フォームハンドラーの状態管理・バリデーションをテストで定義
- 結果: 47テスト追加（AuthValidatorsTest 14, LoginFormHandlerTest 12, RegisterFormHandlerTest 12, ForgotPasswordFormHandlerTest 9）。`Unresolved reference` コンパイルエラーで RED 確認済み。既存 AuthViewModelTest 47テスト回帰なし

---

### Item 94: AuthViewModel 分割 (GREEN/REFACTOR) - DONE
316行の AuthViewModel を AuthValidators + 3 FormHandler に分割し、ViewModel は薄い委譲レイヤーに。
- 種別: 実装 + リファクタ (TDD - GREEN/REFACTOR)
- 対象: `ui/screens/auth/AuthValidators.kt`, `LoginFormHandler.kt`, `RegisterFormHandler.kt`, `ForgotPasswordFormHandler.kt` (新規), `AuthViewModel.kt` (変更)
- ファイル数: 4 新規 + 1 変更
- 依存: Item 93 完了後
- TDD サイクル:
  1. **GREEN**: AuthValidators object + 3 FormHandler 実装 → Item 93 の 47 テスト全パス
  2. **REFACTOR**: AuthViewModel 316行 → 89行（共有 SnackbarController/authSuccessEvent を各ハンドラーに注入）
- 結果: AuthValidatorsTest 14, LoginFormHandlerTest 12, RegisterFormHandlerTest 13, ForgotPasswordFormHandlerTest 9 (計 48) 全パス + 既存 AuthViewModelTest 49 全パス

---

### Item 95: SettingsViewModel 汎用化テスト作成 (RED) - DONE
updateSetting() 統合リファクタリングの安全網として、Snackbar パターンテストを作成。
- 種別: テスト (TDD - RED)
- 対象: `test/.../ui/screens/settings/SettingsViewModelUpdateTest.kt` (新規)
- ファイル数: 1
- 依存: なし
- TDD サイクル:
  1. **RED**: 汎用 updateSetting() の成功/失敗パターンをテストで定義
- 結果: 12テスト追加（標準成功 9, 失敗 2, カスタムメッセージ 1）。既存コードが正しく動作するため全パス（GREEN）。Item 96 のリファクタ後も GREEN 維持を保証
- 既存 SettingsViewModelTest 18テスト回帰なし

---

### Item 96: SettingsViewModel update* メソッド整理 (GREEN/REFACTOR) - DONE
9 個の update* メソッドを汎用的な `updateSetting()` に統合。
- 種別: 実装 + リファクタ (TDD - GREEN/REFACTOR)
- 対象: `ui/screens/settings/SettingsViewModel.kt`
- ファイル数: 1
- 依存: Item 95 完了後
- TDD サイクル:
  1. **GREEN**: `updateSetting()` private メソッド導入（logTag, successMsg, failureMsg, onSuccess コールバック, action）
  2. **REFACTOR**: 9 メソッド（toggleNotifications, updateQuietHours, updateTemperatureThreshold, updateBloodPressureThresholds, updatePulseThresholds, updateMedicationTime, updateThemeMode, toggleSyncEnabled, resetToDefaults）を 1-5 行に簡素化。triggerManualSync は異なるパターンのため除外
  3. 削減量: 205行 → 147行
- 結果: SettingsViewModelUpdateTest 12テスト全パス、SettingsViewModelTest 18テスト回帰なし

---

### フェーズ 4: コード品質改善 (Low Risk)

**目的**: ハードコード値の排除、未使用コードの削除

> 注: Phase 4 は単純な削除/リネームのため、既存テストで検証可能。TDDサイクル不要。

---

### Item 97: SettingsDataSource キー定数の整理 - DONE
`updateMedicationTime` シグネチャを `MedicationTiming` ベースに変更し、キーマッピングを DataSource 内に移動。companion object の重複キー 6 定数を削除。
- 種別: リファクタ
- 対象: `data/local/SettingsDataSource.kt`, `data/repository/SettingsRepositoryImpl.kt`, `test/.../SettingsRepositoryImplTest.kt`
- ファイル数: 3
- 依存: なし
- 結果: SettingsRepositoryImplTest 全パス、SettingsViewModelTest 全パス、assembleDebug BUILD SUCCESSFUL

---

### Item 98: AppConfig.Premium 未使用コード削除 - DONE
未使用の Premium 設定を削除（v3.0 で再追加予定）。Premium オブジェクト全体、Notification 内のプレミアム関連定数 3つ、Fcm.TOPIC_PREMIUM を削除。
- 種別: リファクタ
- 対象: `config/AppConfig.kt`
- ファイル数: 1
- 依存: なし
- 削減量: 約25行
- 結果: assembleDebug BUILD SUCCESSFUL、全ユニットテスト回帰なし

---

### Item 99: TODO コメント整理 - DONE
コードベース全体の TODO を調査し、2件の TODO コメントを削除。未実装機能は KDoc に記録し、HANDOVER.md の「既知の問題」に転記。
- 種別: ドキュメント
- 対象: `data/service/CareNoteMessagingService.kt`
- ファイル数: 1
- 依存: なし
- 結果: `app/src/main/java` 内の TODO/FIXME/HACK/XXX コメントがゼロであることを確認

---

### Item 100: UI ハードコード値の AppConfig 移行 - DONE
padding/size/spacing 等の UI 定数 15個を AppConfig.UI に追加し、共有コンポーネント 6ファイルのハードコード値を定数に置換。
- 種別: リファクタ
- 対象: `config/AppConfig.kt`, `CareNoteCard.kt`, `LoadingIndicator.kt`, `EmptyState.kt`, `ErrorDisplay.kt`, `ClickablePreference.kt`, `SwitchPreference.kt`
- ファイル数: 7
- 依存: なし
- 結果: assembleDebug BUILD SUCCESSFUL。値の変更なし（定数化のみ）

---

### フェーズ 5: Mapper 統合 (High Risk / 将来)

**目的**: Local Mapper と Remote Mapper の重複ロジックを統合

---

### Item 101: 統合 Mapper インターフェース設計 - PENDING
Local/Remote Mapper の統合設計を ADR として文書化。
- 種別: 調査 + 設計
- 対象: `docs/ADR-002-UNIFIED-MAPPER.md` (新規)
- ファイル数: 1
- 依存: Item 82 完了後
- 注意: 実装は v3.0 以降に検討

---

### Item 102: Mapper 統合実装 - PENDING (将来)
設計に基づき全 Mapper を統合（高リスクのため v3.0 以降）。TDD で実施。
- 種別: リファクタ (TDD)
- 対象: 全 Mapper ファイル (12ファイル)
- ファイル数: 12
- 依存: Item 101 完了後
- リスク: **高**（全データ変換に影響）

---

### TDD リファクタリング依存関係図

```
Phase 1 (レガシー保護)
├── Item 82: RemoteMapper テスト
├── Item 83: EntitySyncer テスト ← Item 82
└── Item 84: FirestoreSyncRepository テスト ← Item 83

Phase 2 (Syncer TDD)
├── Item 85: SyncerConfig テスト (RED) ← Item 83
├── Item 86: SyncerConfig 実装 (GREEN) ← Item 85
├── Item 87: EntitySyncer リファクタ (REFACTOR) ← Item 86
└── Item 88: 個別 Syncer 簡素化 ← Item 87

Phase 3 (ファイル分割 TDD) — Phase 1-2 と並行可能
├── Item 89: SettingsSection テスト (RED)
├── Item 90: SettingsScreen 分割 (GREEN/REFACTOR) ← Item 89
├── Item 91: SettingsDialogState テスト (RED) ← Item 90
├── Item 92: SettingsScreen ダイアログ分離 (GREEN/REFACTOR) ← Item 91
├── Item 93: AuthFormHandler テスト (RED)
├── Item 94: AuthViewModel 分割 (GREEN/REFACTOR) ← Item 93
├── Item 95: SettingsViewModel テスト (RED)
└── Item 96: SettingsViewModel 整理 (GREEN/REFACTOR) ← Item 95

Phase 4 (コード品質) — 他と並行可能、TDD 不要
├── Item 97: SettingsDataSource キー整理
├── Item 98: AppConfig.Premium 削除
├── Item 99: TODO 整理
└── Item 100: UI ハードコード整理

Phase 5 (Mapper 統合 TDD) — 将来
├── Item 101: 設計 ← Item 82
└── Item 102: 実装 (TDD) ← Item 101
```

### リスク評価

| Item | フェーズ | TDD | リスク | 理由 |
|------|----------|-----|--------|------|
| 82-84 | 1 | - | Low | レガシー保護、本番コード変更なし |
| 85-88 | 2 | Yes | Medium | Syncer 構造変更、TDD で安全に実施 |
| 89-96 | 3 | Yes | Low-Medium | UI/ViewModel 分割、TDD で検証 |
| 97-100 | 4 | No | Low | 単純な削除/リネーム、既存テストで検証 |
| 101 | 5 | - | None | 調査・設計のみ |
| 102 | 5 | Yes | **High** | 全データ変換に影響、TDD 必須 |

### 推奨実行順序

1. **Phase 1 (Item 82-84)** — レガシー保護を先に整備
2. **Phase 4 (Item 97-100)** — 低リスクの品質改善を並行実行
3. **Phase 3 (Item 89-92)** — SettingsScreen TDD 分割
4. **Phase 2 (Item 85-88)** — Syncer TDD リファクタ
5. **Phase 3 (Item 93-96)** — ViewModel TDD 分割
6. **Phase 5 (Item 101-102)** — v3.0 以降に検討

### 見積もり

| フェーズ | Item 数 | TDD サイクル | 推定タスク数 |
|----------|---------|--------------|--------------|
| Phase 1 | 3 | - | 3 |
| Phase 2 | 4 | RED→GREEN→REFACTOR | 4 |
| Phase 3 | 8 | RED→GREEN/REFACTOR | 8 |
| Phase 4 | 4 | - | 4 |
| Phase 5 | 2 | TDD (将来) | 2 |
| **合計** | **21** | - | **21** |

---

## 将来（v3.0 以降）

### Cloud Storage（写真保存）
- StorageRepository インターフェース
- 画像アップロード/ダウンロード機能
- 健康記録への写真添付

### Google Play Billing（プレミアムサブスクリプション）
- BillingRepository インターフェース
- サブスクリプション管理 UI
- プレミアム機能のゲート

---

## v1.0 完了項目

### Item 54: Timber リリースビルド準備 - v2.0 統合（Item 74）
リリースビルドで NoOpTree または CrashlyticsTree を plant する準備。v2.0 で Crashlytics 導入時に対応予定。
- 種別: 実装
- 対象: `CareNoteApplication.kt`
- ファイル数: 1
- セキュリティ ID: L-1
- 注記: Item 74 として v2.0 ロードマップに統合

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
| 33 | Fake shouldFail フラグ + MedicationVM Snackbar 修正 | DONE |
| 34 | MedicationViewModelTest StandardTestDispatcher + 失敗テスト | DONE |
| 35 | AddMedicationVMTest + AddEditHealthRecordVMTest 拡張 | DONE |
| 36 | SettingsRepositoryImplTest DB 例外パス（+8テスト） | DONE |
| 37 | E2E インフラ（TestDatabaseModule、tearDown、待機ガード、サイレント失敗修正） | DONE |
| 38 | E2E テスト品質（検索マッチャー、月ナビゲーションアサーション、タイムアウト統一） | DONE |
| 39 | STOP_TIMEOUT_MS → AppConfig.UI 集約 | DONE |
| 40 | Snackbar メッセージ → strings.xml (JP/EN) | DONE |
| 41 | SettingsViewModel バリデーションエラー i18n | DONE |
| 42 | AddEdit ViewModel 保存失敗 Snackbar | DONE |
| 43 | MedicationDetailViewModelTest（6テスト） | DONE |
| 44 | AddEditHealthRecordScreen 分割（VitalSigns/Lifestyle セクション） | DONE |
| 45 | savedTask → savedEvent リネーム | DONE |
| 46 | バックアップルールから DB 除外（H-2 部分対応） | DONE |
| 47 | Room DB 暗号化（SQLCipher + Keystore）— H-1 修正 | DONE |
| 48 | テキスト入力 MAX_LENGTH バリデーション — M-1 修正 | DONE |
| 49 | .gitignore から .idea/ 除外 — M-3 修正 | DONE |
| 50 | DataStore → EncryptedSharedPreferences 暗号化 — M-2 修正 | DONE |
| 51 | networkSecurityConfig — L-3 修正 | DONE |
| 52 | 暗号化設定ファイルをバックアップ除外 — H-2 完全修正 | DONE |
| 53 | デバッグログから PII 削除 — L-2 修正 | DONE |
| 55 | Firebase SDK 依存関係追加 | DONE |
| 56 | google-services.json 設定ガイド | DONE |
| 57 | AuthRepository インターフェース定義 | DONE |
| 58 | FirebaseAuthRepositoryImpl 実装 | DONE |
| 59 | LoginScreen / RegisterScreen 実装 | DONE |
| 60 | 認証フロー ナビゲーション統合 | DONE |
| 61 | Auth ユニットテスト（47テスト + Robolectric） | DONE |
| 62 | Firestore ドキュメント設計（FIRESTORE_SCHEMA.md） | DONE |
| 63 | Remote Mapper 実装（9ファイル） | DONE |
| 64 | SyncRepository インターフェース定義（3ファイル） | DONE |
| 65 | FirestoreSyncRepositoryImpl 実装（14ファイル） | DONE |
| 66 | バックグラウンド同期 Worker（SyncWorker, SyncWorkScheduler） | DONE |
| 67 | 同期 UI（設定画面拡張：オン/オフ, 手動同期, 最終同期表示） | DONE |
| 68 | Firestore 同期テスト（FakeSyncRepository, SyncWorkerTest, SyncWorkSchedulerInterface） | DONE |
| 69 | FCM 依存関係と基本設定（CareNoteMessagingService, FirebaseModule） | DONE |
| 70 | 通知チャンネル設定（NotificationHelper, 3チャンネル） | DONE |
| 71 | 服薬リマインダー通知（MedicationReminderWorker, Scheduler） | DONE |
| 72 | 通知テスト（FakeNotificationHelper, 44テスト） | DONE |
| 73 | Crashlytics 依存関係と設定（Item 55 で設定済み、確認のみ） | DONE |
| 74 | CrashlyticsTree 実装（Timber + Crashlytics、L-1 修正） | DONE |
| 75 | ProGuard ルール更新（Firebase 用 keep ルール） | DONE |
| 76 | v2.0 E2E テスト拡張（AuthFlowTest, SyncFlowTest, TestFirebaseModule） | DONE |
| 77 | v2.0 コードレビュー（PII ログ修正、MigrationsTest 更新） | DONE |
| 78 | CLAUDE.md v2.0 更新（Firebase 統合、Worker パターン、PII ログ禁止） | DONE |
| 79 | ログへの PII 漏洩対策（sanitizeErrorMessage で email マスク） | DONE |
| 80 | メール検証状態チェック追加（isEmailVerified 警告 Snackbar） | DONE |
| 81 | パスワード強度要件強化（6 → 8 文字、NIST 準拠） | DONE |
| 82 | RemoteMapper ユニットテスト追加（7ファイル、150テスト） | DONE |
| 83 | EntitySyncer ユニットテスト追加（FakeSyncMappingDao, TestEntitySyncer, 20テスト） | DONE |
| 84 | FirestoreSyncRepositoryImpl テスト追加（MockK で Syncer モック、20テスト） | DONE |
| 85 | SyncerConfig テスト作成 TDD RED（12テスト、コンパイルエラー RED 確認） | DONE |
| 86 | SyncerConfig 実装 TDD GREEN（SyncerConfig + ConfigDrivenEntitySyncer、12テスト全パス） | DONE |
| 87 | EntitySyncer 委譲パターン REFACTOR（設計判断で変更不要、OCP 準拠） | DONE |
| 88 | 個別 Syncer 簡素化（5 Syncer → ConfigDrivenEntitySyncer 置換、約335行削減） | DONE |
| 89 | SettingsSection コンポーネントテスト TDD RED（21テスト、6セクション Composable 未実装で RED） | DONE |
| 90 | SettingsScreen セクション分割 TDD GREEN/REFACTOR（6セクション Composable 実装、462→348行削減、21テスト全パス） | DONE |
| 91 | SettingsDialogState テスト TDD RED（10テスト、sealed class 未実装でコンパイルエラー RED） | DONE |
| 92 | SettingsScreen ダイアログ分離 TDD GREEN/REFACTOR（SettingsDialogState sealed class + SettingsDialogs 抽出、348→193行削減） | DONE |
| 93 | AuthFormHandler テスト TDD RED（47テスト: AuthValidatorsTest 14, LoginFormHandlerTest 12, RegisterFormHandlerTest 12, ForgotPasswordFormHandlerTest 9、コンパイルエラー RED） | DONE |
| 94 | AuthViewModel 分割 TDD GREEN/REFACTOR（AuthValidators + 3 FormHandler 実装、316→89行削減、48新テスト + 49既存テスト全パス） | DONE |
| 95 | SettingsViewModel 汎用化テスト TDD RED（12テスト: Snackbar パターン検証、既存コード GREEN、Item 96 安全網） | DONE |
| 96 | SettingsViewModel update* メソッド整理 TDD GREEN/REFACTOR（updateSetting() 汎用メソッド導入、205→147行削減、30テスト全パス） | DONE |
| 97 | SettingsDataSource キー定数の整理（MedicationTiming ベースに変更、companion object 重複キー 6 定数削除） | DONE |
| 98 | AppConfig.Premium 未使用コード削除（Premium オブジェクト、Notification プレミアム定数、Fcm.TOPIC_PREMIUM、約25行削減） | DONE |
| 99 | TODO コメント整理（2件削除、FCM 未実装機能を既知の問題に転記） | DONE |
| 100 | UI ハードコード値の AppConfig 移行（15定数追加、共有コンポーネント6ファイル置換） | DONE |

## アーキテクチャ参照

| カテゴリ | 値 |
|----------|-----|
| Room DB | v7, SQLCipher 4.6.1 暗号化, sync_mappings テーブル追加 |
| DB キー保存 | EncryptedSharedPreferences (Android Keystore AES256_GCM) |
| 設定保存 | EncryptedSharedPreferences (`carenote_settings_prefs`) |
| バックアップ除外 | DB, DB パスフレーズ prefs, 設定 prefs |
| E2E テスト DB | TestDatabaseModule 経由のインメモリ Room（本番を置換） |
| エラー i18n | `UiText.Resource` / `UiText.ResourceWithArgs` sealed class |
| テーマカラー | `CareNoteColors.current.xxxColor`（ハードコード Color() 禁止） |
| 定数 | `AppConfig` オブジェクト（マジックナンバー禁止） |
| Enum パース | try-catch + フォールバック（NoteMapper, HealthRecordMapper, TaskMapper） |
| Snackbar i18n | `SnackbarEvent` sealed interface (WithResId / WithString) |
| テストパターン | StandardTestDispatcher + Turbine + FakeRepository (MutableStateFlow) |
| Robolectric | 4.14.1（Android SDK クラスのシャドウ用、AuthViewModelTest） |
| Firebase | BOM 33.7.0 (Auth, Firestore, Messaging, Crashlytics, Analytics) |
| Firebase プラグイン | google-services.json 存在時のみ適用（条件付き） |

## スコープ外 / 将来

- **v3.0**: Cloud Storage（写真保存）, Google Play Billing（プレミアムサブスクリプション）
- **手動**: スクリーンショット、フィーチャーグラフィック、プライバシーポリシー Web ホスティング
- **スキップ**: LegalDocumentScreen テスト（純粋な表示、ロジックなし）
