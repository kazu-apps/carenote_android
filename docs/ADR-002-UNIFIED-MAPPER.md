# ADR-002: 統合 Mapper インターフェース設計

## ステータス: 採択（Option C: 統合しない）

## 日付: 2026-02-05

## コンテキスト

CareNote Android は 6 つのドメインモデルに対し、2 種類のマッパーインターフェースを持つ:

1. **Local Mapper** (`Mapper<Entity, Domain>`) — Room Entity と Domain Model の双方向変換
2. **Remote Mapper** (`RemoteMapper<Domain>`) — Firestore Document (`Map<String, Any?>`) と Domain Model の双方向変換

v2.2 TDD リファクタリング完了時点で、これらの統合可能性を評価する。

## 現状分析

### インターフェース比較

| | Local Mapper | Remote Mapper |
|---|---|---|
| **パッケージ** | `data.mapper` | `data.mapper.remote` |
| **型パラメータ** | `<Entity, Domain>` | `<Domain>` |
| **入力型** | Room Entity (型安全) | `Map<String, Any?>` (型なし) |
| **出力型** | Room Entity (型安全) | `Map<String, Any?>` (型なし) |
| **メソッド** | `toDomain(Entity)`, `toEntity(Domain)` | `toDomain(Map)`, `toRemote(Domain, SyncMetadata?)` |
| **リスト変換** | `toDomainList`, `toEntityList` | `toDomainList` |
| **追加責務** | なし | `extractSyncMetadata()` |
| **時刻変換** | ISO 文字列 ↔ `LocalDateTime` | Firestore Timestamp ↔ `LocalDateTime` |
| **Enum 変換** | `valueOf()` + try-catch fallback | 同左 |
| **複雑型の直列化** | CSV (`,`) / セミコロン (`;`) 区切り文字列 | `List<String>` / `Map<String, String>` ネイティブ |
| **DI** | `@Singleton @Inject` | `@Singleton @Inject` + `FirestoreTimestampConverter` |
| **特殊ケース** | なし | `NoteRemoteMapper.toRemote(domain, authorName, syncMetadata)` |

### 対象モデル（6 つ全てが両方を持つ）

| ドメインモデル | Local Mapper | Remote Mapper |
|---|---|---|
| Medication | `MedicationMapper` | `MedicationRemoteMapper` |
| MedicationLog | `MedicationLogMapper` | `MedicationLogRemoteMapper` |
| Note | `NoteMapper` | `NoteRemoteMapper` |
| HealthRecord | `HealthRecordMapper` | `HealthRecordRemoteMapper` |
| CalendarEvent | `CalendarEventMapper` | `CalendarEventRemoteMapper` |
| Task | `TaskMapper` | `TaskRemoteMapper` |

追加: `UserMapper` (Local のみ、Remote 対応なし)

### 重複ロジック

1. **Enum パース**: `valueOf()` + try-catch + fallback パターンが両方に存在
2. **フィールドマッピング**: `id`, `createdAt`, `updatedAt` の変換が共通概念
3. **リスト変換**: `toDomainList()` のデフォルト実装

### 根本的な相違点

1. **入出力型が完全に異なる**: Entity (コンパイル時型安全) vs `Map<String, Any?>` (実行時キャスト)
2. **時刻変換方式が異なる**: `DateTimeFormatter.ISO_LOCAL_DATE_TIME` vs `FirestoreTimestampConverter`
3. **直列化方式が異なる**: CSV/セミコロン区切り文字列 vs Firestore ネイティブ List/Map
4. **Remote 固有の責務**: `SyncMetadata` の注入と抽出
5. **Remote 固有の依存**: `FirestoreTimestampConverter` の DI 注入
6. **NoteRemoteMapper の特殊シグネチャ**: `toRemote(domain, authorName, syncMetadata)` オーバーロード

## 選択肢

### Option A: 完全統合 — 単一インターフェースに統合

```kotlin
interface UnifiedMapper<Domain, Local, Remote> {
    // Local 変換
    fun toDomain(entity: Local): Domain
    fun toLocal(domain: Domain): Local

    // Remote 変換
    fun toDomain(data: Remote): Domain  // コンパイルエラー: 同名メソッド
    fun toRemote(domain: Domain, syncMetadata: SyncMetadata?): Remote
    fun extractSyncMetadata(data: Remote): SyncMetadata
}
```

**問題点**:
- `toDomain()` が 2 つの異なる入力型を受けるため、JVM のメソッドシグネチャ衝突が発生
- 型パラメータが 3 つ (`Domain`, `Local`, `Remote`) で複雑
- `UserMapper` は Remote を持たないため、空実装が必要
- `NoteRemoteMapper` の特殊 `toRemote` が収まらない

**評価**: 技術的に不可能（メソッドシグネチャ衝突）。回避策（メソッド名変更）は可読性を損なう。

### Option B: 共通基底 + 拡張 — 共通ロジックを抽出

```kotlin
// 共通ユーティリティ
object MapperUtils {
    fun <T : Enum<T>> parseEnum(value: String, enumClass: Class<T>, fallback: T): T
    fun parseEnumList(value: Any?, enumClass: Class<T>): List<T>
}

// 既存インターフェースはそのまま維持
interface Mapper<Entity, Domain> { ... }
interface RemoteMapper<Domain> { ... }
```

**問題点**:
- 共有可能なロジックが Enum パース程度に限定される
- 時刻変換は方式が根本的に異なるため共有不可
- 直列化方式も異なるため共有不可
- ユーティリティクラス追加のメリットが小さい

**評価**: 実装可能だが、効果が限定的。Enum パース共通化のために新規ファイル追加は過剰。

### Option C: 統合しない — 現状維持

両インターフェースを独立して維持。変更なし。

**利点**:
- 現在のコードは安定し、全テスト (150+ RemoteMapper テスト含む) が通過
- 各マッパーの責務が明確に分離されている
- Room 層と Firestore 層が独立して進化可能
- 変更リスクゼロ

**評価**: 現時点で最も合理的。

## 判定

**Option C（統合しない）を採択する。**

### 理由

1. **型の非互換性**: Local Mapper は型安全な Entity を扱い、Remote Mapper は `Map<String, Any?>` を扱う。この根本的な型の違いは、統合しても解消されず、むしろ複雑さを増す。

2. **変換ロジックの相違**: 時刻変換（ISO 文字列 vs Firestore Timestamp）、直列化（CSV vs List）が根本的に異なり、共通化の余地が小さい。

3. **共有ロジックの規模**: 重複しているのは Enum パース（try-catch + fallback）程度。これは各マッパーで 3-5 行のコードであり、ユーティリティ抽出の閾値に達しない。

4. **安定性**: 現在の 12 実装ファイルは全てテスト済みで安定稼働中。統合のために全ファイルを変更するリスクに見合うメリットがない。

5. **関心の分離**: Local Mapper は Room スキーマ変更時に、Remote Mapper は Firestore スキーマ変更時に、それぞれ独立して修正できる。統合するとこの利点が失われる。

### リスク評価（統合した場合）

| リスク | 影響 | 確率 |
|--------|------|------|
| 12 ファイル同時変更によるリグレッション | 高（全データ変換に影響） | 中 |
| テスト書き直し（150+ テスト） | 中（工数大） | 高 |
| NoteRemoteMapper 特殊シグネチャの対応困難 | 中 | 高 |
| 新メンバーの学習コスト増（3 型パラメータ） | 低 | 中 |

### 将来の再評価条件

以下の条件が満たされた場合、統合を再検討する:

1. 第 3 のデータソース（例: GraphQL API）が追加され、マッパーの種類が 3 つ以上になった場合
2. Enum パース以外にも重複ロジックが顕著に増加した場合
3. マッパーインターフェース自体に破壊的変更が必要になった場合

## 影響

- コード変更なし
- Item 102（Mapper 統合実装）は本 ADR の判定により **不要** と判断
- 既存のテストスイートに変更なし
