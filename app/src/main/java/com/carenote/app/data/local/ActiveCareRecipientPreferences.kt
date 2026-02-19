package com.carenote.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveCareRecipientPreferences @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    val activeCareRecipientId: Flow<Long> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readActiveId())
        }
        send(readActiveId())
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun setActiveCareRecipientId(id: Long) {
        prefs.edit().putLong(KEY_ACTIVE_CARE_RECIPIENT_ID, id).apply()
    }

    private fun readActiveId(): Long {
        return prefs.getLong(KEY_ACTIVE_CARE_RECIPIENT_ID, DEFAULT_ID)
    }

    companion object {
        private const val PREFS_FILE_NAME = "carenote_active_recipient"
        private const val KEY_ACTIVE_CARE_RECIPIENT_ID = "active_care_recipient_id"
        private const val DEFAULT_ID = 0L
    }
}
