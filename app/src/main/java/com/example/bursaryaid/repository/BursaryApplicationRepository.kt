package com.example.bursaryaid.repository

import com.example.bursaryaid.dao.BursaryApplicationDao
import com.example.bursaryaid.model.ApplicationStatus
import com.example.bursaryaid.model.BursaryApplication
import kotlinx.coroutines.flow.Flow

class BursaryApplicationRepository(private val bursaryApplicationDao: BursaryApplicationDao) {
    suspend fun insertApplication(application: BursaryApplication): Long =
        bursaryApplicationDao.insertApplication(application)

    suspend fun updateApplication(application: BursaryApplication) =
        bursaryApplicationDao.updateApplication(application)

    suspend fun getApplicationById(applicationId: Long): BursaryApplication? =
        bursaryApplicationDao.getApplicationById(applicationId)

    fun getApplicationsByStudent(studentId: Long): Flow<List<BursaryApplication>> =
        bursaryApplicationDao.getApplicationsByStudent(studentId)

    fun getApplicationsByBursary(bursaryId: Long): Flow<List<BursaryApplication>> =
        bursaryApplicationDao.getApplicationsByBursary(bursaryId)

    fun getAllApplications(): Flow<List<BursaryApplication>> =
        bursaryApplicationDao.getAllApplications()

    fun getApplicationsByStatus(status: ApplicationStatus): Flow<List<BursaryApplication>> =
        bursaryApplicationDao.getApplicationsByStatus(status)
}