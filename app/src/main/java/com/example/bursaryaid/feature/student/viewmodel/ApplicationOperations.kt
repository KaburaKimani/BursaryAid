package com.example.bursaryaid.feature.student.viewmodel

import com.example.bursaryaid.data.model.ApplicationRepository
import com.example.bursaryaid.feature.student.logic.ApplicationRules
import kotlinx.coroutines.CancellationException

/** Message shown when a repository call throws. Kept generic: no stack traces for students. */
internal const val GENERIC_ERROR_MESSAGE = "Something went wrong. Please try again."

/** Rethrows cancellation so coroutines cancel correctly; everything else is handled by the caller. */
internal fun Throwable.rethrowIfCancellation() {
    if (this is CancellationException) throw this
}

/**
 * Shared by MyApplicationsViewModel and ApplicationDetailsViewModel so the delete rule
 * exists once. Returns an error message, or null when the draft was deleted.
 */
internal suspend fun deleteDraftApplication(
    repository: ApplicationRepository,
    studentId: Long,
    applicationId: Long
): String? {
    val application = repository.getApplicationById(applicationId)
        ?: return "This application no longer exists."
    if (application.studentId != studentId) return "You can only delete your own applications."
    if (!ApplicationRules.canDelete(application.applicationStatus)) {
        return "Only draft applications can be deleted."
    }
    repository.deleteApplication(applicationId)
    return null
}
