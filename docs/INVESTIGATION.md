# Investigation: functions/ firebase-tools 更新コミット安全性

日付: 2026-02-19
カテゴリ: 調査/分析
対象: functions/package.json + package-lock.json の firebase-tools ^13.0.0 → ^15.6.0 更新

## 概要

firebase-tools を devDependency として ^13.0.0 → ^15.6.0 に更新。firebase-tools は CLI ツール（emulator 起動・デプロイ用）であり、ランタイムコードには一切影響しない。安全にコミット可能。

## 調査結果

### 事実収集 (researcher)

- **package.json 変更**: 2点のみ
  1. `firebase-tools`: `^13.0.0` → `^15.6.0`（メジャー2バージョンジャンプ）
  2. `@types/jest` の順序変更（アルファベット順ソート）
- **package-lock.json**: lockfileVersion 3。firebase-tools の依存ツリー変更により約 2400 行の差分
- **functions/src/ のソースコード**:
  - `index.ts`: `firebase-admin` と `firebase-functions` のみ import。firebase-tools は未使用
  - `billing/verifyPurchase.ts`: 同上。firebase-tools 未使用
- **テストファイル**:
  - `billing/__tests__/verifyPurchase.test.ts`: firebase-admin の mock のみ。firebase-tools 未使用
  - `firestore/__tests__/rules.test.ts`: `@firebase/rules-unit-testing` を使用。firebase-tools は CLI 経由（`npx firebase emulators:exec`）で間接利用のみ
- **CI ワークフロー**: `.github/workflows/` に 3 ファイル存在（`build-test.yml`, `e2e-test.yml`, `osv-scanner.yml`）。いずれも functions/ に対するステップなし。firebase-tools のバージョンを CI で直接参照している箇所なし

### コード分析 (code-analyst)

- **互換性: あり（問題なし）**
- firebase-tools は **CLI ツール**（devDependency）。ランタイム依存ではない
- `package.json` scripts で `firebase emulators:start` と `firebase emulators:exec` を使用。これらは firebase-tools 15.x でも同一 CLI インターフェース
- ランタイム依存は `firebase-admin` (^13.0.0) と `firebase-functions` (^6.3.0) のみ — 変更なし
- devDependencies の他パッケージ（`@firebase/rules-unit-testing` ^4.0.0, `firebase` ^11.0.0）は firebase-tools と独立しており互換性問題なし
- functions/src/ の全 .ts ファイルで `firebase-tools` を import している箇所はゼロ

### リスク評価 (risk-assessor)

- **リスクレベル: LOW（安全にコミット可能）**
- 根拠:
  1. firebase-tools は devDependency（CLI ツール）であり、デプロイされるランタイムコードに影響しない
  2. CI ワークフローに functions/ 関連のステップがないため、CI 破損リスクなし
  3. `osv-scanner.toml` に functions/ 関連の除外設定なし（スキャン対象だが、firebase-tools はツールであり脆弱性リスク低）
  4. lockfileVersion 3（npm v7+）で整合性あり。lockfile の大きな差分は firebase-tools の依存ツリー変更による正常な挙動
  5. HANDOVER.md の既知の問題に「functions/ の firebase-tools 15.6.0 更新が未コミット」と明記されており、コミットが期待されている

## 関連ファイル

| ファイル | 関連度 | メモ |
|---------|--------|------|
| `functions/package.json` | HIGH | firebase-tools ^13.0.0 → ^15.6.0、@types/jest 順序変更 |
| `functions/package-lock.json` | HIGH | 依存ツリー更新（~2400行差分） |
| `functions/src/index.ts` | LOW | firebase-tools 未使用（確認済み） |
| `functions/src/billing/verifyPurchase.ts` | LOW | firebase-tools 未使用（確認済み） |
| `.github/workflows/*.yml` | LOW | functions/ 関連ステップなし |
| `app/osv-scanner.toml` | LOW | functions/ 関連の除外設定なし |

## 未解決の疑問

なし。調査の結果、コミットに問題がないことが確認された。

## 結論

**コミット推奨。** firebase-tools は CLI ツール（devDependency）であり、ランタイムコード・CI・OSV-Scanner に影響しない。package-lock.json の大きな差分は依存ツリー更新による正常な挙動。
