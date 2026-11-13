package com.example.bursaryaid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bursaryaid.model.StudentProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StudentProfile): Long

    @Update
    suspend fun updateProfile(profile: StudentProfile)

    @Query("SELECT * FROM student_profiles WHERE studentId = :studentId")
    suspend fun getProfileById(studentId: Long): StudentProfile?

    @Query("SELECT * FROM student_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfileByUserId(userId: Long): StudentProfile?

    @Query("SELECT * FROM student_profiles WHERE userId = :userId LIMIT 1")
    fun getProfileByUserIdFlow(userId: Long): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profiles")
    fun getAllProfiles(): Flow<List<StudentProfile>>
}