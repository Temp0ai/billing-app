package com.billing.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.billing.app.data.dao.*
import com.billing.app.data.entity.*

@Database(
    entities = [
        Party::class,
        Product::class,
        Invoice::class,
        InvoiceItem::class,
        Payment::class,
        BusinessProfile::class,
        Expense::class,
        SyncLog::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BillingDatabase : RoomDatabase() {
    abstract fun partyDao(): PartyDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun paymentDao(): PaymentDao
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun syncLogDao(): SyncLogDao

    companion object {
        const val DATABASE_NAME = "billing_database"
    }
}
