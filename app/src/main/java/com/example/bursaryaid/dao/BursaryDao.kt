package com.example.bursaryaid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bursaryaid.model.Bursary
import kotlinx.coroutines.flow.Flow

@Dao
interface BursaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBursary(bursary: Bursary): Long

    @Update
    suspend fun updateBursary(bursary: Bursary)

    @Query("SELECT * FROM bursaries WHERE bursaryId = :bursaryId")
    suspend fun getBursaryById(bursaryId: Long): Bursary?

    @Query("SELECT * FROM bursaries ORDER BY applicationDeadlineMillis ASC")
    fun getAllBursaries(): Flow<List<Bursary>>

    @Query("SELECT * FROM bursaries WHERE isOpen = 1 ORDER BY applicationDeadlineMillis ASC")
    fun getOpenBursaries(): Flow<List<Bursary>>
}