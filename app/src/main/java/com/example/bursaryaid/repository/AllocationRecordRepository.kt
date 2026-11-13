package com.example.bursaryaid.repository

import com.example.bursaryaid.dao.AllocationRecordDao
import com.example.bursaryaid.model.AllocationRecord
import kotlinx.coroutines.flow.Flow

class AllocationRecordRepository(private val allocationRecordDao: AllocationRecordDao) {
    suspend fun insertAllocation(allocation: AllocationRecord): Long =
        allocationRecordDao.insertAllocation(allocation)

    suspend fun updateAllocation(allocation: AllocationRecord) =
        allocationRecordDao.updateAllocation(allocation)

    suspend fun getAllocationById(allocationId: Long): AllocationRecord? =
        allocationRecordDao.getAllocationById(allocationId)

    fun getAllocationsByStudent(studentId: Long): Flow<List<AllocationRecord>> =
        allocationRecordDao.getAllocationsByStudent(studentId)

    fun getAllocationsByBursary(bursaryId: Long): Flow<List<AllocationRecord>> =
        allocationRecordDao.getAllocationsByBursary(bursaryId)

    fun getAllAllocations(): Flow<List<AllocationRecord>> =
        allocationRecordDao.getAllAllocations()
}