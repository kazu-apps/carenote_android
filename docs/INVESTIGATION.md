# Investigation: 次に実装すべき機能の優先度分析

日付: 2026-02-19
カテゴリ: 調査/分析
対象: CareNote Android の次期実装優先度。リリースブロッカー、未実装機能、技術的負債を網羅的に評価

## 概要

アプリは主要機能（服薬管理、健康記録、カレンダー、タスク、メモ、タイムライン）が実装済みで、全21 ViewModel にテスト完備、TODO/FIXME ゼロの健全な状態。ただし Billing UI 未実装、Firestore sync 基盤欠損、applicationId に仮文字列残存など、リリースブロッカーが複数存在する。

## 調査結果

### 事実収集 (researcher)

**全画面ルート**: 32ルート実装済み（ボトムナビ 6画面 + 設定 + 認証 + オンボーディング + CRUD 各画面）

**Firestore sync 欠損**:
- `SyncWorker.kt:151` の `getCareRecipientId()` は `careRecipientMembers` コレクションを読み取り専用で使用
- `careRecipientMembers` への**書き込みコードが一切存在しない** — 新規ユーザー登録・招待受諾時に Firestore へメンバー登録されないため、sync 全体が非機能
- HANDOVER.md に `[CRITICAL] Firestore sync 基盤欠損` として既知

**Billing フロー欠損**:
- `BillingRepositoryImpl.kt` (345行): Google Play Billing Client 統合、購入フロー・Product 照会・購入検証まで実装済み
- **`BillingScreen` と `BillingViewModel` が未実装** — ユーザーがプレミアム購入を行う UI が存在しない
- `UserMapper.kt:21` で `isPremium = false` がハードコード — 購入後もプレミアム状態が反映されない
- プレミアム機能ゲーティング: `PremiumFeatureGuard` インターフェースで `TaskReminderWorker` と `SettingsViewModel` に実装済み

**テストカバレッジ**: 全21 ViewModel にテストファイルあり + `SyncStatusViewModelTest` 追加で22テストファイル

**NoOp 実装**: 7クラス（NoOpAuthRepository, NoOpSyncRepository, NoOpBillingRepository, NoOpAnalyticsRepository, NoOpStorageRepository, NoOpPurchaseVerifier, NoOpSyncWorkScheduler）

**TODO/FIXME/HACK**: `app/src/main/` 配下で**ゼロ件**

**大きなファイル**:

| ファイル | 行数 | 備考 |
|---------|------|------|
| `HomeScreen.kt` | 706 | Detekt LargeClass 800行閾値に近接 |
| `CareNoteNavHost.kt` | 686 | 拡張関数で分割済みだが本体が大きい |
| `SettingsScreen.kt` | 610 | |
| `SettingsViewModel.kt` | 551 | |
| `HealthRecordsScreen.kt` | 527 | |

**エクスポート機能**: HealthRecords 画面から CSV/PDF、Settings から タスクCSV・ノートCSV。UI エントリポイントと実装の両方が完備

**Widget**: 今日の服薬リスト + 未完了タスク表示。Widget 全体タップで MainActivity 起動するが、個別アイテムへのディープリンクなし

### コード分析 (code-analyst)

**applicationId 問題**:
- `app/build.gradle.kts:44` — `applicationId = "com.carenote.original.app"` に `original` という仮文字列が残存
- リリース前に正式 ID への変更が**必須**（一度公開すると変更不可）

**署名設定**:
- `app/build.gradle.kts:58-89` — `key.properties` 未配置時、release ビルドが未署名になる条件付き設定
- Google Play Console でアップロード鍵を設定する前提で問題なし

**プライバシーポリシー / 利用規約**:
- アプリ内に JP/EN テキストファイルとして同梱済み（`assets/legal/` に4ファイル）
- 設定画面からアクセス可能
- **Google Play Console 申請時に外部 URL 登録が別途必要**

**ネットワークセキュリティ**: `network_security_config.xml` で平文 HTTP 全面禁止。完璧

**i18n**: `strings.xml` 日英 691 文字列で完全同期

**ProGuard**: 121行、主要ライブラリ全てカバー（Room, Hilt, Coroutines, Firebase, SQLCipher, Billing, Coil, Paging, WorkManager 等）

**アクセシビリティ**: 主要画面で `contentDescription` を `a11y_` プレフィックス文字列リソースで設定。装飾的アイコンは `null` 設定。良好

**オンボーディング**: シングル画面（アイコン + タイトル + 説明 + 開始ボタン）。機能紹介や権限説明なし

### リスク評価 (risk-assessor)

**リリースブロッカー（CRITICAL）**:

| # | 問題 | 影響 |
|---|------|------|
| 1 | `applicationId` に `original` 残存 | 一度公開すると変更不可。公開前に修正必須 |
| 2 | Billing UI 未実装（BillingScreen/ViewModel なし） | プレミアム機能が購入不可。収益化が不能 |
| 3 | `isPremium = false` ハードコード | 購入してもプレミアム状態にならない |

**HIGH リスク**:

| # | 問題 | 影響 |
|---|------|------|
| 4 | Firestore sync 基盤欠損（careRecipientMembers 書き込みなし） | 家族間データ共有が非機能 |
| 5 | Google Play Console にプライバシーポリシー外部 URL 必要 | ストア審査でリジェクトされる |
| 6 | support@carenote.app がプレースホルダー | ユーザーサポート不能 |

**MEDIUM リスク**:

| # | 問題 | 影響 |
|---|------|------|
| 7 | HomeScreen.kt 706行（LargeClass 800行閾値近接） | 次の機能追加で Detekt 違反の可能性 |
| 8 | CareNoteNavHost.kt 686行 | 同上 |
| 9 | オンボーディングがシングル画面 | ユーザー離脱率に影響 |
| 10 | Widget に個別ディープリンクなし | UX 改善余地 |

**LOW リスク**:

| # | 問題 | 影響 |
|---|------|------|
| 11 | `android:allowBackup="true"` | SharedPreferences がバックアップされる（DB は除外済み） |
| 12 | SettingsViewModel.kt 551行、SettingsScreen.kt 610行 | 保守性の低下 |

## 関連ファイル

| ファイル | 関連度 | メモ |
|---------|--------|------|
| `app/build.gradle.kts:44` | CRITICAL | applicationId に `original` 残存 |
| `data/mapper/UserMapper.kt:21` | CRITICAL | isPremium = false ハードコード |
| `data/repository/BillingRepositoryImpl.kt` | HIGH | 購入ロジック実装済みだが UI なし |
| `data/worker/SyncWorker.kt:151-158` | HIGH | careRecipientMembers 読み取り専用 |
| `ui/screens/home/HomeScreen.kt` | MEDIUM | 706行 |
| `ui/navigation/CareNoteNavHost.kt` | MEDIUM | 686行 |
| `ui/navigation/Screen.kt` | LOW | 32ルート定義（参照用） |
| `assets/legal/` | LOW | プライバシーポリシー/利用規約テキスト |

## 未解決の疑問

- `applicationId` の正式名はユーザーが決定すべき（ビジネス判断）
- Billing はリリース前に必要か、それとも無料版としてまずリリースするか（ビジネス判断）
- Firestore sync は MVP に含めるか、ローカルオンリーで初回リリースするか（ビジネス判断）

## 結論

**推奨実装優先度**:

1. **applicationId 修正** — 1行変更だが最重要。公開後は変更不可
2. **Billing UI 実装** (BillingScreen + BillingViewModel + isPremium 反映) — 収益化の前提条件
3. **Firestore 初期セットアップフロー** — sync 基盤を機能させる前提。ただし「ローカルオンリーで先にリリース」も選択肢
4. **大きなファイル分割** (HomeScreen, CareNoteNavHost) — Detekt 閾値超過の予防
5. **オンボーディング強化** — ユーザーリテンション向上
