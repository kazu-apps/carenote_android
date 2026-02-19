package com.carenote.app.config

/**
 * Analytics 画面名・イベント定数
 */
object AnalyticsConfigs {

    /**
     * Analytics 画面名定数
     */
    object Analytics {
        const val SCREEN_MEDICATION = "medication"
        const val SCREEN_CALENDAR = "calendar"
        const val SCREEN_TASKS = "tasks"
        const val SCREEN_HEALTH_RECORDS = "health_records"
        const val SCREEN_NOTES = "notes"
        const val SCREEN_SETTINGS = "settings"
        const val SCREEN_LOGIN = "login"
        const val SCREEN_REGISTER = "register"
        const val SCREEN_HOME = "home"

        // Auth
        const val EVENT_SIGN_IN = "sign_in"
        const val EVENT_SIGN_UP = "sign_up"
        const val EVENT_SIGN_OUT = "sign_out"
        const val EVENT_PASSWORD_RESET_SENT = "password_reset_sent"
        const val EVENT_PASSWORD_CHANGED = "password_changed"
        const val EVENT_ACCOUNT_DELETED = "account_deleted"

        // Medication
        const val EVENT_MEDICATION_CREATED = "medication_created"
        const val EVENT_MEDICATION_UPDATED = "medication_updated"
        const val EVENT_MEDICATION_DELETED = "medication_deleted"
        const val EVENT_MEDICATION_LOG_RECORDED = "medication_log_recorded"
        const val EVENT_MEDICATION_LOG_EXPORT_CSV = "medication_log_export_csv"
        const val EVENT_MEDICATION_LOG_EXPORT_PDF = "medication_log_export_pdf"

        // Calendar
        const val EVENT_CALENDAR_EVENT_CREATED = "calendar_event_created"
        const val EVENT_CALENDAR_EVENT_UPDATED = "calendar_event_updated"
        const val EVENT_CALENDAR_EVENT_DELETED = "calendar_event_deleted"
        const val EVENT_CALENDAR_EVENT_COMPLETED = "calendar_event_completed"
        const val PARAM_EVENT_TYPE = "event_type"

        // Task
        const val EVENT_TASK_CREATED = "task_created"
        const val EVENT_TASK_UPDATED = "task_updated"
        const val EVENT_TASK_COMPLETED = "task_completed"
        const val EVENT_TASK_UNCOMPLETED = "task_uncompleted"
        const val EVENT_TASK_DELETED = "task_deleted"
        const val EVENT_TASK_EXPORT_CSV = "task_export_csv"
        const val EVENT_TASK_EXPORT_PDF = "task_export_pdf"

        // Health Record
        const val EVENT_HEALTH_RECORD_CREATED = "health_record_created"
        const val EVENT_HEALTH_RECORD_UPDATED = "health_record_updated"
        const val EVENT_HEALTH_RECORD_DELETED = "health_record_deleted"
        const val EVENT_HEALTH_RECORD_EXPORT_CSV = "health_record_export_csv"
        const val EVENT_HEALTH_RECORD_EXPORT_PDF = "health_record_export_pdf"

        // Note
        const val EVENT_NOTE_CREATED = "note_created"
        const val EVENT_NOTE_UPDATED = "note_updated"
        const val EVENT_NOTE_DELETED = "note_deleted"
        const val EVENT_NOTE_EXPORT_CSV = "note_export_csv"
        const val EVENT_NOTE_EXPORT_PDF = "note_export_pdf"

        // Emergency Contact
        const val EVENT_EMERGENCY_CONTACT_CREATED = "emergency_contact_created"
        const val EVENT_EMERGENCY_CONTACT_UPDATED = "emergency_contact_updated"
        const val EVENT_EMERGENCY_CONTACT_DELETED = "emergency_contact_deleted"

        // Care Recipient
        const val EVENT_CARE_RECIPIENT_SAVED = "care_recipient_saved"

        // Sync
        const val EVENT_MANUAL_SYNC = "manual_sync"

        // Params
        const val PARAM_STATUS = "status"

        // Search
        const val SCREEN_SEARCH = "search"
        const val EVENT_SEARCH_PERFORMED = "search_performed"
        const val EVENT_SEARCH_RESULT_CLICKED = "search_result_clicked"

        // Home
        const val EVENT_HOME_SEE_ALL_CLICKED = "home_see_all_clicked"
        const val EVENT_HOME_SECTION_CLICKED = "home_section_clicked"
        const val PARAM_SECTION = "section"
        const val EVENT_HOME_ITEM_CLICKED = "home_item_clicked"
        const val PARAM_ITEM_ID = "item_id"

        // Member
        const val SCREEN_MEMBER_MANAGEMENT = "member_management"
        const val EVENT_INVITATION_SENT = "invitation_sent"
        const val EVENT_INVITATION_ACCEPTED = "invitation_accepted"
        const val EVENT_INVITATION_CANCELLED = "invitation_cancelled"
        const val EVENT_MEMBER_DELETED = "member_deleted"

        // Billing
        const val EVENT_PURCHASE_STARTED = "purchase_started"
        const val EVENT_PURCHASE_RESTORED = "purchase_restored"
        const val EVENT_MANAGE_SUBSCRIPTION = "manage_subscription"
        const val PARAM_PRODUCT_ID = "product_id"
    }
}
