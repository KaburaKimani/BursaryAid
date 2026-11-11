package com.example.bursaryaid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bursaryaid.model.AllocationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AllocationRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: AllocationRecord): Long

    @Update
    suspend fun updateAllocation(allocation: AllocationRecord)

    @Query("SELECT * FROM allocation_records WHERE allocationId = :allocationId")
    suspend fun getAllocationById(allocationId: Long): AllocationRecord?

    @Query("SELECT * FROM allocation_records WHERE studentId = :studentId ORDER BY allocatedAtMillis DESC")
    fun getAllocationsByStudent(studentId: Long): Flow<List<AllocationRecord>>

    @Query("SELECT * FROM allocation_records WHERE bursaryId = :bursaryId ORDER BY allocatedAtMillis DESC")
    fun getAllocationsByBursary(bursaryId: Long): Flow<List<AllocationRecord>>

    @Query("SELECT * FROM allocation_records ORDER BY allocatedAtMillis DESC")
    fun getAllAllocations(): Flow<List<AllocationRecord>>
}