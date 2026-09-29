package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "services")
data class ServiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val govtFee: Double,
    val entrepreneurFee: Double,
    val isDefault: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val citizenName: String,
    val citizenPhone: String,
    val serviceName: String,
    val govtFee: Double,
    val entrepreneurFee: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY isDefault DESC, name ASC")
    fun getAllServices(): Flow<List<ServiceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceItem)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteServiceById(id: Int)

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getCount(): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionItem>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startOfToday ORDER BY timestamp DESC")
    fun getTodayTransactions(startOfToday: Long): Flow<List<TransactionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionItem)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)
}

@Database(entities = [ServiceItem::class, TransactionItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "up_hisab_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class AppRepository(private val database: AppDatabase) {
    val allServices: Flow<List<ServiceItem>> = database.serviceDao().getAllServices()
    val allTransactions: Flow<List<TransactionItem>> = database.transactionDao().getAllTransactions()

    fun getTodayTransactions(startOfToday: Long): Flow<List<TransactionItem>> {
        return database.transactionDao().getTodayTransactions(startOfToday)
    }

    suspend fun insertTransaction(transaction: TransactionItem) {
        database.transactionDao().insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Int) {
        database.transactionDao().deleteTransactionById(id)
    }

    suspend fun insertService(service: ServiceItem) {
        database.serviceDao().insertService(service)
    }

    suspend fun deleteService(id: Int) {
        database.serviceDao().deleteServiceById(id)
    }

    suspend fun ensureDefaultServices() {
        if (database.serviceDao().getCount() == 0) {
            val defaultServices = listOf(
                ServiceItem(name = "জন্ম নিবন্ধন সনদ (Birth Registration)", govtFee = 50.0, entrepreneurFee = 30.0, isDefault = true),
                ServiceItem(name = "মৃত্যু নিবন্ধন সনদ (Death Registration)", govtFee = 50.0, entrepreneurFee = 30.0, isDefault = true),
                ServiceItem(name = "নাগরিকত্ব সনদপত্র (Citizen Certificate)", govtFee = 20.0, entrepreneurFee = 20.0, isDefault = true),
                ServiceItem(name = "চারিত্রিক সনদপত্র (Character Certificate)", govtFee = 20.0, entrepreneurFee = 20.0, isDefault = true),
                ServiceItem(name = "জাতীয় পরিচয়পত্র সংশোধন (NID Correction)", govtFee = 230.0, entrepreneurFee = 100.0, isDefault = true),
                ServiceItem(name = "খতিয়ান/পর্চা আবেদন (Khatian/Porcha)", govtFee = 100.0, entrepreneurFee = 50.0, isDefault = true),
                ServiceItem(name = "অনলাইন বিদ্যুৎ বিল পরিশোধ (Electricity Bill)", govtFee = 0.0, entrepreneurFee = 20.0, isDefault = true),
                ServiceItem(name = "সরকারি চাকরির আবেদন (Govt Job Application)", govtFee = 0.0, entrepreneurFee = 100.0, isDefault = true),
                ServiceItem(name = "অন্যান্য অনলাইন আবেদন (Other Online Service)", govtFee = 0.0, entrepreneurFee = 50.0, isDefault = true)
            )
            for (service in defaultServices) {
                database.serviceDao().insertService(service)
            }
        }
    }
}
