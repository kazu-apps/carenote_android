# Investigation: セキュリティレビュー発見事項の詳細調査

日付: 2026-02-18
カテゴリ: 調査/分析
対象: セキュリティレビューで特定された CRITICAL/HIGH/MEDIUM 問題の詳細コード調査

## 概要

セキュリティレビューで発見された 1 CRITICAL + 7 HIGH + 9 MEDIUM + 7 LOW の問題について、
対象コードの正確な位置、修正パターン、影響範囲、修正リスクを詳細に調査した。
修正の優先度と実行計画を以下にまとめる。

## 調査結果

### 事実収集 (researcher)

#### C-1: DatabaseRecoveryHelper サイレント DB 削除
- **ファイル**: `data/local/DatabaseRecoveryHelper.kt:10-18`
- `recoverIfNeeded()` はパスフレーズ不一致時に `deleteDatabaseFiles()` で DB を無条件削除
- `canOpenDatabase()` (L20-38) は `catch (e: Exception)` で全例外をキャッチ → パスフレーズ不一致以外（DB 破損、ディスクエラー等）でも false を返す
- ユーザー確認なし、バックアップなし、Analytics 通知なし
- **呼び出し元**: `di/DatabaseModule.kt:48` の `provideDatabase()`
- **連鎖リスク**: `DatabasePassphraseManager.kt:80-87` の `getOrRecreatePrefs()` で EncryptedSharedPreferences 破損 → 新パスフレーズ生成 → DB 開けない → DB 削除

#### H-1: AcceptInvitationViewModel メール未検証
- **ファイル**: `ui/screens/member/AcceptInvitationViewModel.kt:101-145`
- `accept()` は `authRepository.getCurrentUser() == null` のみチェック（ログイン確認のみ）
- `invitation.inviteeEmail` と `currentUser.email` の一致確認が**欠落**
- L117-122 で `Member` 作成時に `uid = currentUser.uid` をそのまま使用、メール照合なし
- 任意のログインユーザーがトークンを知っていれば他人宛招待を受諾可能

#### H-2: Biometric 認証バイパス
- **ファイル**: `ui/MainActivity.kt:298-325`
- L299-301: `canAuthenticate() == false` → `isAuthenticated = true` で無条件スキップ
- L323: `onError = { _ -> }` — 認証エラー時に何もしない → アプリが空白状態で停止
- `BiometricHelper` は DI 未使用（`new BiometricHelper()` で直接生成）→ テスト困難
- L128-136: `!isLoggedIn || !settings.biometricEnabled || authenticated` が true ならナビゲーション表示

#### H-3: android:allowBackup
- **ファイル**: `AndroidManifest.xml:10` — `android:allowBackup="true"`
- `backup_rules.xml`: sharedpref 全体 include、`carenote_db_prefs.xml` と `carenote_settings_prefs.xml` のみ exclude
- `data_extraction_rules.xml`: cloud-backup/device-transfer で同じルール
- **database ドメイン未除外** — 暗号化 DB ファイルがバックアップ対象に含まれる
- file/external/cache ドメインも未規定

#### H-4: raw e.message の UI 露出 (10+ 箇所)
- `HomeViewModel.kt:95` — `error = e.message` (HomeUiState.error: String?)
- `MedicationViewModel.kt:86` — `DomainError.DatabaseError(e.message ?: "Unknown error")`
- `MedicationViewModel.kt:197,225` — `ExportState.Error(e.message ?: "Unknown error")`
- `MedicationDetailViewModel.kt:61`, `TimelineViewModel.kt:79`, `HealthRecordsViewModel.kt:91`, `CalendarViewModel.kt:65`, `SearchViewModel.kt:57` — 同パターン
- `HealthRecordsViewModel.kt:130,158`, `SettingsViewModel.kt:367,395,423,451` — ExportState.Error
- HomeViewModel は特に問題: `String?` 型で生メッセージを直接保持

#### H-5: Timber ログの ExceptionMasker 未使用 (60+ 箇所)
- `ExceptionMasker` (`data/util/ExceptionMasker.kt`): Firebase 例外のみ対応
- 使用箇所: `EntitySyncer.kt` (7), `MedicationLogSyncer.kt` (3), `FirebaseStorageRepositoryImpl.kt` (2) — 計 12 箇所
- **未使用**: `SyncWorker.kt` (5箇所), `DatabasePassphraseManager.kt`, `DatabaseEncryptionMigrator.kt`, `TimelineRepositoryImpl.kt` (5箇所), 全 ViewModel — 合計 60+ 箇所
- ViewModel 層の `$error` は `DomainError` 型（Throwable ではない）が多い → ExceptionMasker 対象外
- Repository/Worker 層の `$e` は `Exception` で PII リスクあり

#### H-6: 招待トークンセキュリティ
- **トークン生成**: `SendInvitationViewModel.kt:195-200` — `SecureRandom` 使用（128-bit entropy、安全）
- **トークン保存**: Room DB に平文保存（ハッシュ化なし）
- **トークン検証**: `AcceptInvitationViewModel.kt:58-98` — DB 検索 + ステータス/期限チェック
- 問題: レート制限なし、タイミング攻撃保護なし、期限切れの再チェックなし（`accept()` 時）
- deep link URL にトークン直接埋め込み → ブラウザ履歴/リファラー漏洩リスク

#### H-7: 入力バリデーション不足
- `CareRecipientViewModel.kt:115-153`: `validateRequired(name)` のみ、6フィールドの MAX_LENGTH 未チェック
  - 未使用定数: NAME(100), NICKNAME(50), CARE_LEVEL(50), MEDICAL_HISTORY(1000), ALLERGIES(500), MEMO(500)
- `AddEditEmergencyContactViewModel.kt:129-157`: memo の MAX_LENGTH 未チェック、電話番号フォーマット未検証
- `FormValidator.kt`: `validateRequired`, `validateMaxLength`, `combineValidations` のみ — 専用バリデータなし

#### MEDIUM 問題の概要
- **M-1**: `carenote://` カスタムスキーム — BROWSABLE なし、ホスト指定なし、任意アプリが登録可能
- **M-2**: `network_security_config.xml` — cleartext 禁止済み（良好）、証明書ピニングなし
- **M-3**: `SearchViewModel.kt:36-63` — クエリ長制限/サニタイズなし
- **M-4**: `InputValidator.kt:20` — EMAIL_REGEX が `..` や先頭ドットを許容
- **M-5**: `ImageCompressor.kt:24-49` — MIME type/サイズ検証なし、OOM リスク
- **M-6**: `DomainError.UnknownError` が raw throwable.message をラップ
- **M-7**: `api-key.json` 参照に存在チェックなし (`build.gradle.kts:108`)
- **M-8**: `DatabaseEncryptionMigrator.kt:58-69` — SQL 文字列にパスフレーズ hex 値を補間
- **M-9**: RootDetector が su バイナリ + Build.TAGS のみ — Magisk/KernelSU で容易にバイパス

### コード分析 (code-analyst)

#### FormValidator パターン
- 3 メソッドのみ: `validateRequired`, `validateMaxLength`, `combineValidations`
- 使用 ViewModel: AddEditCalendarEvent, AddEditNote, AddEditMedication, AddEditEmergencyContact, CareRecipient
- CareRecipientViewModel は `validateRequired` のみ使用（MAX_LENGTH 未使用）
- 電話番号/URL/特殊文字フィルタ等の専用バリデータなし

#### ExceptionMasker 使用範囲
- sync 層 + FirebaseStorageRepository の 3 ファイルのみ
- Repository 層の `Result.catchingSuspend` パターンでは ExceptionMasker 未使用
- DomainError.message に PII が含まれうる

#### エラーハンドリングフロー
- **UiState パターン** (DomainError → ErrorDisplay): Calendar, HealthRecords, Medication, MedicationDetail, Search, Timeline
- **独自パターン**: AcceptInvitation (UiText ベース), Settings/Auth (snackbar)
- **Snackbar**: 41 ファイルが SnackbarController を使用
- パターン不統一: リスト画面=UiState+ErrorDisplay, AddEdit 画面=FormState+snackbar, AcceptInvitation=独自

#### 招待システム全体フロー
- 送信: email 検証 → 自己招待チェック → 重複チェック → SecureRandom トークン → DB 保存 → リンク生成
- 受諾: deep link → トークン検索 → ステータス/期限チェック → Member 作成
- 欠陥: accept() でメール未検証、期限の再チェックなし、inviterUid 空文字許容

#### DB パスフレーズ管理フロー
```
DatabaseModule.provideDatabase()
  → PassphraseManager.getOrCreatePassphrase()
    → getOrRecreatePrefs() -- EncryptedSharedPreferences 取得/再作成
    → getMasterPassphrase() -- 32 bytes, SecureRandom
    → deriveKey() -- PBKDF2WithHmacSHA256
  → EncryptionMigrator.migrateIfNeeded()
  → RecoveryHelper.recoverIfNeeded()
  → SupportOpenHelperFactory(passphrase) → Room.databaseBuilder
```

### リスク評価 (risk-assessor)

#### 修正リスクサマリー

| # | 修正項目 | リスク | 主要リスク | 緩和策 |
|---|---------|--------|-----------|--------|
| 1 | DatabaseRecoveryHelper バックアップ追加 | HIGH | ストレージ容量不足、起動不能リスク | StatFs チェック、cache dir 使用、try-catch 必須 |
| 2 | AcceptInvitation メール検証 | MEDIUM | メール変更ユーザーが受諾不可 | lowercase 比較、ガイダンス表示 |
| 3 | Biometric 認証修正 | MEDIUM | 認証手段なしデバイスでアプリ使用不可 | 現状フォールバック維持、NONE_ENROLLED のみガイダンス |
| 4 | allowBackup=false | HIGH | 既存バックアップデータ消失、デバイス移行不可 | 段階的変更、Firestore 同期をプロモーション |
| 5 | e.message → DomainError | LOW | HomeScreen が error 未使用のため影響小 | UiText 化推奨だが低優先 |
| 6 | ExceptionMasker 全面適用 | LOW | ログ可読性低下、デバッグ困難 | Timber のみマスク、Crashlytics は full exception 維持 |
| 7 | 入力バリデーション追加 | MEDIUM | 既存データ編集時にエラー表示 | 新規入力のみ適用、既存データはソフト警告 |
| 8 | Deep link スキーム変更 | HIGH | 既存通知/共有リンク無効化 | 短期は変更なし、中期で App Links 移行 |

#### 特に注意すべきエッジケース
1. **DatabaseRecoveryHelper での起動不能**: `provideDatabase()` 内で例外 → アプリ完全起動不能。try-catch 必須
2. **既存データと新バリデーションの衝突**: 過去に保存した長い文字列が編集時にエラー → ユーザー混乱
3. **通知トレイの古い deep link**: スキーム変更 → 端末に残る通知が機能しなくなる
4. **allowBackup=false 後のデータ復元不可**: Firestore 同期なしユーザーはデータ喪失

#### 影響テストファイル
| 修正項目 | 影響テスト |
|---------|----------|
| DatabaseRecoveryHelper | `DatabaseRecoveryHelperTest.kt` — バックアップテスト追加 |
| AcceptInvitation | `AcceptInvitationViewModelTest.kt` + `AcceptInvitationFlowTest.kt` |
| Biometric | `BiometricHelperTest.kt` — 既存4ケースで十分 |
| allowBackup | テスト不要 |
| e.message | `HomeViewModelTest.kt` — error state アサーション変更 |
| ExceptionMasker | `ExceptionMaskerTest.kt` — Room 例外テスト追加 |
| バリデーション | 各 ViewModel テスト — バリデーションケース追加 |
| Deep link | `NotificationHelperTest.kt`, `AcceptInvitationFlowTest.kt` |

## 関連ファイル

| ファイル | 関連度 | メモ |
|---------|--------|------|
| `data/local/DatabaseRecoveryHelper.kt` | 最高 | C-1: サイレント DB 削除 |
| `ui/screens/member/AcceptInvitationViewModel.kt` | 最高 | H-1: メール未検証 + H-6: トークン検証 |
| `ui/MainActivity.kt` | 高 | H-2: Biometric バイパス |
| `AndroidManifest.xml` | 高 | H-3: allowBackup + M-1: deep link |
| `res/xml/backup_rules.xml` | 高 | H-3: database ドメイン未除外 |
| `res/xml/data_extraction_rules.xml` | 高 | H-3: database ドメイン未除外 |
| `ui/screens/home/HomeViewModel.kt` | 高 | H-4: raw e.message |
| `ui/viewmodel/ExportState.kt` | 高 | H-4: ExportState.Error |
| `data/util/ExceptionMasker.kt` | 高 | H-5: sync 層のみ使用 |
| `ui/screens/member/SendInvitationViewModel.kt` | 高 | H-6: トークン生成 |
| `ui/screens/carerecipient/CareRecipientViewModel.kt` | 高 | H-7: MAX_LENGTH 未チェック |
| `ui/screens/emergencycontact/AddEditEmergencyContactViewModel.kt` | 高 | H-7: memo/phone 未検証 |
| `ui/util/FormValidator.kt` | 中 | 専用バリデータ追加候補 |
| `ui/util/BiometricHelper.kt` | 中 | H-2: DI 未使用 |
| `data/local/DatabasePassphraseManager.kt` | 中 | C-1 連鎖元 |
| `di/DatabaseModule.kt` | 中 | C-1 呼び出し元 |
| `config/AppConfig.kt` | 中 | MAX_LENGTH 定数定義元 |
| `data/worker/SyncWorker.kt` | 中 | H-5: ExceptionMasker 未使用 |
| `ui/util/RootDetector.kt` | 低 | M-9: 検出方法が簡易 |
| `data/local/ImageCompressor.kt` | 低 | M-5: MIME/サイズ検証なし |
| `domain/validator/InputValidator.kt` | 低 | M-4: EMAIL_REGEX |
| `res/xml/network_security_config.xml` | 低 | 証明書ピニングなし |

## 推奨実行計画

### Phase 1: CRITICAL + 即効性の高い HIGH（安全な変更）
1. **allowBackup=false** + backup_rules 修正 — 変更1行 + XML 修正、テスト不要
2. **AcceptInvitation メール検証** — 5行追加、リスク低
3. **DatabaseRecoveryHelper バックアップ追加** — try-catch + File.copyTo、テスト追加

### Phase 2: HIGH（影響範囲の広い修正）
4. **e.message の DomainError/UiText 化** — HomeViewModel + ExportState の 10+ 箇所
5. **入力バリデーション追加** — CareRecipientViewModel + EmergencyContactViewModel
6. **招待トークンの accept() 期限再チェック** — AcceptInvitationViewModel に 3 行追加

### Phase 3: MEDIUM + 長期改善
7. **ExceptionMasker 拡張と全面適用** — Room 例外サポート追加、Timber 出力箇所更新
8. **Biometric onError 改善** — 再試行ダイアログ追加
9. **Deep link スキーム改善** — App Links 移行準備（実ドメイン設定が前提）

## 未解決の疑問

1. `AppConfig.Member.INVITATION_TOKEN_LENGTH` の実際の値（AppConfig.kt を確認する必要あり）
2. `carenote.example.com` の本番ドメイン — App Links 設定に必須
3. Firestore Security Rules のサーバーサイド検証状況 — クライアントサイドの認可だけでは不十分
4. `EncryptedSharedPreferences` の破損頻度 — 実運用での DatabaseRecoveryHelper 発動率
5. ExportState.Error の message を UiText 化する場合の、CSV/PDF エクスポート全体の影響範囲
