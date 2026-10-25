package com.example.bursaryaid.feature.student.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.data.model.ApplicationRepository
import com.example.bursaryaid.data.model.ApplicationStatus
import com.example.bursaryaid.data.model.BursaryApplication
import com.example.bursaryaid.data.model.BursaryOpportunity
import com.example.bursaryaid.data.model.BursaryRepository
import com.example.bursaryaid.data.model.StudentProfileRepository
import com.example.bursaryaid.feature.student.logic.ApplicationRules
import com.example.bursaryaid.feature.student.logic.FormField
import com.example.bursaryaid.feature.student.logic.StudentFeatureConstants
import com.example.bursaryaid.feature.student.logic.StudentFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApplicationFormUiState(
    /** null = creating a new application; non-null = editing that application. */
    val selectedApplicationId: Long? = null,
    val studentId: Long? = null,
    val bursaryId: Long? = null,
    val bursaries: List<BursaryOpportunity> = emptyList(),
    val academicYear: String = "",
    val requestedAmountKesInput: String = "",
    val applicationReason: String = "",
    val supportingDocuments: List<String> = emptyList(),
    val applicationStatus: ApplicationStatus = ApplicationStatus.DRAFT,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val formErrors: Map<String, String> = emptyMap(),
    /** True when the student has no profile yet; the screen then sends them to the profile form. */
    val isProfileMissing: Boolean = false,
    /** One-shot signal: a save/submit succeeded and the screen may navigate away. */
    val isSaved: Boolean = false
)

/**
 * Create / edit / submit workflow for one bursary application.
 *
 * @param onApplicationSubmitted hook invoked after a successful submission. Member 5 (notifications)
 *   or Member 1 (wiring) supplies it, e.g. to call createNotification() with
 *   NotificationType.APPLICATION_SUBMITTED. Defaults to a no-op so this module has no hard dependency.
 *   A failure inside the hook is swallowed: the submission itself has already been saved.
 */
class ApplicationFormViewModel(
    private val userId: Long,
    selectedApplicationId: Long?,
    private val studentProfileRepository: StudentProfileRepository,
    private val applicationRepository: ApplicationRepository,
    private val bursaryRepository: BursaryRepository,
    private val onApplicationSubmitted: suspend (BursaryApplication) -> Unit = {},
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationFormUiState(selectedApplicationId = selectedApplicationId))
    val uiState: StateFlow<ApplicationFormUiState> = _uiState.asStateFlow()

    init {
        loadFormData()
    }

    // ---------------------------------------------------------------- loading

    /** Loads the student's id, the open bursaries and (when editing) the existing application. */
    fun loadFormData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isProfileMissing = false) }
            try {
                val profile = studentProfileRepository.getStudentProfile(userId)
                if (profile == null) {
                    _uiState.update { it.copy(isLoading = false, isProfileMissing = true) }
                    return@launch
                }
                val bursaries = bursaryRepository.getOpenBursaries().first()
                val editingId = _uiState.value.selectedApplicationId
                if (editingId == null) {
                    _uiState.update { it.copy(studentId = profile.studentId, bursaries = bursaries, isLoading = false) }
                } else {
                    val existing = getApplicationById(editingId)
                    when {
                        existing == null || existing.studentId != profile.studentId ->
                            _uiState.update { it.copy(isLoading = false, errorMessage = "This application could not be found.") }
                        !ApplicationRules.canEdit(existing.applicationStatus) ->
                            _uiState.update { it.copy(isLoading = false, errorMessage = "This application can no longer be edited.") }
                        else -> _uiState.update {
                            it.copy(
                                studentId = profile.studentId,
                                bursaries = bursaries,
                                bursaryId = existing.bursaryId,
                                academicYear = existing.academicYear,
                                requestedAmountKesInput = if (existing.requestedAmountKes > 0L) existing.requestedAmountKes.toString() else "",
                                applicationReason = existing.applicationReason,
                                supportingDocuments = existing.supportingDocuments,
                                applicationStatus = existing.applicationStatus,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load the form. Check your connection to the device storage and try again.") }
            }
        }
    }

    /** READ one application. Returns null when it does not exist. */
    suspend fun getApplicationById(applicationId: Long): BursaryApplication? =
        applicationRepository.getApplicationById(applicationId)

    // ------------------------------------------------------------- validation

    /** Strict (submission) validation. Updates formErrors and returns true when the form is valid. */
    fun validateApplicationForm(): Boolean = runValidation(requireComplete = true)

    private fun runValidation(requireComplete: Boolean): Boolean {
        val state = _uiState.value
        val formErrors = StudentFormValidator.validateApplicationForm(
            bursaryId = state.bursaryId,
            bursary = state.bursaries.firstOrNull { it.bursaryId == state.bursaryId },
            academicYear = state.academicYear,
            requestedAmountKesInput = state.requestedAmountKesInput,
            applicationReason = state.applicationReason,
            supportingDocuments = state.supportingDocuments,
            nowMillis = currentTimeMillis(),
            requireComplete = requireComplete,
            // Answering an information request stays possible after the bursary closes.
            enforceBursaryOpen = requireComplete && state.applicationStatus != ApplicationStatus.NEEDS_INFORMATION
        )
        _uiState.update {
            it.copy(
                formErrors = formErrors,
                errorMessage = if (formErrors.isEmpty()) null else "Please fix the highlighted fields."
            )
        }
        return formErrors.isEmpty()
    }

    // --------------------------------------------------------------- actions

    /** Saves the form as a DRAFT: creates a new draft, or updates the one being edited. */
    fun saveApplicationDraft() {
        val editingId = _uiState.value.selectedApplicationId
        if (editingId != null) updateApplication(editingId) else saveNew(submit = false)
    }

    /** CREATE a submitted application, or promote the one being edited to SUBMITTED. */
    fun submitApplication() = saveNew(submit = true)

    /**
     * UPDATE an existing editable application without changing its status
     * (DRAFT stays DRAFT, NEEDS_INFORMATION stays NEEDS_INFORMATION).
     * Drafts are validated leniently; NEEDS_INFORMATION applications must stay complete.
     */
    fun updateApplication(applicationId: Long) {
        if (_uiState.value.isLoading) return
        launchOperation {
            val studentId = requireStudentId() ?: return@launchOperation
            val existing = loadEditable(applicationId, studentId) ?: return@launchOperation
            val requireComplete = existing.applicationStatus != ApplicationStatus.DRAFT
            if (!runValidation(requireComplete)) return@launchOperation
            if (isDuplicate(studentId, applicationId)) return@launchOperation

            applicationRepository.updateApplication(
                buildApplication(existing, studentId, existing.applicationStatus, existing.submittedAtMillis)
            )
            _uiState.update { it.copy(successMessage = "Changes saved.", isSaved = true) }
        }
    }

    private fun saveNew(submit: Boolean) {
        if (_uiState.value.isLoading) return
        launchOperation {
            val studentId = requireStudentId() ?: return@launchOperation
            val editingId = _uiState.value.selectedApplicationId
            val existing = editingId?.let { loadEditable(it, studentId) ?: return@launchOperation }

            if (!runValidation(requireComplete = submit)) return@launchOperation
            if (isDuplicate(studentId, editingId)) return@launchOperation

            val now = currentTimeMillis()
            val status = if (submit) ApplicationStatus.SUBMITTED else ApplicationStatus.DRAFT
            val application = buildApplication(existing, studentId, status, if (submit) now else null)

            val saved = if (existing == null) {
                application.copy(applicationId = applicationRepository.insertApplication(application))
            } else {
                applicationRepository.updateApplication(application)
                application
            }
            if (submit) {
                try {
                    onApplicationSubmitted(saved)
                } catch (e: Exception) {
                    e.rethrowIfCancellation() // notification failure must not undo a saved submission
                }
            }
            _uiState.update {
                it.copy(
                    successMessage = if (submit) "Application submitted." else "Draft saved.",
                    isSaved = true
                )
            }
        }
    }

    // ----------------------------------------------------------- field events

    /** The bursary cannot be changed once reviewers have asked for more information. */
    fun onBursarySelected(bursaryId: Long) {
        if (_uiState.value.applicationStatus == ApplicationStatus.NEEDS_INFORMATION) return
        edit(FormField.BURSARY_ID) { it.copy(bursaryId = bursaryId) }
    }
    fun onAcademicYearChanged(value: String) = edit(FormField.ACADEMIC_YEAR) { it.copy(academicYear = value) }
    fun onRequestedAmountKesChanged(value: String) =
        edit(FormField.REQUESTED_AMOUNT_KES) { it.copy(requestedAmountKesInput = value.filter(Char::isDigit)) }
    fun onApplicationReasonChanged(value: String) = edit(FormField.APPLICATION_REASON) {
        it.copy(applicationReason = value.take(StudentFeatureConstants.MAX_APPLICATION_REASON_LENGTH))
    }

    fun onSupportingDocumentAdded(documentUri: String) {
        val current = _uiState.value.supportingDocuments
        when {
            documentUri in current -> Unit
            current.size >= StudentFeatureConstants.MAX_SUPPORTING_DOCUMENTS ->
                _uiState.update { it.copy(formErrors = it.formErrors + (FormField.SUPPORTING_DOCUMENTS to
                    "You can attach at most ${StudentFeatureConstants.MAX_SUPPORTING_DOCUMENTS} documents.")) }
            else -> edit(FormField.SUPPORTING_DOCUMENTS) { it.copy(supportingDocuments = current + documentUri) }
        }
    }

    fun onSupportingDocumentRemoved(documentUri: String) =
        edit(FormField.SUPPORTING_DOCUMENTS) { it.copy(supportingDocuments = it.supportingDocuments - documentUri) }

    fun consumeSaved() = _uiState.update { it.copy(isSaved = false) }

    // --------------------------------------------------------------- helpers

    private fun edit(field: String, change: (ApplicationFormUiState) -> ApplicationFormUiState) {
        _uiState.update { change(it).copy(formErrors = it.formErrors - field, successMessage = null) }
    }

    private fun launchOperation(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                block()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _uiState.update { it.copy(errorMessage = GENERIC_ERROR_MESSAGE) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun requireStudentId(): Long? {
        val studentId = _uiState.value.studentId
        if (studentId == null) {
            _uiState.update { it.copy(errorMessage = "Complete your student profile before applying.", isProfileMissing = true) }
        }
        return studentId
    }

    /** Re-reads the application from storage so stale screens cannot edit a locked record. */
    private suspend fun loadEditable(applicationId: Long, studentId: Long): BursaryApplication? {
        val existing = getApplicationById(applicationId)
        val message = when {
            existing == null || existing.studentId != studentId -> "This application could not be found."
            !ApplicationRules.canEdit(existing.applicationStatus) -> "This application can no longer be edited."
            else -> null
        }
        if (message != null) {
            _uiState.update { it.copy(errorMessage = message) }
            return null
        }
        return existing
    }

    /** Sets a bursary-level error and returns true when the student already applied to this bursary. */
    private suspend fun isDuplicate(studentId: Long, excludeApplicationId: Long?): Boolean {
        val bursaryId = _uiState.value.bursaryId ?: return false
        val existing = applicationRepository.getApplicationsByStudent(studentId).first()
        val duplicate = ApplicationRules.isDuplicate(existing, bursaryId, excludeApplicationId)
        if (duplicate) {
            _uiState.update {
                it.copy(
                    formErrors = it.formErrors + (FormField.BURSARY_ID to "You already have an application for this bursary."),
                    errorMessage = "You already have an application for this bursary."
                )
            }
        }
        return duplicate
    }

    private fun buildApplication(
        existing: BursaryApplication?,
        studentId: Long,
        applicationStatus: ApplicationStatus,
        submittedAtMillis: Long?
    ): BursaryApplication {
        val state = _uiState.value
        return BursaryApplication(
            applicationId = existing?.applicationId ?: 0L,
            studentId = studentId,
            bursaryId = requireNotNull(state.bursaryId),
            academicYear = state.academicYear.trim(),
            requestedAmountKes = state.requestedAmountKesInput.trim().toLongOrNull() ?: 0L,
            applicationStatus = applicationStatus,
            applicationReason = state.applicationReason.trim(),
            supportingDocuments = state.supportingDocuments,
            submittedAtMillis = submittedAtMillis,
            updatedAtMillis = currentTimeMillis()
        )
    }
}
