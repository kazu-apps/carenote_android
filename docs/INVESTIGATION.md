# Investigation: 次に実装すべき機能の調査

日付: 2026-02-18
カテゴリ: 調査/分析
対象: CareNote Android — 全機能の完成度・ギャップ・リスク評価から次の実装候補を特定

## 概要

CareNote Android は Clean Architecture + 21 画面 + Firebase 統合の成熟したコードベース。TODO/FIXME ゼロ、全 ViewModel にテスト存在。主な実装候補はプレミアム/Billing UI、カレンダーイベントリマインダー、オフライン状態インジケーターの 3 領域。

## 調査結果

### 事実収集 (researcher)

**コード品質指標:**
- TODO/FIXME/HACK/STUB: **ゼロ件**
- プレースホルダー UI（Coming Soon 等）: **なし**
- 未到達ルート: **なし**（Screen.kt 全ルートが CareNoteNavHost で登録済み）
- 空の Repository 実装: **なし**

**未実装機能:**

| 優先度 | 機能 | 根拠 |
|--------|------|------|
| HIGH | プレミアム/サブスクリプション UI | `BillingRepository`, `PremiumFeatureGuard`, `AppConfig.Billing` のインフラ完成済みだが、設定画面にエントリーポイントなし。strings.xml に premium/billing 文字列ゼロ |
| MEDIUM | FCM トークンサーバー送信 | `CareNoteMessagingService.kt:25-40` — `onNewToken()` と `onMessageReceived()` は Timber ログのみ。バックエンド前提 |
| LOW | Wear OS 対応 | CLAUDE.md に将来予定。別モジュール必要 |
| LOW | CSV データインポート | HANDOVER.md スコープ外記載 |

**Dead Code:**
- `nav_tasks` 文字列 (`strings.xml:9`): Task→CalendarEvent 統合後、Kotlin コードからの参照ゼロ
- `TaskRepository.kt`: 意図的に空ファイル化（Phase 4 で削除済みの記録）

### コード分析 (code-analyst)

**画面別完成度:**

| 画面 | 完成度 | 欠けている機能 |
|------|--------|--------------|
| Timeline | ★★★★★ | — |
| Medication | ★★★★★ | — |
| Notes | ★★★★☆ | タグ未設定ノートの一覧表示なし |
| Calendar | ★★★★☆ | 非タスクイベントのリマインダーなし |
| Settings | ★★★★☆ | Billing/Premium セクションなし |
| HealthRecords | ★★★★☆ | — |
| Home | ★★★☆☆ | アイテム個別タップ→詳細遷移なし（"See All" のみ） |
| Search | ★★★☆☆ | カテゴリフィルタなし |
| EmergencyContacts | ★★★☆☆ | 電話発信・共有・エクスポート機能なし |
| Member Management | ★★★☆☆ | メンバー役割変更（オーナー譲渡）なし |

**テストカバレッジ:**
- 全 ViewModel に対応テスト存在
- 全 Exporter (CSV/PDF × 4カテゴリ) にテスト存在
- `AddEditCalendarEventViewModelTest` のタスクフィールド検証充実度は要確認

**UX 改善余地:**
- オフライン状態インジケーターなし（同期失敗時のユーザー通知なし）
- `contentDescription = null` が 17 箇所（多くは装飾的アイコンで正当）
- 画面遷移アニメーション未統一（Compose Navigation デフォルト使用）
- ダークモード: 完全対応済み

**不足しているエクスポート:**
- EmergencyContact エクスポート
- CalendarEvent（非タスク）単独エクスポート

**不足している通知:**
- カレンダーイベント（非タスク）リマインダー（病院予約等）
- 同期失敗通知
- メンバー招待通知（Cloud Functions バックエンド未実装）

### リスク評価 (risk-assessor)

**技術的負債:**

| 優先度 | 項目 | 場所 | 詳細 |
|--------|------|------|------|
| HIGH | fallbackToDestructiveMigration | `di/DatabaseModule.kt:58` | リリースブロッカー。ユーザーデータ全消滅リスク |
| HIGH | SettingsViewModel @Suppress | `ui/screens/settings/SettingsViewModel.kt:49` | TooManyFunctions 抑制中。サブ ViewModel 分割推奨 |
| MEDIUM | security-crypto alpha | `libs.versions.toml:22` | 1.1.0-alpha06。安定版なし |
| MEDIUM | biometric 古いバージョン | `libs.versions.toml:23` | 1.1.0 (2021年)。1.2.x 系が最新安定版 |
| MEDIUM | baselineprofile alpha | `libs.versions.toml:54` | 1.5.0-alpha02。CI リスク |
| LOW | SwipeToDismissItem deprecated | `ui/components/SwipeToDismissItem.kt:38` | ExperimentalMaterial3Api |

**セキュリティリスク:**

| 優先度 | 項目 | 場所 | 詳細 |
|--------|------|------|------|
| HIGH | BillingRepositoryImpl debugMessage 漏洩 | `data/repository/BillingRepositoryImpl.kt:270,277` | `billingResult.debugMessage` を DomainError に直接格納。内部情報が UI/ログに流れるリスク |
| MEDIUM | Rate Limiting 未実装 | — | Firebase Auth エンドポイントにレートリミットなし（バックエンド依存） |
| LOW | PII ログ | — | 問題なし |
| LOW | Raw クエリ/SQL インジェクション | — | 問題なし |
| LOW | ハードコードシークレット | — | 問題なし |

**UX リスク:**

| 優先度 | 項目 | 場所 | 詳細 |
|--------|------|------|------|
| MEDIUM | AddEditCalendarEventScreen 688行 | `ui/screens/calendar/AddEditCalendarEventScreen.kt` | Task 統合後にフォームフィールド大幅増加。Detekt 閾値 800行未達だが複雑度増加リスク |
| MEDIUM | HomeScreen 571行 | `ui/screens/home/HomeScreen.kt` | サマリーリストに Paging 未使用 |
| LOW | Timeline/Calendar Paging 未使用 | — | 数百件以上で性能問題の可能性 |

## 次に実装すべき機能の推奨順位

### Tier 1: リリース前必須
| # | 機能 | 種別 | 対象 |
|---|------|------|------|
| 1 | fallbackToDestructiveMigration 無効化 + Migration 整備 | リファクタリング | `DatabaseModule.kt`, Migration クラス群 |
| 2 | BillingRepositoryImpl debugMessage セキュリティ修正 | バグ修正 | `BillingRepositoryImpl.kt:270,277` |

### Tier 2: 機能追加（高優先）
| # | 機能 | 種別 | 対象 |
|---|------|------|------|
| 3 | プレミアム/サブスクリプション UI | 機能追加 | Settings 画面 + 新規 Premium 画面 + strings.xml |
| 4 | カレンダーイベント（非タスク）リマインダー | 機能追加 | Worker + NotificationHelper + AddEditCalendarEventScreen |
| 5 | オフライン状態インジケーター | 機能追加 | UI 共通コンポーネント + SyncState 連携 |

### Tier 3: 品質改善（中優先）
| # | 機能 | 種別 | 対象 |
|---|------|------|------|
| 6 | 依存ライブラリ更新 (security-crypto, biometric) | メンテナンス | `libs.versions.toml` |
| 7 | Dead Code 除去 (nav_tasks, TaskRepository.kt) | リファクタリング | strings.xml, domain/repository/ |
| 8 | Home 画面アイテム個別タップ→詳細遷移 | UX 改善 | HomeScreen + HomeViewModel |
| 9 | 画面遷移アニメーション統一 | UX 改善 | CareNoteNavHost.kt |

### Tier 4: 将来（低優先）
| # | 機能 | 種別 | 備考 |
|---|------|------|------|
| 10 | EmergencyContact エクスポート | 機能追加 | CSV/PDF |
| 11 | Search カテゴリフィルタ | UX 改善 | SearchScreen + SearchViewModel |
| 12 | SettingsViewModel 分割 | リファクタリング | サブ ViewModel 化 |
| 13 | FCM リモート通知 | 機能追加 | Cloud Functions バックエンド前提 |
| 14 | Wear OS 対応 | 機能追加 | 別モジュール |

## 関連ファイル

| ファイル | 関連度 | メモ |
|---------|--------|------|
| `di/DatabaseModule.kt:58` | HIGH | fallbackToDestructiveMigration |
| `data/repository/BillingRepositoryImpl.kt:270,277` | HIGH | debugMessage セキュリティ |
| `domain/repository/BillingRepository.kt` | HIGH | プレミアム UI のインターフェース |
| `config/AppConfig.kt` (Billing セクション) | HIGH | プレミアム SKU・制限値 |
| `data/service/CareNoteMessagingService.kt:25-40` | MEDIUM | FCM スタブ |
| `gradle/libs.versions.toml:22-23` | MEDIUM | alpha/古いライブラリ |
| `ui/screens/settings/SettingsViewModel.kt:49` | MEDIUM | @Suppress TooManyFunctions |
| `ui/screens/calendar/AddEditCalendarEventScreen.kt` | MEDIUM | 688行、分割候補 |
| `ui/screens/home/HomeScreen.kt` | LOW | 571行、個別タップ遷移なし |

## 未解決の疑問

1. `AppConfig.Support.CONTACT_EMAIL` は実アドレスに更新済みか？（HANDOVER.md では placeholder と記載）
2. `tasks_*` 系 strings.xml 文字列のうち、AddEditCalendarEventScreen から間接参照されているものはどれか？（Dead Code 判定の精度向上に必要）
3. Billing サーバーサイド検証 (Cloud Functions) の開発タイムラインは？（プレミアム UI 実装の前提条件になりうる）
