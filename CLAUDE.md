# CareNote - 家族でつながる介護記録 Android アプリ

服薬管理、健康記録、カレンダー、タスク、メモ・申し送りを搭載した
家族介護者向け Android ネイティブアプリ。

## クイックリファレンス

```bash
# ビルド
./gradlew.bat assembleDebug

# ユニットテスト
./gradlew.bat testDebugUnitTest

# 特定テストクラス実行
./gradlew.bat testDebugUnitTest --tests "com.carenote.app.domain.common.ResultTest"

# UI テスト（要エミュレータ）
./gradlew.bat connectedDebugAndroidTest

# カバレッジ（80% LINE 閾値）
./gradlew.bat jacocoTestReport jacocoTestCoverageVerification

# 静的解析（CLI ツール。Gradle プラグインではない）
detekt --config detekt.yml --input app/src/main/java
```

## 技術スタック

| カテゴリ | 技術 |
|---------|------|
| 言語 | Kotlin 2.0.21 / JVM 17 |
| UI | Jetpack Compose + Material 3 (BOM 2024.12.01) |
| DI | Hilt 2.53.1 (KSP) |
| DB | Room 2.6.1 + SQLCipher 4.6.1 (`carenote_database` v7) |
| ナビゲーション | Navigation Compose 2.8.5 |
| 非同期 | Coroutines 1.9.0 + StateFlow |
| ログ | Timber 5.0.1 |
| Firebase | BOM 33.7.0 (Auth, Firestore, Messaging, Crashlytics) |
| WorkManager | 2.10.0 (HiltWorker) |
| テスト | JUnit 4 + MockK 1.13.9 + Turbine 1.0.0 + Robolectric 4.14.1 |
| SDK | compileSdk 35, minSdk 26, targetSdk 35 |

## アーキテクチャ

### Clean Architecture（依存方向: ui → domain → data）

- **ui**: Jetpack Compose Screen + ViewModel (Hilt @Inject)。State は `StateFlow` で管理
- **domain**: Repository interfaces, domain models, `Result<T, DomainError>`
- **data**: Room DB, Firestore, Repository implementations, Mapper

### DI モジュール

| モジュール | 責務 |
|-----------|------|
| `di/AppModule.kt` | Repository バインディング + Gson |
| `di/DatabaseModule.kt` | Room DB + DAO (8 テーブル) |
| `di/FirebaseModule.kt` | FirebaseAuth, Firestore, Messaging + AuthRepository |
| `di/SyncModule.kt` | SyncRepository + EntitySyncer 群 |
| `di/WorkerModule.kt` | WorkManager + SyncWorkScheduler |

### ナビゲーション

`ui/navigation/Screen.kt` の sealed class でルート定義:
- **Auth**: Login, Register, ForgotPassword
- **BottomNav**: Medication, Calendar, Tasks, HealthRecords, Notes
- **Secondary**: Settings, AddMedication
- `ui/navigation/CareNoteNavHost.kt` でルーティング管理

### エラーハンドリング

- `domain/common/Result.kt` — 独自の `Result<T, E>` sealed class（kotlin.Result ではない）
- `domain/common/DomainError.kt` — 6 種の sealed class (Database, NotFound, Validation, Network, Unauthorized, Unknown)
- DomainError は **Throwable ではない**。Timber に渡す際は `Timber.w("msg: $error")` と文字列化

### 同期パターン（Firestore）

- `domain/common/SyncResult.kt` — 同期結果 (Success, PartialSuccess, Failure)
- `domain/common/SyncState.kt` — 同期状態 (Idle, Syncing, Success, Error)
- **競合解決**: Last-Write-Wins (LWW) — `updatedAt` 比較で新しい方を採用

## パッケージ構成

```
app/src/main/java/com/carenote/app/
├── config/          AppConfig（全設定値の一元管理。マジックナンバー禁止）
├── data/
│   ├── local/       Room (DB, DAO, Entity, Converter, Migration)
│   ├── mapper/      Entity ↔ Domain マッパー
│   │   └── remote/  Firestore ↔ Domain マッパー (RemoteMapper)
│   ├── remote/
│   │   └── model/   SyncMetadata（同期メタデータ）
│   ├── repository/  Repository 実装
│   │   └── sync/    EntitySyncer + 各エンティティ Syncer
│   ├── service/     CareNoteMessagingService (FCM)
│   └── worker/      SyncWorker, MedicationReminderWorker
├── di/              Hilt モジュール (App, Database, Firebase, Sync, Worker)
├── domain/
│   ├── common/      Result<T,E>, DomainError, SyncResult, SyncState
│   ├── model/       ドメインモデル (data class, immutable)
│   └── repository/  Repository インターフェース
└── ui/
    ├── navigation/  Screen sealed class + CareNoteNavHost
    ├── screens/     各画面 (Screen.kt)
    │   └── auth/    LoginScreen, RegisterScreen, ForgotPasswordScreen
    ├── theme/       Material3 テーマ（Color, Type, Theme）
    └── util/        NotificationHelper, CrashlyticsTree
```

## Firebase 統合

### Firebase Auth（認証）

- `AuthRepository` — 認証インターフェース (signIn, signUp, signOut, etc.)
- `FirebaseAuthRepositoryImpl` — Firebase Auth 実装
- `currentUser: Flow<User?>` で認証状態を監視
- FirebaseAuthException → DomainError マッピング

### Cloud Firestore（データ同期）

- **構造**: `careRecipients/{id}/medications/{id}` のサブコレクション構造
- **同期**: Room ↔ Firestore 双方向同期
- **ID マッピング**: `sync_mappings` テーブルで Room ID ↔ Firestore ID を管理

### EntitySyncer パターン

```kotlin
// 基底クラス: data/repository/sync/EntitySyncer.kt
abstract class EntitySyncer<Entity, Domain> {
    abstract val entityType: String
    abstract fun collectionPath(careRecipientId: String): String

    // テンプレートメソッド
    suspend fun sync(careRecipientId: String, lastSyncTime: LocalDateTime?): SyncResult {
        val pushResult = pushLocalChanges(...)
        val pullResult = pullRemoteChanges(...)
        return mergeResults(pushResult, pullResult)
    }
}
```

各 Syncer: `MedicationSyncer`, `MedicationLogSyncer`, `NoteSyncer`, `HealthRecordSyncer`, `CalendarEventSyncer`, `TaskSyncer`

### RemoteMapper パターン

```kotlin
// インターフェース: data/mapper/remote/RemoteMapper.kt
interface RemoteMapper<Domain> {
    fun toDomain(data: Map<String, Any?>): Domain
    fun toRemote(domain: Domain, syncMetadata: SyncMetadata?): Map<String, Any?>
    fun extractSyncMetadata(data: Map<String, Any?>): SyncMetadata
}
```

### FCM（プッシュ通知）

- `CareNoteMessagingService` — FirebaseMessagingService 実装
- `NotificationHelper` — 通知チャンネル管理 + 通知表示

### Crashlytics

- `CrashlyticsTree` — Timber Tree 実装
- WARN 以上のログを Crashlytics に送信
- 例外は `recordException()` で自動記録

## Worker パターン

### SyncWorker（定期同期）

```kotlin
@HiltWorker
class SyncWorker : CoroutineWorker {
    // 1. 認証確認
    // 2. careRecipientId 取得
    // 3. syncRepository.syncAll() 実行
    // 4. 結果に応じて Result.success/retry/failure
}
```

- 定期実行: 15分間隔（WorkManager 最小値）
- 制約: NetworkType.CONNECTED
- リトライ: NetworkError → 可能, UnauthorizedError → 不可

### MedicationReminderWorker（服薬リマインダー）

- 指定時刻に通知を発行
- おやすみ時間（quietHours）チェック
- ユーザー設定で通知オン/オフ

## テーマ

- **ライトテーマ**（温かみのあるクリーム背景 #FFF8F0）
- **プライマリカラー**: グリーン系（信頼感 #2E7D32）
- **フォントサイズ**: bodyLarge 18sp（高齢者向け大きめ）
- **最小タッチターゲット**: 48dp

## テスト

### 構成

| 種類 | フレームワーク | 場所 |
|------|-------------|------|
| Unit | JUnit 4 + MockK + Turbine + Coroutines Test | `app/src/test/` |
| UI/E2E | Hilt + Espresso + UIAutomator + Compose UI Test | `app/src/androidTest/` |
| Runner | `com.carenote.app.HiltTestRunner` | build.gradle.kts |
| カバレッジ | JaCoCo 0.8.12（LINE 80% 閾値） | `jacocoTestCoverageVerification` |

### Fake Repository パターン

`test/.../fakes/` に配置。`MutableStateFlow<List<T>>` でインメモリ状態管理。

Firebase 関連:
- `FakeAuthRepository` — 認証状態のテスト制御
- `FakeSyncRepository` — 同期状態のテスト制御
- `FakeSyncWorkScheduler` — WorkManager 依存排除

### E2E テスト

`androidTest/.../di/TestFirebaseModule.kt` で本番モジュールを Fake に置換。

## コード規約

### ログ

**Timber 必須**。`println()`, `Log.d()`, `Log.e()` 等は禁止。

**PII ログ禁止**: UID, email, 個人名等をログに含めない。
```kotlin
// NG
Timber.d("User signed in: ${user.uid}")

// OK
Timber.d("User signed in successfully")
```

### i18n（多言語対応）

- デフォルト: 日本語 `res/values/strings.xml`
- 英語: `res/values-en/strings.xml`
- **新規文字列追加時は必ず両方のファイルを更新**

### 設定値

マジックナンバーは全て `config/AppConfig.kt` に集約。直接リテラルを使わない。

主要カテゴリ:
- `AppConfig.Auth` — 認証関連（パスワード長、メール長）
- `AppConfig.Sync` — 同期関連（タイムアウト、リトライ回数）
- `AppConfig.Notification` — 通知チャンネル ID

### Detekt ルール（maxIssues=0）

| ルール | 閾値 |
|--------|------|
| LongMethod | 50 行 |
| LargeClass | 800 行 |
| MaxLineLength | 120 文字 |
| NestedBlockDepth | 4 |
| CyclomaticComplexMethod | 15 |

## よくある落とし穴

1. **Detekt は CLI ツール** — Gradle プラグインとして追加しないこと（MockK インストルメンテーションと競合）
2. **Room Entity 変更時** — Migration ファイル作成 + `DatabaseModule.kt` への登録が必須
3. **strings.xml は JP/EN ペア更新** — 片方だけ更新すると実行時に英語/日本語が混在
4. **DomainError は Throwable ではない** — `Timber.w(error, msg)` は使えない。`Timber.w("msg: $error")` と書く
5. **Result は独自実装** — `domain/common/Result.kt` の `Result<T, E>`。kotlin.Result ではない
6. **Windows 環境** — `./gradlew.bat` を使用。パス区切りは `\`
7. **ProGuard (release)** — 新ライブラリ追加時は `app/proguard-rules.pro` の keep ルール確認
8. **Zero Detekt tolerance** — maxIssues=0, all issues must be fixed
9. **google-services.json** — Firebase 設定ファイル。`.gitignore` 済み。`docs/FIREBASE_SETUP.md` 参照
10. **PII ログ禁止** — UID, email, 個人名をログに含めない（L-2 セキュリティ要件）
11. **WorkManager 最小間隔** — 定期実行は最短 15分。それ未満は設定しても 15分になる
12. **Firebase 例外処理** — FirebaseAuthException は DomainError にマッピングして返す

## 今後の追加予定

- Firebase Cloud Storage（写真保存）
- Google Play Billing（プレミアムサブスクリプション）
