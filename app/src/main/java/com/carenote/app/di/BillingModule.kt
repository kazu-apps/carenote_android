package com.carenote.app.di

import android.content.Context
import com.carenote.app.config.AppConfig
import com.carenote.app.data.local.dao.PurchaseDao
import com.carenote.app.data.mapper.PurchaseMapper
import com.carenote.app.data.remote.FirebasePurchaseVerifier
import com.carenote.app.data.remote.NoOpPurchaseVerifier
import com.carenote.app.data.remote.PurchaseVerifier
import com.carenote.app.data.repository.BillingRepositoryImpl
import com.carenote.app.data.repository.NoOpBillingRepository
import com.carenote.app.domain.repository.BillingRepository
import com.carenote.app.domain.util.Clock
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBillingAvailability(
        @ApplicationContext context: Context
    ): BillingAvailability {
        return BillingAvailability.check(context)
    }

    @Provides
    @Singleton
    fun providePurchaseVerifier(
        firebaseAvailability: FirebaseAvailability
    ): PurchaseVerifier {
        if (!firebaseAvailability.isAvailable) return NoOpPurchaseVerifier()
        return FirebasePurchaseVerifier(
            dagger.Lazy { FirebaseFunctions.getInstance(AppConfig.Billing.CLOUD_FUNCTIONS_REGION) }
        )
    }

    @Provides
    @Singleton
    fun provideBillingRepository(
        availability: BillingAvailability,
        @ApplicationContext context: Context,
        purchaseDao: PurchaseDao,
        purchaseMapper: PurchaseMapper,
        clock: Clock,
        purchaseVerifier: PurchaseVerifier
    ): BillingRepository {
        if (!availability.isAvailable) return NoOpBillingRepository()
        return BillingRepositoryImpl(context, purchaseDao, purchaseMapper, clock, purchaseVerifier)
    }
}
