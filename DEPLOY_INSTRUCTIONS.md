# Carenote Android デプロイ手順 (Gradle Play Publisher 版)

**Gradle Play Publisher (GPP)** を使用して Google Play Store (内部テスト版) にアプリをデプロイする手順です。
Ruby や Fastlane のインストールは不要です。

## 1. 事前準備 (APIキー)

Google Play Console からサービスアカウントの API キーを取得する必要があります（Fastlane の場合と同じです）。

1.  **Google Play Console** を開きます。
2.  **設定 (Setup)** > **API アクセス (API access)** に移動します。
3.  **新しいサービスアカウントを作成** し、**Google Cloud Console** でサービスアカウントを作成します。
4.  ロールに **サービスアカウントユーザー** を付与します。
5.  作成したアカウントの「鍵」を作成し、**JSON** 形式でダウンロードします。
6.  ダウンロードしたファイルを **`api-key.json`** という名前で、このディレクトリ (`c:\Users\hikit\projects\apps\carenote\carenote_android\api-key.json`) に保存します。
7.  **Google Play Console** に戻り、新しいサービスアカウントに「アクセス権を付与」します（**リリース > テストトラックの管理** などの権限が必要です）。

## 2. デプロイ実行

ターミナルで以下のコマンドを実行するだけで、ビルドとアップロードが自動で行われます。

### 内部テスト版としてデプロイ

```powershell
./gradlew publishBundle
```

このコマンドは以下の処理を行います：
1.  リリース用バンドル (`.aab`) のビルド
2.  Google Play Store の内部テストトラックへのアップロード

### 設定の確認 (Dry Run)

実際にアップロードせずに設定を確認したい場合は、以下を実行してください：

```powershell
./gradlew publishBundle --dry-run
```
