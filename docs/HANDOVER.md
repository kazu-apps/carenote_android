# HANDOVER.md - CareNote Android

## セッションステータス: 完了

## 現在のタスク: Item 65 FirestoreSyncRepositoryImpl 実装

ローカル (Room) とリモート (Firestore) 間のデータ同期を行う `SyncRepository` の実装クラスを作成。

### 完了した変更

1. **data/local/entity/SyncMappingEntity.kt** (新規)
   - Room ID ↔ Firestore Document ID のマッピングを管理
   - `entityType`, `localId`, `remoteId`, `lastSyncedAt`, `isDeleted` フィールド
   - `entity_type + local_id` と `entity_type + remote_id` にユニークインデックス

2. **data/local/dao/SyncMappingDao.kt** (新規)
   - `getByLocalId()`, `getByRemoteId()`, `getAllByType()`, `upsert()`, `markDeleted()` 等

3. **data/local/migration/Migrations.kt** (変更)
   - `MIGRATION_6_7`: sync_mappings テーブル作成 + インデックス追加

4. **data/local/CareNoteDatabase.kt** (変更)
   - version: 6 → 7
   - `SyncMappingEntity` 追加
   - `syncMappingDao()` 追加

5. **di/DatabaseModule.kt** (変更)
   - `provideSyncMappingDao()` 追加

6. **di/FirebaseModule.kt** (変更)
   - `provideFirebaseFirestore()` 追加

7. **data/local/SettingsDataSource.kt** (変更)
   - `getLastSyncTime()`, `updateLastSyncTime()` 追加

8. **config/AppConfig.kt** (変更)
   - `Sync` オブジェクト追加 (`TIMEOUT_MS`, `BATCH_SIZE`, `ENTITY_TYPE_COUNT`)

9. **data/repository/sync/EntitySyncer.kt** (新規)
   - エンティティタイプ別の同期ロジックを抽象化した基底クラス
   - 双方向同期、ID マッピング管理、Last-Write-Wins 競合解決

10. **data/repository/sync/ 6 Syncer 実装** (新規)
    - `MedicationSyncer.kt`
    - `MedicationLogSyncer.kt` (サブコレクション対応)
    - `NoteSyncer.kt`
    - `HealthRecordSyncer.kt`
    - `CalendarEventSyncer.kt`
    - `TaskSyncer.kt`

11. **data/repository/FirestoreSyncRepositoryImpl.kt** (新規)
    - `SyncRepository` の実装
    - 全エンティティ同期、個別同期、進捗状態管理
    - `MutableStateFlow<SyncState>` で UI 通知

12. **di/SyncModule.kt** (新規)
    - `provideSyncRepository()` で DI 設定

### ビルド結果

- `./gradlew.bat assembleDebug` - 成功

## 次のアクション

1. **v2.0 開発継続**: `/task-exec` で Item 66 から順次実行
2. **次の PENDING 項目**:
   - Item 66: バックグラウンド同期 Worker（Item 65 完了）
   - Item 69: FCM 基本設定（Item 55 依存解消済み）
   - Item 73: Crashlytics 設定（Item 55 依存解消済み）

## 既知の問題

### 未解決（要対応）

**その他:**
- 問い合わせメールがプレースホルダー (carenote.app.support@gmail.com) — リリース前に確認
- リリース APK の実機テスト未実施

### 記録のみ（対応保留）

| 重要度 | 出典 | 内容 |
|--------|------|------|
| MEDIUM | M-4 | SettingsDataSource の動的キー生成（呼び出し側は定数使用） |
| MEDIUM | M-5 | Room スキーマ JSON がコミット済み（プライベートリポジトリでは許容） |
| MEDIUM | Item 30 | ValidationUtils.kt が未使用のデッドコード（本番インポートなし） |
| MEDIUM | Item 32 | WorkManager/HiltWork の事前実装（v2.0 通知用） |
| MEDIUM | Item 32 | JaCoCo `**/util/*` 除外が広範囲（テストは存在） |
| MEDIUM | Item 31 | テスト品質: Mapper ラウンドトリップ不完全、Repository Turbine 未使用、ViewModel Loading→Success テスト欠落 |
| LOW | L-1 | リリースビルドに Timber Tree なし（Crashlytics と共に追加） |
| LOW | L-4 | 全 DAO が OnConflictStrategy.REPLACE 使用（マルチデバイス同期リスク） |
| LOW | Item 32 | POST_NOTIFICATIONS の事前宣言（無害） |
| LOW | Item 32 | 2024年後半の依存関係（CVE なし、一貫性あり） |
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

### Item 66: バックグラウンド同期 Worker - PENDING
WorkManager で定期的にクラウド同期を実行。
- 種別: 実装
- 対象: `data/worker/SyncWorker.kt`, `di/WorkerModule.kt`
- ファイル数: 2
- 依存: Item 65 完了後

---

### Item 67: 同期 UI（設定画面拡張） - PENDING
設定画面に同期オン/オフ、手動同期ボタン、最終同期日時表示を追加。
- 種別: 実装
- 対象: `ui/screens/settings/SettingsScreen.kt`, `SettingsViewModel.kt`
- ファイル数: 2
- 依存: Item 66 完了後

---

### Item 68: Firestore 同期テスト - PENDING
SyncRepository の Fake 実装と統合テスト。
- 種別: テスト
- 対象: `test/.../fakes/FakeSyncRepository.kt`, `SyncWorkerTest.kt`
- ファイル数: 3
- 依存: Item 67 完了後

---

### フェーズ 4: Firebase Cloud Messaging（プッシュ通知）

---

### Item 69: FCM 依存関係と基本設定 - PENDING
firebase-messaging を追加し、FirebaseMessagingService を実装。
- 種別: 実装
- 対象: `data/service/CareNoteMessagingService.kt`, `AndroidManifest.xml`
- ファイル数: 2
- 依存: Item 55 完了後

---

### Item 70: 通知チャンネル設定 - PENDING
Android 8+ 向け NotificationChannel 作成と NotificationHelper ユーティリティ。
- 種別: 実装
- 対象: `ui/util/NotificationHelper.kt`, `CareNoteApplication.kt`
- ファイル数: 2
- 依存: Item 69 完了後

---

### Item 71: 服薬リマインダー通知 - PENDING
設定された服薬時間に WorkManager からローカル通知を発行。
- 種別: 実装
- 対象: `data/worker/MedicationReminderWorker.kt`
- ファイル数: 1
- 依存: Item 70 完了後

---

### Item 72: 通知テスト - PENDING
NotificationHelper と MedicationReminderWorker のユニットテスト。
- 種別: テスト
- 対象: `test/.../NotificationHelperTest.kt`, `MedicationReminderWorkerTest.kt`
- ファイル数: 2
- 依存: Item 71 完了後

---

### フェーズ 5: Firebase Crashlytics + リリース品質

---

### Item 73: Crashlytics 依存関係と設定 - PENDING
firebase-crashlytics を追加し、build.gradle に crashlytics プラグイン設定。
- 種別: 実装
- 対象: `build.gradle.kts` (project/app), `libs.versions.toml`
- ファイル数: 3
- 依存: Item 55 完了後

---

### Item 74: CrashlyticsTree 実装 (Timber + Crashlytics) - PENDING
リリースビルドで Timber ログを Crashlytics に送信する Tree 実装。L-1 対応。
- 種別: 実装
- 対象: `util/CrashlyticsTree.kt`, `CareNoteApplication.kt`
- ファイル数: 2
- 依存: Item 73 完了後
- セキュリティ ID: L-1

---

### Item 75: ProGuard ルール更新 - PENDING
Firebase SDK 用の ProGuard keep ルールを追加。
- 種別: 実装
- 対象: `app/proguard-rules.pro`
- ファイル数: 1
- 依存: Item 74 完了後

---

### Item 76: v2.0 E2E テスト拡張 - PENDING
認証フロー、同期フローの E2E テストを追加。
- 種別: テスト
- 対象: `androidTest/.../e2e/AuthFlowTest.kt`, `SyncFlowTest.kt`
- ファイル数: 2
- 依存: Item 68, 72, 75 完了後

---

### Item 77: v2.0 コードレビュー - PENDING
Firebase 統合全体のセキュリティ・品質レビュー。
- 種別: コードレビュー
- 対象: 全 Firebase 関連ファイル
- 依存: Item 76 完了後

---

### Item 78: CLAUDE.md v2.0 更新 - PENDING
Firebase 関連の技術スタック、パターン、注意事項を CLAUDE.md に追記。
- 種別: ドキュメント
- 対象: `CLAUDE.md`
- ファイル数: 1
- 依存: Item 77 完了後

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
