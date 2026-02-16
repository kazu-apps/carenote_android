# プライバシーポリシー / Privacy Policy

最終更新日 / Last Updated: 2026-02-15

---

## 日本語

### はじめに

ケアのて（以下「本アプリ」）をご利用いただきありがとうございます。本プライバシーポリシーは、本アプリにおける個人情報の取り扱いについて説明します。

### 収集する情報

本アプリは以下の情報を端末内およびクラウド（後述の Firebase サービス）に保存します。

- メールアドレスおよび認証情報（アカウント登録・ログインに使用）
- 服薬情報（薬の名前、用量、服用タイミング、服薬記録）
- 健康記録（体温、血圧、脈拍、体重、食事量、排泄、体調メモ）
- カレンダー予定（通院、訪問介護等の予定）
- タスク（介護に関する日常タスク）
- メモ・申し送り（介護に関するメモ）
- 写真（介護記録に添付された画像）
- アプリ設定（通知設定、健康記録の閾値、服薬デフォルト時刻）

### データの保存場所

データはお使いの端末内（ローカルストレージ）に暗号化して保存されるほか、家族間でのデータ共有・同期のために Google が運営する Firebase クラウドサーバーにも保存されます。クラウドへのデータ送信はすべて暗号化通信（TLS）で保護されており、必要最小限のデータのみが送信されます。

### Firebase サービスの利用

本アプリは Google が提供する Firebase プラットフォームの以下のサービスを利用しています。

- **Firebase Authentication**: アカウントの認証（メールアドレスとパスワードによるログイン）に使用します。メールアドレスと認証トークンが Google のサーバーに保存されます。
- **Cloud Firestore**: 家族間でのデータ同期（服薬情報、健康記録、カレンダー予定、タスク、メモ等）に使用します。同期対象のデータが Google のサーバーに暗号化して保存されます。
- **Firebase Storage**: 介護記録に添付された写真の保存に使用します。画像データが Google のサーバーに保存されます。
- **Firebase Analytics**: アプリの使用状況を匿名で分析するために使用します。収集されるデータは統計情報のみであり、個人を特定する情報は含まれません。
- **Firebase Crashlytics**: アプリのクラッシュ（異常終了）を検知・分析するために使用します。端末の種類や OS バージョン等の技術的な情報のみが送信され、個人を特定する情報は含まれません。
- **Firebase Cloud Messaging (FCM)**: プッシュ通知の配信に使用します。通知トークンが Google のサーバーに保存されます。

### 第三者との共有

本アプリは、上記の Firebase サービスを通じて Google のサーバーにデータを保存します。これは本アプリの機能（認証、データ同期、通知等）を提供するために必要な範囲に限定されます。Google は独自のプライバシーポリシーに基づいてデータを管理しています。詳細は [Google プライバシーポリシー](https://policies.google.com/privacy) をご確認ください。

上記以外の第三者に対して、お客様の個人情報を販売、貸与、または共有することはありません。

### 広告・トラッキング

本アプリは広告を表示しません。Firebase Analytics を使用していますが、これは匿名の統計情報（画面閲覧数、機能使用頻度等）の収集のみを目的としており、広告配信やユーザーの行動追跡には使用しません。

### データの削除

アプリをアンインストールすると、端末内に保存されたすべてのデータが削除されます。アプリ内の設定画面からアカウントを削除すると、Firebase Authentication のアカウント（認証情報）が削除されます。Cloud Firestore および Firebase Storage に保存された同期データの自動削除機能は現在開発中です。クラウドデータの削除をご希望の場合は、support-carenote@ks-apps.org までお問い合わせください。

また、アプリ内の設定画面から「デフォルト設定に戻す」を選択することで、設定値を初期状態に戻すことができます。

### データの安全性

本アプリはお客様のデータの安全性を重視しています。

- 端末内のデータは SQLCipher による暗号化で保護されています
- クラウドとの通信はすべて TLS（暗号化通信）で保護されています
- Firebase サービスは Google のセキュリティ基準に基づいて運営されています
- 必要最小限のデータのみをクラウドに送信します

### 子どものプライバシー

本アプリは介護者（成人）を対象としており、13歳未満のお子様を対象としたサービスではありません。

### プライバシーポリシーの変更

本ポリシーは予告なく変更される場合があります。変更があった場合は、アプリ内で最新のポリシーを掲載します。

### お問い合わせ

本ポリシーに関するお問い合わせは、以下までご連絡ください。

メール: support-carenote@ks-apps.org

---

## English

### Introduction

Thank you for using CareNote (the "App"). This Privacy Policy explains how the App handles your personal information.

### Information We Collect

The App stores the following information on your device and in the cloud (via Firebase services described below):

- Email address and authentication credentials (used for account registration and login)
- Medication information (names, dosages, timing, medication logs)
- Health records (temperature, blood pressure, pulse, weight, meal intake, excretion, condition notes)
- Calendar events (hospital visits, home care visits, etc.)
- Tasks (daily caregiving tasks)
- Notes and handovers (caregiving memos)
- Photos (images attached to care records)
- App settings (notification settings, health thresholds, default medication times)

### Where Data Is Stored

Data is stored in encrypted form on your device (local storage) and also on Firebase cloud servers operated by Google for the purpose of data sharing and synchronization among family members. All data transmitted to the cloud is protected by encrypted communication (TLS), and only the minimum necessary data is transmitted.

### Use of Firebase Services

The App uses the following services from Google's Firebase platform:

- **Firebase Authentication**: Used for account authentication (login with email and password). Your email address and authentication tokens are stored on Google's servers.
- **Cloud Firestore**: Used for data synchronization among family members (medication information, health records, calendar events, tasks, notes, etc.). Synchronized data is stored in encrypted form on Google's servers.
- **Firebase Storage**: Used for storing photos attached to care records. Image data is stored on Google's servers.
- **Firebase Analytics**: Used for anonymous analysis of app usage. Only statistical data is collected, and no personally identifiable information is included.
- **Firebase Crashlytics**: Used to detect and analyze app crashes. Only technical information such as device type and OS version is transmitted, and no personally identifiable information is included.
- **Firebase Cloud Messaging (FCM)**: Used for delivering push notifications. Notification tokens are stored on Google's servers.

### Third-Party Sharing

The App stores data on Google's servers through the Firebase services described above. This is limited to the scope necessary to provide the App's functionality (authentication, data synchronization, notifications, etc.). Google manages data in accordance with its own privacy policy. For details, please refer to [Google Privacy Policy](https://policies.google.com/privacy).

We do not sell, rent, or share your personal information with any third parties other than those described above.

### Advertising and Tracking

The App does not display advertisements. While we use Firebase Analytics, it is solely for collecting anonymous statistical information (such as screen views and feature usage frequency) and is not used for ad delivery or tracking user behavior.

### Data Deletion

Uninstalling the App will delete all data stored on your device. Data stored in the cloud (Firebase) can be deleted by deleting your account from the Settings screen within the App. Deleting your account will remove all authentication information and synchronized data from the cloud.

You can also reset settings to their defaults from the Settings screen within the App.

### Data Security

The App places great importance on the security of your data:

- Data on your device is protected by SQLCipher encryption
- All communication with the cloud is protected by TLS (encrypted communication)
- Firebase services are operated in accordance with Google's security standards
- Only the minimum necessary data is transmitted to the cloud

### Children's Privacy

The App is intended for caregivers (adults) and is not directed at children under 13.

### Changes to This Policy

This policy may be updated without prior notice. Any changes will be reflected within the App.

### Contact Us

For questions about this policy, please contact us at:

Email: support-carenote@ks-apps.org
