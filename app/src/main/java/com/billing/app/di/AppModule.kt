package com.billing.app.di

import android.content.Context
import androidx.room.Room
import com.billing.app.data.dao.*
import com.billing.app.data.database.BillingDatabase
import com.billing.app.pdf.PdfGenerator
import com.billing.app.sync.googledrive.GoogleDriveSyncManager
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
    fun provideDatabase(@ApplicationContext context: Context): BillingDatabase {
        return Room.databaseBuilder(
            context,
            BillingDatabase::class.java,
            BillingDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePartyDao(database: BillingDatabase): PartyDao = database.partyDao()

    @Provides
    fun provideProductDao(database: BillingDatabase): ProductDao = database.productDao()

    @Provides
    fun provideInvoiceDao(database: BillingDatabase): InvoiceDao = database.invoiceDao()

    @Provides
    fun provideInvoiceItemDao(database: BillingDatabase): InvoiceItemDao = database.invoiceItemDao()

    @Provides
    fun providePaymentDao(database: BillingDatabase): PaymentDao = database.paymentDao()

    @Provides
    fun provideBusinessProfileDao(database: BillingDatabase): BusinessProfileDao = database.businessProfileDao()

    @Provides
    fun provideExpenseDao(database: BillingDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideSyncLogDao(database: BillingDatabase): SyncLogDao = database.syncLogDao()

    @Provides
    @Singleton
    fun providePdfGenerator(@ApplicationContext context: Context): PdfGenerator {
        return PdfGenerator(context)
    }

    @Provides
    @Singleton
    fun provideGoogleDriveSyncManager(@ApplicationContext context: Context): GoogleDriveSyncManager {
        return GoogleDriveSyncManager(context)
    }
}
