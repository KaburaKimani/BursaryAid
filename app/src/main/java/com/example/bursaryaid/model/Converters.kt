package com.example.bursaryaid.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = enumValueOf(value)

    @TypeConverter
    fun fromApplicationStatus(value: ApplicationStatus): String = value.name

    @TypeConverter
    fun toApplicationStatus(value: String): ApplicationStatus = enumValueOf(value)

    @TypeConverter
    fun fromDecisionStatus(value: DecisionStatus): String = value.name

    @TypeConverter
    fun toDecisionStatus(value: String): DecisionStatus = enumValueOf(value)

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = enumValueOf(value)
}