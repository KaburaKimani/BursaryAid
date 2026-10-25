package com.example.bursaryaid.feature.student.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bursaryaid.data.model.ApplicationRepository
import com.example.bursaryaid.data.model.BursaryApplication
import com.example.bursaryaid.data.model.BursaryRepository
import com.example.bursaryaid.data.model.StudentProfileRepository

/**
 * Plain factories so no dependency-injection library has to be agreed first.
 * Member 1 calls these where the screens are created, e.g.
 *   val vm: StudentProfileViewModel = viewModel(key = "profile-$userId", factory = studentProfileViewModelFactory(userId, profileRepo))
 */
fun studentProfileViewModelFactory(
    userId: Long,
    studentProfileRepository: StudentProfileRepository
): ViewModelProvider.Factory = viewModelFactory {
    initializer { StudentProfileViewModel(userId, studentProfileRepository) }
}

fun applicationFormViewModelFactory(
    userId: Long,
    selectedApplicationId: Long?,
    studentProfileRepository: StudentProfileRepository,
    applicationRepository: ApplicationRepository,
    bursaryRepository: BursaryRepository,
    onApplicationSubmitted: suspend (BursaryApplication) -> Unit = {}
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        ApplicationFormViewModel(
            userId, selectedApplicationId, studentProfileRepository,
            applicationRepository, bursaryRepository, onApplicationSubmitted
        )
    }
}

fun myApplicationsViewModelFactory(
    userId: Long,
    studentProfileRepository: StudentProfileRepository,
    applicationRepository: ApplicationRepository,
    bursaryRepository: BursaryRepository
): ViewModelProvider.Factory = viewModelFactory {
    initializer { MyApplicationsViewModel(userId, studentProfileRepository, applicationRepository, bursaryRepository) }
}

fun applicationDetailsViewModelFactory(
    userId: Long,
    selectedApplicationId: Long,
    studentProfileRepository: StudentProfileRepository,
    applicationRepository: ApplicationRepository,
    bursaryRepository: BursaryRepository
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        ApplicationDetailsViewModel(
            userId, selectedApplicationId, studentProfileRepository, applicationRepository, bursaryRepository
        )
    }
}
