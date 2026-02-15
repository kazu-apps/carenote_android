package com.carenote.app.di

import com.carenote.app.data.mapper.UserMapper
import android.content.Context
import com.carenote.app.data.repository.FirebaseAnalyticsRepositoryImpl
import com.carenote.app.data.repository.FirebaseAuthRepositoryImpl
import com.carenote.app.data.repository.FirebaseStorageRepositoryImpl
import com.carenote.app.data.repository.NoOpAnalyticsRepository
import com.carenote.app.data.repository.NoOpAuthRepository
import com.carenote.app.data.repository.NoOpStorageRepository
import com.carenote.app.domain.repository.AnalyticsRepository
import com.carenote.app.domain.repository.AuthRepository
import com.carenote.app.domain.repository.StorageRepository
import com.carenote.app.ui.util.RootDetectionChecker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAvailability(): FirebaseAvailability {
        return FirebaseAvailability.check()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(availability: FirebaseAvailability): FirebaseFirestore {
        if (!availability.isAvailable) {
            throw IllegalStateException("Firebase is not available — this provider should only be called via dagger.Lazy")
        }
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(availability: FirebaseAvailability): FirebaseMessaging {
        if (!availability.isAvailable) {
            throw IllegalStateException("Firebase is not available — this provider should only be called via dagger.Lazy")
        }
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(availability: FirebaseAvailability): FirebaseStorage {
        if (!availability.isAvailable) {
            throw IllegalStateException("Firebase is not available — this provider should only be called via dagger.Lazy")
        }
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        availability: FirebaseAvailability,
        userMapper: UserMapper
    ): AuthRepository {
        if (!availability.isAvailable) return NoOpAuthRepository()
        return FirebaseAuthRepositoryImpl(FirebaseAuth.getInstance(), userMapper)
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        availability: FirebaseAvailability,
        storage: dagger.Lazy<FirebaseStorage>,
        rootDetector: RootDetectionChecker
    ): StorageRepository {
        if (!availability.isAvailable) return NoOpStorageRepository()
        return FirebaseStorageRepositoryImpl(storage, rootDetector)
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        availability: FirebaseAvailability,
        @ApplicationContext context: Context
    ): FirebaseAnalytics {
        if (!availability.isAvailable) {
            throw IllegalStateException("Firebase is not available — this provider should only be called via dagger.Lazy")
        }
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        availability: FirebaseAvailability,
        analytics: dagger.Lazy<FirebaseAnalytics>
    ): AnalyticsRepository {
        if (!availability.isAvailable) return NoOpAnalyticsRepository()
        return FirebaseAnalyticsRepositoryImpl(analytics)
    }
}
