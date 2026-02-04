# Firestore スキーマ設計

CareNote Android アプリの Cloud Firestore データ構造設計。

## 設計方針

| 方針 | 説明 |
|------|------|
| **被介護者中心** | 全データを `careRecipients/{id}/` 配下のサブコレクションに配置 |
| **家族共有** | `careRecipientMembers` で複数ユーザーのアクセス権を管理 |
| **ID マッピング** | 各ドキュメントに `localId` フィールドを追加（Room ID 対応） |
| **非正規化** | `authorName` 等を埋め込んでクエリ効率化 |
| **オフラインファースト** | Room をプライマリ、Firestore を同期先として設計 |

## タイムスタンプ形式

| Java 型 | Firestore 形式 | 例 |
|---------|---------------|-----|
| `LocalDateTime` | `Timestamp` | `2025-02-04T10:30:00` → Firestore Timestamp |
| `LocalDate` | `String` (YYYY-MM-DD) | `"2025-02-04"` |
| `LocalTime` | `String` (HH:mm) | `"08:30"` |

## Enum 保存形式

全 Enum は **String** 形式で保存（可読性、後方互換性）。

| Enum | 値 |
|------|-----|
| `MedicationTiming` | `"MORNING"`, `"NOON"`, `"EVENING"` |
| `MedicationLogStatus` | `"TAKEN"`, `"SKIPPED"`, `"POSTPONED"` |
| `NoteTag` | `"CONDITION"`, `"MEAL"`, `"REPORT"`, `"OTHER"` |
| `MealAmount` | `"FULL"`, `"MOSTLY"`, `"HALF"`, `"LITTLE"`, `"NONE"` |
| `ExcretionType` | `"NORMAL"`, `"SOFT"`, `"HARD"`, `"DIARRHEA"`, `"NONE"` |
| `TaskPriority` | `"LOW"`, `"MEDIUM"`, `"HIGH"` |
| `ThemeMode` | `"LIGHT"`, `"DARK"`, `"SYSTEM"` |

---

## コレクション構造

```
firestore-root/
├── users/{userId}/
│   ├── (profile fields)
│   └── settings (embedded or subcollection)
│
├── careRecipients/{careRecipientId}/
│   ├── (profile fields)
│   ├── medications/{medicationId}/
│   │   └── logs/{logId}
│   ├── notes/{noteId}
│   ├── healthRecords/{recordId}
│   ├── calendarEvents/{eventId}
│   └── tasks/{taskId}
│
└── careRecipientMembers/{membershipId}
    ├── userId
    ├── careRecipientId
    └── role
```

---

## ドキュメント詳細

### 1. users/{userId}

Firebase Auth の UID をドキュメント ID として使用。

```typescript
interface UserDocument {
  // Auth UID (ドキュメント ID と同一)
  uid: string;

  // プロフィール
  name: string;
  email: string;
  isPremium: boolean;

  // メタデータ
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // 設定（埋め込み）
  settings: {
    themeMode: string;  // "LIGHT" | "DARK" | "SYSTEM"
    notificationsEnabled: boolean;
    quietHoursStart: number;  // 0-23
    quietHoursEnd: number;    // 0-23

    // 健康閾値
    temperatureHigh: number;
    bloodPressureHighUpper: number;
    bloodPressureHighLower: number;
    pulseHigh: number;
    pulseLow: number;

    // 服薬デフォルト時刻
    morningHour: number;
    morningMinute: number;
    noonHour: number;
    noonMinute: number;
    eveningHour: number;
    eveningMinute: number;
  };

  // FCM トークン（通知用）
  fcmTokens: string[];

  // 最後にアクティブだったデバイス情報
  lastActiveDevice?: string;
}
```

### 2. careRecipients/{careRecipientId}

被介護者プロフィール。UUID v4 をドキュメント ID として使用。

```typescript
interface CareRecipientDocument {
  // 識別子
  id: string;  // ドキュメント ID と同一

  // プロフィール
  name: string;
  nickname: string;
  birthDate: string | null;  // "YYYY-MM-DD" or null
  gender: string;
  careLevel: string;
  medicalHistory: string;
  allergies: string;

  // 作成者
  createdBy: string;  // users/{userId}

  // メタデータ
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

### 3. careRecipients/{id}/medications/{medicationId}

服薬管理。UUID v4 をドキュメント ID として使用。

```typescript
interface MedicationDocument {
  // ID マッピング
  localId: number;  // Room ID (Long)

  // 基本情報
  name: string;
  dosage: string;

  // 服用スケジュール
  timings: string[];  // ["MORNING", "NOON", "EVENING"]
  times: {
    MORNING?: string;  // "08:00"
    NOON?: string;     // "12:00"
    EVENING?: string;  // "18:00"
  };
  reminderEnabled: boolean;

  // メタデータ
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // 同期メタデータ
  syncedAt: Timestamp;
  deletedAt: Timestamp | null;  // 論理削除
}
```

### 4. careRecipients/{id}/medications/{id}/logs/{logId}

服薬記録。UUID v4 をドキュメント ID として使用。

```typescript
interface MedicationLogDocument {
  // ID マッピング
  localId: number;  // Room ID (Long)
  medicationLocalId: number;  // Room medicationId

  // 記録内容
  status: string;  // "TAKEN" | "SKIPPED" | "POSTPONED"
  scheduledAt: Timestamp;
  recordedAt: Timestamp;
  memo: string;

  // 同期メタデータ
  syncedAt: Timestamp;
  deletedAt: Timestamp | null;
}
```

### 5. careRecipients/{id}/notes/{noteId}

メモ・申し送り。UUID v4 をドキュメント ID として使用。

```typescript
interface NoteDocument {
  // ID マッピング
  localId: number;  // Room ID (Long)

  // 内容
  title: string;
  content: string;
  tag: string;  // "CONDITION" | "MEAL" | "REPORT" | "OTHER"

  // 作成者（非正規化）
  authorId: string;     // users/{userId}
  authorName: string;   // 非正規化：クエリ効率化

  // メタデータ
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // 同期メタデータ
  syncedAt: Timestamp;
  deletedAt: Timestamp | null;
}
```

### 6. careRecipients/{id}/healthRecords/{recordId}

健康記録。UUID v4 をドキュメント ID として使用。

```typescript
interface HealthRecordDocument {
  // ID マッピング
  localId: number;  // Room ID (Long)

  // バイタルサイン
  temperature: number | null;
  bloodPressureHigh: number | null;
  bloodPressureLow: number | null;
  pulse: number | null;
  weight: number | null;

  // 生活記録
  meal: string | null;       // "FULL" | "MOSTLY" | "HALF" | "LITTLE" | "NONE"
  excretion: string | null;  // "NORMAL" | "SOFT" | "HARD" | "DIARRHEA" | "NONE"
  conditionNote: string;

  // タイムスタンプ
  recordedAt: Timestamp;
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // 同期メタデータ
  syncedAt: Timestamp;
  deletedAt: Timestamp | null;
}
```

### 7. careRecipients/{id}/calendarEvents/{eventId}

カレンダーイベント。UUID v4 をドキュメント ID として使用。

```typescript
interface CalendarEventDocument {
  // ID マッピング
  localId: number;  // Room ID (Long)

  // 内容
  title: string;
  description: string;

  // 日時
  date: string;           // "YYYY-MM-DD"
  startTime: string | null;  // "HH:mm" or null
  endTime: string | null;    // "HH:mm" or null
  isAllDay: boolean;

  // メタデータ
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // 同期メタデータ
  syncedAt: Timestamp;
  deletedAt: Timestamp | null;
}
```

### 8. careRecipients/{id}/tasks/{taskId}

タスク。UUID v4 をドキュメント ID として使用。

```typescript
interface TaskDocument {
  // ID マッピング
  localId: number;  // Room ID (Long)

  // 内容
  title: string;
  description: string;
  dueDate: string | null;  // "YYYY-MM-DD" or null
  isCompleted: boolean;
  priority: string;  // "LOW" | "MEDIUM" | "HIGH"

  // メタデータ
  createdAt: Timestamp;
  updatedAt: Timestamp;

  // 同期メタデータ
  syncedAt: Timestamp;
  deletedAt: Timestamp | null;
}
```

### 9. careRecipientMembers/{membershipId}

被介護者へのアクセス権管理。複合キー `{userId}_{careRecipientId}` をドキュメント ID として使用。

```typescript
interface CareRecipientMemberDocument {
  // 関連付け
  userId: string;           // users/{userId}
  careRecipientId: string;  // careRecipients/{id}

  // 権限
  role: string;  // "OWNER" | "EDITOR" | "VIEWER"

  // メタデータ
  invitedBy: string;  // users/{userId}
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

---

## ID 戦略

### Room Long ID ↔ Firestore String ID

| 操作 | ID 生成 |
|------|---------|
| Room 新規作成 | auto-increment Long |
| Firestore 新規作成 | UUID v4 |
| 同期時 | `localId` フィールドで Room ID を保持 |

### マッピングテーブル（Room 側）

```kotlin
@Entity(tableName = "sync_mappings")
data class SyncMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String,    // "medication", "note", etc.
    val localId: Long,         // Room ID
    val remoteId: String,      // Firestore document ID
    val lastSyncedAt: Long     // epoch millis
)
```

---

## 同期メタデータ

全ドキュメントに以下のフィールドを追加:

| フィールド | 型 | 説明 |
|-----------|-----|------|
| `syncedAt` | `Timestamp` | 最終同期日時 |
| `deletedAt` | `Timestamp?` | 論理削除日時（null = 有効） |

### 競合解決

**Last-Write-Wins (LWW)** 戦略を採用:
- `updatedAt` のタイムスタンプを比較
- 新しい方を採用
- 競合が発生した場合、両バージョンをログに記録

---

## Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ユーザー自身のドキュメントのみ読み書き可能
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // 被介護者ドキュメント: メンバーのみアクセス可能
    match /careRecipients/{careRecipientId} {
      allow read, write: if isAuthorizedMember(careRecipientId);

      // サブコレクションも同様
      match /{subCollection}/{docId} {
        allow read, write: if isAuthorizedMember(careRecipientId);

        // logs サブコレクション（medications 配下）
        match /logs/{logId} {
          allow read, write: if isAuthorizedMember(careRecipientId);
        }
      }
    }

    // メンバーシップ: 自分のメンバーシップのみ読み取り可能
    match /careRecipientMembers/{membershipId} {
      allow read: if request.auth != null &&
                    resource.data.userId == request.auth.uid;
      allow create: if request.auth != null &&
                      request.resource.data.invitedBy == request.auth.uid;
      allow update, delete: if request.auth != null &&
                              resource.data.userId == request.auth.uid;
    }

    // ヘルパー関数: 認可されたメンバーかどうか
    function isAuthorizedMember(careRecipientId) {
      return request.auth != null &&
             exists(/databases/$(database)/documents/careRecipientMembers/$(request.auth.uid + '_' + careRecipientId));
    }
  }
}
```

---

## 複合インデックス

Firestore Console または `firestore.indexes.json` で設定:

```json
{
  "indexes": [
    {
      "collectionGroup": "medications",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "deletedAt", "order": "ASCENDING" },
        { "fieldPath": "updatedAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "notes",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "deletedAt", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "healthRecords",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "deletedAt", "order": "ASCENDING" },
        { "fieldPath": "recordedAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "calendarEvents",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "deletedAt", "order": "ASCENDING" },
        { "fieldPath": "date", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "tasks",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "deletedAt", "order": "ASCENDING" },
        { "fieldPath": "isCompleted", "order": "ASCENDING" },
        { "fieldPath": "dueDate", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "logs",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "deletedAt", "order": "ASCENDING" },
        { "fieldPath": "scheduledAt", "order": "DESCENDING" }
      ]
    }
  ]
}
```

---

## クエリパターン

### 1. ユーザーがアクセス可能な被介護者一覧

```kotlin
firestore.collection("careRecipientMembers")
    .whereEqualTo("userId", currentUserId)
    .get()
    .await()
    .documents
    .map { it.getString("careRecipientId") }
```

### 2. 被介護者の服薬一覧（削除済み除外）

```kotlin
firestore.collection("careRecipients")
    .document(careRecipientId)
    .collection("medications")
    .whereEqualTo("deletedAt", null)
    .orderBy("updatedAt", Query.Direction.DESCENDING)
    .get()
```

### 3. 特定日のカレンダーイベント

```kotlin
firestore.collection("careRecipients")
    .document(careRecipientId)
    .collection("calendarEvents")
    .whereEqualTo("date", "2025-02-04")
    .whereEqualTo("deletedAt", null)
    .get()
```

### 4. 未完了タスク（期限順）

```kotlin
firestore.collection("careRecipients")
    .document(careRecipientId)
    .collection("tasks")
    .whereEqualTo("deletedAt", null)
    .whereEqualTo("isCompleted", false)
    .orderBy("dueDate")
    .get()
```

---

## 移行考慮事項

### v1.0 → v2.0 データ移行

1. **初回同期時**: Room の全データを Firestore にアップロード
2. **ID マッピング**: `SyncMappingEntity` テーブルで Room ID ↔ Firestore ID を管理
3. **オフライン対応**: Firestore のオフライン永続化を有効化

### 将来の拡張

| 機能 | スキーマ変更 |
|------|-------------|
| 写真添付 | `healthRecords` に `photoUrls: string[]` 追加 |
| コメント | `notes/{id}/comments/{commentId}` サブコレクション |
| 通知履歴 | `users/{id}/notifications/{notificationId}` サブコレクション |

---

## 参照

- [Firestore Data Model](https://firebase.google.com/docs/firestore/data-model)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Firestore Indexes](https://firebase.google.com/docs/firestore/query-data/indexing)
