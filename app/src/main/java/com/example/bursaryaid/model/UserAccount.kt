
package com.example.bursaryaid.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_accounts",
    indices = [
        Index(
            value = ["email"],
            unique = true
        )
    ]
)
data class UserAccount(

    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0L,

    val fullName: String,

    val email: String,

    val phoneNumber: String,

    val passwordHash: String,

    val userRole: UserRole,

    val isAccountActive: Boolean,

    val createdAtMillis: Long
)
