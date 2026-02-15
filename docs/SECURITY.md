# Security Overview

CareNote は個人の健康情報（要配慮個人情報）を扱う介護記録アプリであり、
個人情報保護法（APPI）に準拠したセキュリティ対策を実装しています。

## Data Encryption

### ローカルデータベース
- **SQLCipher 4.6.1** を使用して Room データベースを暗号化
- 暗号化鍵は `DatabasePassphraseManager` が `EncryptedSharedPreferences` で管理
- PBKDF2 (100,000 イテレーション、256bit 鍵長、32byte ソルト) で鍵を導出

### 設定データ
- `EncryptedSharedPreferences` (AndroidX Security-Crypto) で暗号化保存
- AES256-SIV (鍵暗号化) + AES256-GCM (値暗号化)

### 通信
- Firebase SDK が TLS 1.2+ を強制
- Cloud Firestore / Firebase Storage への通信はすべて HTTPS

## Access Control

### Firebase Auth（認証）
- メール/パスワード認証
- メールアドレス検証（未検証時は警告表示）
- パスワード要件: 8文字以上

### 生体認証ロック
- `BiometricHelper` が AndroidX Biometric API を使用
- アプリ起動時・バックグラウンド復帰時に認証を要求（設定で有効化）
- セッションタイムアウト: ユーザー設定可能（1〜60分、デフォルト5分）

### セッション管理
- `MainActivity` のライフサイクルオブザーバーでバックグラウンド時間を計測
- タイムアウト超過時は生体認証を再要求

## Root Detection and Restrictions

### 検出方法
- `RootDetector` が以下をチェック:
  - Build タグに `test-keys` が含まれるか
  - `su` バイナリが既知のパス（`/system/xbin/su`, `/system/bin/su` 等）に存在するか

### Root 検出時の制限
1. **エクスポート機能の無効化**: CSV/PDF エクスポートをブロック（Settings, HealthRecords, Medication の各 ViewModel）
2. **写真アップロードの無効化**: `FirebaseStorageRepositoryImpl` で `SecurityError` を返却
3. **セッションタイムアウトの短縮**: 通常設定に関わらず 60 秒に固定
4. **警告ダイアログ**: アプリ起動時に制限内容を告知

### DI 統合
- `RootDetectionChecker` インターフェースを `AppModule` で `@Singleton` 提供
- テストでは `FakeRootDetector` に差し替え可能

## Secure File Handling

### FileProvider
- 一時ファイル共有は `FileProvider` 経由（`file://` URI は使用しない）
- エクスポートファイルはキャッシュディレクトリに生成し、共有後にクリーンアップ

### SecureFileDeleter
- エクスポート/写真キャッシュの安全な削除
- キャッシュ TTL: 写真 7日、エクスポート 1時間

### ExceptionMasker
- 外部に返すエラーメッセージから内部パス・スタックトレースを除去
- `FirebaseStorageRepositoryImpl` 等で使用

## ProGuard / R8

- リリースビルドで `minifyEnabled = true` + `shrinkResources = true`
- `proguard-rules.pro` に以下の keep ルールを設定:
  - Room Entity, TypeConverter, Mapper
  - Hilt DI クラス
  - Navigation Screen sealed class
  - SQLCipher ネイティブライブラリ
  - Firebase (Crashlytics, Analytics, Messaging, Storage)
  - WorkManager + HiltWorker
  - Paging 3, Security-Crypto, Biometric
  - Coil 3 (画像ローダー)
- Crashlytics 用に `-keepattributes SourceFile,LineNumberTable` で行番号を保持

## Logging and PII Protection

### Timber ログ
- `Timber` のみ使用（`println()`, `Log.d()` 等は禁止）
- `CrashlyticsTree` が WARN 以上のログを Firebase Crashlytics に送信

### PII ログ禁止ルール
以下の情報をログに含めてはならない:
- UID, メールアドレス, 個人名
- 医療情報, 健康記録の具体値
- 認証トークン, パスワード

```kotlin
// NG
Timber.d("User signed in: ${user.uid}")

// OK
Timber.d("User signed in successfully")
```

## OWASP Mobile Top 10 Mapping

| # | リスク | 対策 |
|---|--------|------|
| M1 | Improper Credential Usage | Firebase Auth + EncryptedSharedPreferences |
| M2 | Inadequate Supply Chain Security | Gradle 依存ロック + BOM バージョン管理 |
| M3 | Insecure Authentication/Authorization | 生体認証 + セッションタイムアウト + Root 検出 |
| M4 | Insufficient Input/Output Validation | Domain Validator + AppConfig 範囲チェック |
| M5 | Insecure Communication | TLS 1.2+ (Firebase SDK 強制) |
| M6 | Inadequate Privacy Controls | PII ログ禁止 + ExceptionMasker |
| M7 | Insufficient Binary Protections | ProGuard/R8 難読化 + Root 検出 |
| M8 | Security Misconfiguration | Detekt (maxIssues=0) + セキュリティレビュー |
| M9 | Insecure Data Storage | SQLCipher + EncryptedSharedPreferences |
| M10 | Insufficient Cryptography | PBKDF2 (100K iter) + AES-256 |
