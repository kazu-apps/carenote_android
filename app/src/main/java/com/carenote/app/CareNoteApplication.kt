package com.carenote.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.carenote.app.di.FirebaseAvailability
import com.carenote.app.ui.util.CrashlyticsTree
import com.carenote.app.ui.util.NotificationHelper
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class CareNoteApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        initializeAppCheck()

        // 通知チャンネル作成（Android 8+）
        notificationHelper.createNotificationChannels()
    }

    private fun initializeAppCheck() {
        if (BuildConfig.DEBUG) return
        if (!FirebaseAvailability.check().isAvailable) return

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        Timber.d("Firebase App Check initialized")
    }
}
