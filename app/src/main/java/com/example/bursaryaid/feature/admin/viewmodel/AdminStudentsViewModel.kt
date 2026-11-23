package com.example.bursaryaid.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.model.StudentProfile
import com.example.bursaryaid.model.UserAccount
import com.example.bursaryaid.model.UserRole
import com.example.bursaryaid.repository.StudentProfileRepository
import com.example.bursaryaid.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StudentTrackingItem(
    val user: UserAccount,
    val profile: StudentProfile?
)

class AdminStudentsViewModel(
    userRepository: UserRepository,
    studentProfileRepository: StudentProfileRepository
) : ViewModel() {

    val students: StateFlow<List<StudentTrackingItem>> =
        combine(
            userRepository.getUsersByRole(UserRole.STUDENT),
            studentProfileRepository.getAllProfiles()
        ) { users, profiles ->
            val profileMap = profiles.associateBy { it.userId }
            users.map { user ->
                StudentTrackingItem(
                    user = user,
                    profile = profileMap[user.userId]
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}

fun adminStudentsViewModelFactory(
    userRepository: UserRepository,
    studentProfileRepository: StudentProfileRepository
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminStudentsViewModel::class.java)) {
            return AdminStudentsViewModel(userRepository, studentProfileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
