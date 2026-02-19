package com.carenote.app.config

/**
 * アプリケーション全体の設定値を集約
 * マジックナンバーを避け、一元管理することで保守性を向上
 *
 * 各カテゴリの実定義は個別ファイルの object に配置。
 * このファイルは後方互換のための delegation facade。
 */
object AppConfig {

    // Domain
    val Paging get() = DomainConfigs.Paging
    val Medication get() = DomainConfigs.Medication
    val HealthThresholds get() = DomainConfigs.HealthThresholds
    val Note get() = DomainConfigs.Note
    val HealthRecord get() = DomainConfigs.HealthRecord
    val Graph get() = DomainConfigs.Graph
    val Calendar get() = DomainConfigs.Calendar
    val Task get() = DomainConfigs.Task
    val Home get() = DomainConfigs.Home
    val Timeline get() = DomainConfigs.Timeline
    val CareRecipient get() = DomainConfigs.CareRecipient
    val EmergencyContact get() = DomainConfigs.EmergencyContact

    // UI
    val UI get() = UiConfigs.UI
    val Theme get() = UiConfigs.Theme
    val Time get() = UiConfigs.Time
    val Legal get() = UiConfigs.Legal
    val Support get() = UiConfigs.Support

    // Notification
    val Notification get() = NotificationConfigs.Notification

    // Analytics
    val Analytics get() = AnalyticsConfigs.Analytics

    // Auth & Security
    val Auth get() = AuthSecurityConfigs.Auth
    val Session get() = AuthSecurityConfigs.Session
    val Security get() = AuthSecurityConfigs.Security
    val Biometric get() = AuthSecurityConfigs.Biometric

    // Data
    val Sync get() = DataConfigs.Sync
    val Photo get() = DataConfigs.Photo
    val Export get() = DataConfigs.Export
    val Widget get() = DataConfigs.Widget

    // Integration
    val Billing get() = IntegrationConfigs.Billing
    val Fcm get() = IntegrationConfigs.Fcm
    val Member get() = IntegrationConfigs.Member
}
