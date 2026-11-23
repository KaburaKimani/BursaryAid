package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bursaries")
data class Bursary(
    @PrimaryKey(autoGenerate = true)
    val bursaryId: Long = 0L,
    val bursaryName: String,
    val bursaryDescription: String,
    val fundingOrganization: String = "",
    val totalBudgetKes: Long = 0L,
    val minimumIncomeKes: Long = 0L,
    val maximumAwardKes: Long = 0L,
    val applicationDeadlineMillis: Long,
    val isBursaryOpen: Boolean = true
) {
    /**
     * A bursary is ACTIVE only while it is switched on AND its deadline has not passed.
     * After the deadline it is treated as closed everywhere (student and admin).
     */
    fun isAcceptingApplications(nowMillis: Long = System.currentTimeMillis()): Boolean =
        isBursaryOpen && applicationDeadlineMillis >= nowMillis
}