
package com.example.bursaryaid.database

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 3,
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

        // Version 1 -> Version 2:
        // Add a unique index to user email.
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_user_accounts_email
                    ON user_accounts(email)
                    """.trimIndent()
                )
            }
        }

        // Version 2 -> Version 3:
        // Store the administrator who made an allocation decision.
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE allocation_records
                    ADD COLUMN reviewedByUserId INTEGER
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    AppConstants.DATABASE_NAME
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3
                    )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
