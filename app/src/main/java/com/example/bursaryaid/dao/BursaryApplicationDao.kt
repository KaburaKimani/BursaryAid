package com.example.bursaryaid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.BursaryApplication
import kotlinx.coroutines.flow.Flow

@Dao
interface BursaryApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: BursaryApplication): Long

    @Update
    suspend fun updateApplication(application: BursaryApplication)

    @Query("SELECT * FROM bursary_applications WHERE applicationId = :applicationId")
    suspend fun getApplicationById(applicationId: Long): BursaryApplication?

    @Query("SELECT * FROM bursary_applications WHERE studentId = :studentId ORDER BY submittedAtMillis DESC")
    fun getApplicationsByStudent(studentId: Long): Flow<List<BursaryApplication>>

    @Query("SELECT * FROM bursary_applications WHERE bursaryId = :bursaryId ORDER BY submittedAtMillis DESC")
    fun getApplicationsByBursary(bursaryId: Long): Flow<List<BursaryApplication>>

    @Query("SELECT * FROM bursary_applications ORDER BY submittedAtMillis DESC")
    fun getAllApplications(): Flow<List<BursaryApplication>>

    @Query("SELECT * FROM bursary_applications WHERE applicationStatus = :status")
    fun getApplicationsByStatus(status: ApplicationStatus): Flow<List<BursaryApplication>>
}