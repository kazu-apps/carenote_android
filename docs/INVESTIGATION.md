# Investigation: android.newDsl=false deprecated 警告

日付: 2026-02-17
カテゴリ: 調査/分析
対象: `gradle.properties:18` の `android.newDsl=false` deprecated 警告の原因と対応方針

## 概要

AGP 9.0.1 でビルド時に「android.newDsl=false is deprecated, will be removed in AGP 10.0」警告が表示される。原因は Gradle Play Publisher (GPP) 3.10.1 が AGP 9.0 の新 DSL と非互換のため、回避策として設定されたフラグ。GPP 4.0.0 (2025-01-25 リリース) で修正済みのため、アップグレードで解消可能。

## 調査結果

### 事実収集 (researcher)

- `gradle.properties:18` に `android.newDsl=false` が設定されている。コメント: `# Workaround for Gradle Play Publisher compatibility with AGP 9.0`
- AGP 9.0 で新 DSL インターフェース (`gradle-api` artifact) が安定版となり、`android.newDsl=true` がデフォルトに変更
- `android.newDsl=false` は旧 deprecated DSL インターフェースへの明示的フォールバック
- GPP 3.10.1 (`libs.versions.toml:176`) は `BaseAppModuleExtension` 等の旧 DSL 型に依存しており、AGP 9.0 の新 DSL では `Extension of type 'BaseAppModuleExtension' does not exist` エラーが発生
- **GPP 4.0.0** (2025-01-25 リリース) で AGP 9.0 + `newDsl=true` 完全サポート済み (GitHub Issue #1175 → PR #1181 でクローズ)
- `android.newDsl=false` は AGP 10.0 (2026年後半予定) で**完全削除**
- `play {}` ブロック: `app/build.gradle.kts:107-111` に `serviceAccountCredentials`, `defaultToAppBundles`, `track` の3設定

### コード分析 (code-analyst)

- 全 `build.gradle.kts` (`app`, `benchmark`, `baselineprofile`) の android {} ブロックは**新 DSL 構文のみ**使用
  - `namespace =`, `compileSdk =`, `buildFeatures { compose = true }` 等 — 全て新 DSL 互換
  - 旧 DSL 構文（`compileSdkVersion(...)` 関数呼び出し形式）は不使用
- `android.newDsl=false` に依存しているのは**play-publisher プラグインのみ**
- play-publisher は `app/build.gradle.kts:11` で `alias(libs.plugins.play.publisher)` として apply
- CI ワークフロー (`.github/workflows/ci.yml`) に play-publisher 直接呼び出しステップはないが、`assembleDebug` でプラグイン設定フェーズが走るため間接影響あり
- `play {}` ブロックの構文は GPP 3.x → 4.x で API 互換

### リスク評価 (risk-assessor)

| リスク項目 | レベル | 詳細 |
|-----------|--------|------|
| 行削除のみ（GPP 据え置き） | **HIGH** | play-publisher 3.10.1 はビルド破壊確定 |
| AGP 10.0 対応期限 | **MEDIUM** | 2026年後半までに対応必須（技術的負債） |
| GPP 4.0.0 アップグレードリスク | **LOW** | API 互換あり、AGP 9.0.1 対応済み |
| セキュリティ直接リスク | **LOW** | ビルド設定フラグのみ、直接的な脆弱性なし |
| セキュリティ間接リスク | **LOW** | AGP アップグレードブロッカーによるパッチ遅延の可能性 |

## 推奨対応

| 選択肢 | リスク | 工数 | 推奨 |
|--------|--------|------|------|
| (a) 行削除のみ | HIGH（ビルド破壊） | 低 | ❌ |
| (b) GPP 3.10.1 → 4.0.0 + 行削除 | LOW | 低〜中 | ✅ **推奨** |
| (c) GPP 削除 + 行削除 | LOW | 高 | △ |

**推奨: 選択肢 (b)** — 最小工数で deprecated 警告を解消し、AGP 10.0 への移行パスを確保。

修正手順:
1. `libs.versions.toml` の play-publisher を `"3.10.1"` → `"4.0.0"` に変更
2. `gradle.properties` から `android.newDsl=false` 行を削除
3. `./gradlew.bat assembleDebug` でビルド確認
4. `play {}` ブロックの構文変更は不要（API 互換）

## 関連ファイル

| ファイル | 関連度 | メモ |
|---------|--------|------|
| `gradle.properties:18` | HIGH | `android.newDsl=false` の設定箇所 |
| `gradle/libs.versions.toml:176` | HIGH | play-publisher バージョン定義 |
| `app/build.gradle.kts:11` | HIGH | play-publisher プラグイン適用 |
| `app/build.gradle.kts:107-111` | MEDIUM | `play {}` 設定ブロック |
| `app/build.gradle.kts:28-105` | LOW | android {} ブロック（既に新 DSL 互換） |

## 未解決の疑問

- GPP 4.0.0 の `play {}` ブロック設定が完全互換かはビルド実行で最終確認が必要
- `api-key.json` (サービスアカウントキー) の `.gitignore` 設定は未確認（別途セキュリティ確認推奨）
