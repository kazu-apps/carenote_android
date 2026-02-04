# Firebase Setup Guide

CareNote Android アプリで Firebase を使用するための設定ガイド。

## 前提条件

- Google アカウント
- Firebase Console へのアクセス権限
- Android Studio または Gradle CLI

## Step 1: Firebase プロジェクト作成

1. [Firebase Console](https://console.firebase.google.com/) にアクセス
2. 「プロジェクトを追加」をクリック
3. プロジェクト名を入力（例: `CareNote` または `carenote-prod`）
4. Google Analytics の設定
   - 本番環境: 有効化を推奨（ユーザー行動分析に有用）
   - 開発環境: 任意（無効でも可）
5. 「プロジェクトを作成」をクリック

## Step 2: Android アプリ登録

1. Firebase Console でプロジェクトを開く
2. 「アプリを追加」→ Android アイコンをクリック
3. 以下の情報を入力:

| フィールド | 値 |
|-----------|-----|
| パッケージ名 | `com.carenote.app` |
| アプリのニックネーム | CareNote（任意） |
| SHA-1 証明書 | 下記参照（任意だが認証に必要） |

### SHA-1 フィンガープリント取得方法

#### デバッグ用（開発時）

```bash
# Windows
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# macOS/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### リリース用（本番）

```bash
keytool -list -v -keystore your-release-key.jks -alias your-alias
```

出力から `SHA1:` の行をコピーして Firebase Console に貼り付け。

## Step 3: google-services.json 配置

1. Firebase Console で「google-services.json をダウンロード」をクリック
2. ダウンロードしたファイルを以下に配置:

```
carenote_android/
└── app/
    └── google-services.json  ← ここに配置
```

### セキュリティ注意事項

- `google-services.json` は **機密ファイル** です
- このファイルは `.gitignore` に含まれており、Git にコミットされません
- チームメンバーには安全な方法（1Password, Google Drive 共有等）で共有してください
- **絶対に公開リポジトリにプッシュしないでください**

## Step 4: ビルド確認

```bash
# Windows
.\gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

### 期待される動作

- `google-services.json` が存在する場合: Firebase プラグインが自動適用され、Firebase SDK が有効化
- `google-services.json` が存在しない場合: ビルドは成功するが、Firebase 機能は無効

### ビルド成功時のログ例

```
> Task :app:processDebugGoogleServices
Parsing json file: .../app/google-services.json
```

## Step 5: Firebase Console で確認

1. アプリをエミュレータまたは実機で起動
2. Firebase Console → プロジェクト概要 でアプリが認識されていることを確認
3. Analytics → Realtime でイベントが記録されていることを確認（Analytics 有効時）

## 本番/開発環境の分離（補足）

異なる Firebase プロジェクトを本番/開発で使い分ける場合:

### Build Flavor を使用する方法

```
app/
├── src/
│   ├── debug/
│   │   └── google-services.json  ← 開発用
│   ├── release/
│   │   └── google-services.json  ← 本番用
│   └── main/
│       └── ...
```

`build.gradle.kts` で Flavor 設定:

```kotlin
android {
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
        }
        create("prod") {
            dimension = "environment"
        }
    }
}
```

### 推奨構成

| 環境 | Firebase プロジェクト | パッケージ名 |
|------|---------------------|-------------|
| 開発 | carenote-dev | com.carenote.app.dev |
| 本番 | carenote-prod | com.carenote.app |

## トラブルシューティング

### エラー: "File google-services.json is missing"

google-services.json が正しい場所（`app/` 直下）に配置されているか確認。

### エラー: "No matching client found for package name"

google-services.json 内のパッケージ名が `com.carenote.app` と一致しているか確認。

### Firebase SDK が動作しない

1. Sync Project with Gradle Files を実行
2. Build → Clean Project → Rebuild Project
3. Android Studio を再起動

## 関連ドキュメント

- [Firebase Android Setup (公式)](https://firebase.google.com/docs/android/setup)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Cloud Firestore](https://firebase.google.com/docs/firestore)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics)
