# Investigation: Roborazzi CI Golden Image 更新 + Phase 1B Billing Cloud Functions 実現可能性

日付: 2026-02-19
カテゴリ: 調査/分析
対象: HANDOVER.md 残存アクション（Roborazzi CI + Billing Cloud Functions）の実行可能性

## 概要

HANDOVER.md の残りアクション 4 項目のうち、Roborazzi golden image CI 更新と Phase 1B Billing Cloud Functions について調査。Roborazzi は `gh workflow run` で即座にトリガー可能。Billing Cloud Functions は Node.js v22 + npm 10.9 が利用可能だが、Firebase CLI 未インストール・サービスアカウント設定・Google Play Developer API 連携が必要で、実装規模は中程度。

## 調査結果

### 事実収集 (researcher)

#### Roborazzi CI

- `.github/workflows/ci.yml` に `workflow_dispatch` 設定あり
  - input: `update_snapshots` (boolean, description: "Update Roborazzi snapshots")
  - `update_snapshots == true` の場合: `recordRoborazziDebug` を実行し、結果を自動コミット・プッシュ
  - `update_snapshots == false`（デフォルト）: `verifyRoborazziDebug` で検証のみ
- golden image 保存先: `app/src/test/snapshots/` 配下（Roborazzi デフォルト）
- CI では `ubuntu-latest` 上で実行（Linux フォント環境）
- Windows ローカルとのフォントレンダリング差分は既知（HANDOVER.md に soft-fail 対応済み記載）
- トリガーコマンド: `gh workflow run CI -f update_snapshots=true`

#### Billing Cloud Functions

- 既存の Billing 実装:
  - `app/src/main/java/com/carenote/app/domain/model/BillingProduct.kt` — SKU 定義（premium_monthly, premium_yearly）
  - `app/src/main/java/com/carenote/app/domain/model/PurchaseState.kt` — 購入状態 enum
  - `app/src/main/java/com/carenote/app/domain/repository/BillingRepository.kt` — クライアント側インターフェース
  - `app/src/main/java/com/carenote/app/data/repository/BillingRepositoryImpl.kt` — Google Play Billing Library 7.1.1 実装
  - `app/src/main/java/com/carenote/app/data/repository/NoOpBillingRepository.kt` — Billing 非対応時のフォールバック
  - `app/src/main/java/com/carenote/app/di/BillingModule.kt` — DI 設定（BillingAvailability パターン）
  - `app/src/main/java/com/carenote/app/config/AppConfig.kt` — Billing セクション（SKU, 機能制限値）
- purchaseToken のフロー:
  1. ユーザーが `launchBillingFlow()` で購入開始
  2. Google Play が `Purchase` オブジェクトを返す（purchaseToken 含む）
  3. クライアント側で `acknowledgePurchase()` を実行
  4. **サーバーサイド検証なし** — purchaseToken を Google Play Developer API で検証するステップが未実装
- `functions/` ディレクトリ: 存在しない
- `firebase.json`: 存在しない（Firebase プロジェクト初期化未実施）
- Firestore の購入情報保存: 現時点では未実装（クライアント側 BillingClient キャッシュのみ）

### コード分析 (code-analyst)

#### Roborazzi パターン

- Compose Preview Scanner 0.8.1 と統合: `@Preview` アノテーション付き Composable を自動検出してスクリーンショットを生成
- テストファイル: `app/src/test/java/com/carenote/app/screenshots/` 配下
- CI ワークフロー構成:
  - `verifyRoborazziDebug`: golden image との差分検出（CI デフォルト）
  - `recordRoborazziDebug`: golden image 更新（workflow_dispatch + update_snapshots=true 時）
  - 差分検出時は `roborazzi-diffs` アーティファクトとして出力
- soft-fail: CI はスクリーンショット検証失敗時もビルド全体を失敗にしない設定

#### Billing アーキテクチャ

- Clean Architecture 準拠:
  - **domain 層**: `BillingRepository` インターフェース、`BillingProduct` / `PurchaseState` モデル
  - **data 層**: `BillingRepositoryImpl` (Google Play Billing Library)、`NoOpBillingRepository`
  - **di 層**: `BillingModule` (Hilt)、`BillingAvailability` で Google Play Services 有無を検出
  - **ui 層**: `SettingsScreen` / `SettingsViewModel` から購入フロー起動
- Cloud Functions で必要なエンドポイント設計:
  1. `verifyPurchase(purchaseToken, productId, packageName)` — Google Play Developer API で検証
  2. `handleSubscriptionEvent(notification)` — RTDN (Real-Time Developer Notification) ハンドラ
  3. Firestore の `purchases` コレクションに検証結果を永続化

### リスク評価 (risk-assessor)

#### Roborazzi CI

- **リスク: LOW** — `gh workflow run` は安全な操作。golden image の自動コミットは CI 内で完結
- Windows/Linux フォント差分: 既知の問題で soft-fail 対応済み。CI で更新した golden image は Linux 基準になるため、ローカル Windows での verify は差分が出る可能性がある
- 推奨: CI で golden image 更新後、ローカルでの verify はスキップまたは soft-fail 設定を維持

#### Phase 1B Billing Cloud Functions

- **「Claude Code の守備範囲外」再評価**:
  - Node.js v22 + npm 10.9 が利用可能 → Firebase CLI は `npm install -g firebase-tools` で即座にインストール可能
  - Cloud Functions のコード自体は JavaScript/TypeScript で記述可能
  - **しかし**: 以下の外部依存があり完全自動化は困難:
    1. Firebase プロジェクト設定（コンソールでの Functions 有効化）
    2. Google Cloud サービスアカウント秘密鍵の取得・設定
    3. Google Play Developer API の OAuth 設定
    4. RTDN (Real-Time Developer Notification) の Pub/Sub 設定
    5. 本番環境へのデプロイ（`firebase deploy --only functions`）
- **セキュリティリスク: HIGH（未実装時）** — サーバーサイド検証なしでは:
  - 偽造された purchaseToken による不正アクセス
  - 返金後もプレミアム機能が使い続けられる
  - Google Play ポリシー違反の可能性
- **実装規模**: 中程度（3-5 ファイル、~300 行）
  - `functions/src/index.ts` — メインエンドポイント
  - `functions/src/verify.ts` — 検証ロジック
  - `functions/src/firestore.ts` — Firestore 操作
  - `functions/package.json` — 依存定義
  - `firebase.json` — プロジェクト設定
- **結論**: コード実装は Claude Code で可能だが、外部設定（Google Cloud Console, Firebase Console）が必要。コード作成 + ローカルエミュレータテストまでは自動化可能。デプロイは手動ステップ必須。

## 関連ファイル

| ファイル | 関連度 | メモ |
|---------|--------|------|
| `.github/workflows/ci.yml` | HIGH | Roborazzi workflow_dispatch 設定 |
| `app/src/test/snapshots/` | HIGH | Golden image 保存先 |
| `domain/repository/BillingRepository.kt` | HIGH | Billing クライアント側インターフェース |
| `data/repository/BillingRepositoryImpl.kt` | HIGH | Google Play Billing 実装 |
| `di/BillingModule.kt` | MEDIUM | Billing DI 設定 |
| `config/AppConfig.kt` | MEDIUM | Billing SKU 定義 |
| `domain/model/BillingProduct.kt` | MEDIUM | プレミアム商品モデル |
| `domain/model/PurchaseState.kt` | MEDIUM | 購入状態定義 |

## 実行可能性サマリー

| アクション | 実行可能? | 難易度 | 備考 |
|-----------|----------|--------|------|
| Roborazzi golden image CI 更新 | **即実行可** | 低 | `gh workflow run CI -f update_snapshots=true` |
| Phase 1B: Cloud Functions コード作成 | **可能** | 中 | Firebase CLI + エミュレータでローカルテストまで |
| Phase 1B: 本番デプロイ | 半手動 | 高 | Google Cloud Console 設定が必要 |
| リリース APK 実機テスト | 不可 | - | 物理デバイス必要 |
| 問い合わせメール確定 | 不可 | - | ビジネス判断 |

## 未解決の疑問

- Firebase プロジェクト ID は何か？（`google-services.json` 未配置のため確認不可）
- Google Play Developer API のサービスアカウントは作成済みか？
- RTDN (Real-Time Developer Notification) を設定する予定はあるか？
- Cloud Functions のランタイムは Node.js 18 or 20 のどちらを想定？
