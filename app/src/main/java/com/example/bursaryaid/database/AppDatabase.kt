package com.example.bursaryaid.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bursaryaid.dao.AllocationRecordDao
import com.example.bursaryaid.dao.AppNotificationDao
import com.example.bursaryaid.dao.BursaryApplicationDao
import com.example.bursaryaid.dao.BursaryDao
import com.example.bursaryaid.dao.StudentProfileDao
import com.example.bursaryaid.dao.UserDao
import com.example.bursaryaid.model.AllocationRecord
import com.example.bursaryaid.model.AppConstants
import com.example.bursaryaid.model.AppNotification
import com.example.bursaryaid.model.Bursary
import com.example.bursaryaid.model.BursaryApplication
import com.example.bursaryaid.model.Converters
import com.example.bursaryaid.model.StudentProfile
import com.example.bursaryaid.model.UserAccount

@Database(
    entities = [
        UserAccount::class,
        StudentProfile::class,
        Bursary::class,
        BursaryApplication::class,
        AllocationRecord::class,
        AppNotification::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun bursaryDao(): BursaryDao
    abstract fun bursaryApplicationDao(): BursaryApplicationDao
    abstract fun allocationRecordDao(): AllocationRecordDao
    abstract fun appNotificationDao(): AppNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    AppConstants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}