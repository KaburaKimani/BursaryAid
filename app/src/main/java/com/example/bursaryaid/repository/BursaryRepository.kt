package com.example.bursaryaid.repository

import com.example.bursaryaid.dao.BursaryDao
import com.example.bursaryaid.model.Bursary
import kotlinx.coroutines.flow.Flow

class BursaryRepository(private val bursaryDao: BursaryDao) {
    suspend fun insertBursary(bursary: Bursary): Long =
        bursaryDao.insertBursary(bursary)

    suspend fun updateBursary(bursary: Bursary) =
        bursaryDao.updateBursary(bursary)

    suspend fun getBursaryById(bursaryId: Long): Bursary? =
        bursaryDao.getBursaryById(bursaryId)

    fun getAllBursaries(): Flow<List<Bursary>> =
        bursaryDao.getAllBursaries()

    fun getOpenBursaries(): Flow<List<Bursary>> =
        bursaryDao.getOpenBursaries()
}