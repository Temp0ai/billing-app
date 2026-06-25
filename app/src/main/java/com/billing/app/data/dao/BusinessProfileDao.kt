package com.billing.app.data.dao

import androidx.room.*
import com.billing.app.data.entity.BusinessProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {

    @Query("SELECT * FROM business_profile WHERE id = 1")
    fun getProfile(): Flow<BusinessProfile?>

    @Query("SELECT * FROM business_profile WHERE id = 1")
    suspend fun getProfileSync(): BusinessProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BusinessProfile)

    @Update
    suspend fun updateProfile(profile: BusinessProfile)

    @Query("UPDATE business_profile SET currentInvoiceNumber = currentInvoiceNumber + 1 WHERE id = 1")
    suspend fun incrementInvoiceNumber()

    @Query("SELECT currentInvoiceNumber FROM business_profile WHERE id = 1")
    suspend fun getCurrentInvoiceNumber(): Int?
}
