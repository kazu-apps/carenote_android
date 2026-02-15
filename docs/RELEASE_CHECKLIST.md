# リリースチェックリスト

CareNote Android のリリース前に確認すべき項目一覧。

## コード品質

- [ ] `./gradlew.bat testDebugUnitTest` — 全ユニットテスト通過
- [ ] `./gradlew.bat jacocoTestReport jacocoTestCoverageVerification` — カバレッジ 80% 以上
- [ ] `detekt --config detekt.yml --input app/src/main/java` — Detekt 違反 0 件
- [ ] `./gradlew.bat verifyRoborazziDebug` — スクリーンショット回帰テスト通過

## ビルド検証

- [ ] `./gradlew.bat assembleDebug` — Debug ビルド成功
- [ ] `./gradlew.bat assembleRelease` — Release ビルド成功
- [ ] `./gradlew.bat generateBaselineProfile` — Baseline Profile 生成
- [ ] ProGuard ルール確認 (`app/proguard-rules.pro`)
- [ ] R8/ProGuard マッピングファイル保存 (`app/build/outputs/mapping/release/mapping.txt`)
- [ ] APK サイズ確認（前回リリースとの差分チェック）
- [ ] リリース APK の実機テスト

## セキュリティ

- [ ] ハードコードされたシークレットがないこと
- [ ] PII ログが含まれていないこと（UID、メール、個人名）
- [ ] `google-services.json` が `.gitignore` に含まれていること
- [ ] SQLCipher パスフレーズ管理の動作確認
- [ ] Root 検出ダイアログの動作確認
- [ ] Root 検出時のエクスポート/アップロード制限動作確認
- [ ] ProGuard ルール — WorkManager, Paging, Security-Crypto, Biometric の keep ルール確認
- [ ] SecureFileDeleter によるキャッシュクリア動作確認
- [ ] ExceptionMasker によるエラーメッセージのサニタイズ確認
- [ ] セキュリティドキュメント（SECURITY.md, DATA_RETENTION_POLICY.md）の最新性確認
- [ ] 生体認証ロックの動作確認

## ローカライゼーション

- [ ] `res/values/strings.xml` (JP) と `res/values-en/strings.xml` (EN) の文字列一致確認
- [ ] 全画面で日本語/英語の切替動作確認
- [ ] 未翻訳文字列がないこと

## Firebase

- [ ] Crashlytics が正常に動作すること
- [ ] Firebase Auth のサインイン/サインアウト動作確認
- [ ] Cloud Firestore 同期の動作確認
- [ ] Firebase Storage の写真アップロード確認
- [ ] FCM 通知の受信確認
- [ ] Firebase Analytics イベント送信確認
- [ ] Firebase BOM バージョンと google-services.json の整合性確認

## ProGuard/R8 検証

- [ ] `./gradlew.bat assembleRelease` 成功
- [ ] Release APK の主要画面遷移確認（難読化による ClassNotFoundException がないこと）
- [ ] `proguard-rules.pro` に新規ライブラリの keep ルールが含まれていること

## 機能テスト

- [ ] 服薬管理: 追加・編集・削除・ログ記録
- [ ] カレンダー: イベント追加・編集・削除
- [ ] タスク: 追加・編集・削除・完了切替・繰り返し
- [ ] 健康記録: 記録追加・グラフ表示・CSV/PDF エクスポート
- [ ] メモ: 追加・編集・削除・タグフィルター・検索
- [ ] 設定: 全設定項目の変更・保存確認
- [ ] 通知: 服薬リマインダー・タスクリマインダー
- [ ] ウィジェット: 表示・更新・タップ動作

## ストア準備

- [ ] `versionCode` インクリメント (`build.gradle.kts`)
- [ ] `versionName` 更新 (`build.gradle.kts`)
- [ ] リリースノート作成（JP/EN）
- [ ] ストア掲載情報の更新（スクリーンショット等）
- [ ] プライバシーポリシー/利用規約の最終更新日確認
