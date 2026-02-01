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
| DB | Room 2.6.1 (`carenote_database` v1, 2 エンティティ) |
| ナビゲーション | Navigation Compose 2.8.5 |
| 非同期 | Coroutines 1.9.0 + StateFlow |
| ログ | Timber 5.0.1 |
| テスト | JUnit 4 + MockK 1.13.9 + Turbine 1.0.0 |
| SDK | compileSdk 35, minSdk 26, targetSdk 35 |

## アーキテクチャ

### Clean Architecture（依存方向: ui → domain → data）

- **ui**: Jetpack Compose Screen + ViewModel (Hilt @Inject)。State は `StateFlow` で管理
- **domain**: Repository interfaces, domain models, `Result<T, DomainError>`
- **data**: Room DB, Repository implementations, Mapper

### DI モジュール

- `di/AppModule.kt` — Repository バインディング（2 リポジトリ）+ Gson
- `di/DatabaseModule.kt` — Room DB + 2 DAO

### ナビゲーション

`ui/navigation/Screen.kt` の sealed class でルート定義:
- **BottomNav**: Medication, Calendar, Tasks, HealthRecords, Notes
- **Secondary**: Settings, AddMedication
- `ui/navigation/CareNoteNavHost.kt` でルーティング管理

### エラーハンドリング

- `domain/common/Result.kt` — 独自の `Result<T, E>` sealed class（kotlin.Result ではない）
- `domain/common/DomainError.kt` — 6 種の sealed class (Database, NotFound, Validation, Network, Unauthorized, Unknown)
- DomainError は **Throwable ではない**。Timber に渡す際は `Timber.w("msg: $error")` と文字列化

## パッケージ構成

```
app/src/main/java/com/carenote/app/
├── config/          AppConfig（全設定値の一元管理。マジックナンバー禁止）
├── data/
│   ├── local/       Room (DB, DAO, Entity, Converter)
│   ├── mapper/      Entity ↔ Domain マッパー
│   └── repository/  Repository 実装（全て interface を実装）
├── di/              Hilt モジュール (App, Database)
├── domain/
│   ├── common/      Result<T,E>, DomainError
│   ├── model/       ドメインモデル (data class, immutable)
│   └── repository/  Repository インターフェース
└── ui/
    ├── navigation/  Screen sealed class + CareNoteNavHost
    ├── screens/     各画面 (Screen.kt)
    └── theme/       Material3 テーマ（Color, Type, Theme）
```

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

## コード規約

### ログ

**Timber 必須**。`println()`, `Log.d()`, `Log.e()` 等は禁止。

### i18n（多言語対応）

- デフォルト: 日本語 `res/values/strings.xml`
- 英語: `res/values-en/strings.xml`
- **新規文字列追加時は必ず両方のファイルを更新**

### 設定値

マジックナンバーは全て `config/AppConfig.kt` に集約。直接リテラルを使わない。

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

## 今後の追加予定

- Firebase Auth（認証）
- Cloud Firestore（家族間データ同期）
- Firebase Cloud Messaging（プッシュ通知）
- Firebase Cloud Storage（写真保存）
- Google Play Billing（プレミアムサブスクリプション）
