package com.carenote.app.di

import android.content.Context
import com.carenote.app.data.local.NotificationCountDataSource
import com.carenote.app.data.repository.ConnectivityRepositoryImpl
import com.carenote.app.data.repository.PremiumFeatureGuardImpl
import com.carenote.app.domain.repository.BillingRepository
import com.carenote.app.domain.repository.ConnectivityRepository
import com.carenote.app.domain.repository.PremiumFeatureGuard
import com.carenote.app.domain.util.Clock
import com.carenote.app.domain.util.SystemClock
import com.carenote.app.ui.util.RootDetectionChecker
import com.carenote.app.ui.util.RootDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = SystemClock()

    @Provides
    @Singleton
    fun provideRootDetectionChecker(): RootDetectionChecker = RootDetector()

    @Provides
    @Singleton
    fun providePremiumFeatureGuard(
        billingRepository: BillingRepository,
        notificationCountDataSource: NotificationCountDataSource,
        clock: Clock
    ): PremiumFeatureGuard {
        return PremiumFeatureGuardImpl(
            billingRepository, notificationCountDataSource, clock
        )
    }

    @Provides
    @Singleton
    fun provideConnectivityRepository(
        @ApplicationContext context: Context
    ): ConnectivityRepository {
        return ConnectivityRepositoryImpl(context)
    }
}
