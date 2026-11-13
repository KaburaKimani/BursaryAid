package com.example.bursaryaid.repository

import com.example.bursaryaid.dao.StudentProfileDao
import com.example.bursaryaid.model.StudentProfile
import kotlinx.coroutines.flow.Flow

class StudentProfileRepository(private val studentProfileDao: StudentProfileDao) {
    suspend fun insertOrUpdateProfile(profile: StudentProfile): Long =
        studentProfileDao.insertOrUpdateProfile(profile)

    suspend fun updateProfile(profile: StudentProfile) =
        studentProfileDao.updateProfile(profile)

    suspend fun getProfileById(studentId: Long): StudentProfile? =
        studentProfileDao.getProfileById(studentId)

    suspend fun getProfileByUserId(userId: Long): StudentProfile? =
        studentProfileDao.getProfileByUserId(userId)

    fun getProfileByUserIdFlow(userId: Long): Flow<StudentProfile?> =
        studentProfileDao.getProfileByUserIdFlow(userId)

    fun getAllProfiles(): Flow<List<StudentProfile>> =
        studentProfileDao.getAllProfiles()
}