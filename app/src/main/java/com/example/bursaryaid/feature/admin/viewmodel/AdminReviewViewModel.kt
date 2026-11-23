package com.example.bursaryaid.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bursaryaid.feature.admin.data.AdminReviewRepository
import com.example.bursaryaid.feature.admin.logic.AdminApplicationDetail
import com.example.bursaryaid.feature.admin.logic.DecisionEvent
import com.example.bursaryaid.feature.admin.logic.DecisionForm
import com.example.bursaryaid.feature.admin.logic.DecisionResult
import com.example.bursaryaid.feature.admin.logic.DecisionValidator
import com.example.bursaryaid.feature.admin.logic.EligibilityResult
import com.example.bursaryaid.feature.admin.logic.EligibilityRules
import com.example.bursaryaid.feature.admin.logic.ReviewWorkflow
import com.example.bursaryaid.feature.admin.logic.toEligibilityInput
import com.example.bursaryaid.model.ApplicationStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminReviewUiState(
    val detail: AdminApplicationDetail? = null,
    val eligibility: EligibilityResult? = null,
    /** What the administrator has typed. Field keys are in DecisionForm and DecisionValidator. */
    val awardedAmountText: String = "",
    val decisionReason: String = "",
    val informationMessage: String = "",
    val formErrors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isApplicationMissing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canStartReview: Boolean
        get() = detail?.let { ReviewWorkflow.canStartReview(it.applicationStatus) } == true
    val canDecide: Boolean
        get() = detail?.let { ReviewWorkflow.canDecide(it.applicationStatus) } == true
    val canRequestInformation: Boolean
        get() = detail?.let { ReviewWorkflow.canRequestInformation(it.applicationStatus) } == true
    val isFinal: Boolean
        get() = detail?.let { ReviewWorkflow.isFinal(it.applicationStatus) } == true
}

class AdminReviewViewModel(
    private val selectedApplicationId: Long,
    private val reviewedByUserId: Long,
    private val adminReviewRepository: AdminReviewRepository,
    /** Called after a decision is saved, so notifications can react (Member 5). */
    private val onDecisionRecorded: suspend (DecisionEvent) -> Unit = {}
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReviewUiState())
    val uiState: StateFlow<AdminReviewUiState> = _uiState.asStateFlow()

    init {
        loadApplication()
    }

    /** READ: loads one application. Safe to call again as a "retry". */
    fun loadApplication() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                var detail = adminReviewRepository.getApplicationDetail(selectedApplicationId)

                // Opening a newly submitted application starts its review automatically.
                // This takes the admin directly to the Accept / Reject decision controls.
                if (detail?.applicationStatus == ApplicationStatus.SUBMITTED) {
                    val reviewStarted = adminReviewRepository.startReview(selectedApplicationId)
                    if (reviewStarted.isSuccess) {
                        detail = adminReviewRepository.getApplicationDetail(selectedApplicationId)
                    }
                }

                _uiState.update {
                    if (detail == null) {
                        it.copy(isLoading = false, isApplicationMissing = true)
                    } else {
                        it.copy(
                            isLoading = false,
                            isApplicationMissing = false,
                            detail = detail,
                            eligibility = EligibilityRules.check(detail.toEligibilityInput())
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Could not load the application. Please try again.")
                }
            }
        }
    }

    // ----- form editing: typing clears that field's error -----

    fun onAmountChanged(text: String) = _uiState.update {
        it.copy(awardedAmountText = text, formErrors = it.formErrors - DecisionForm.FIELD_AMOUNT)
    }

    fun onReasonChanged(text: String) = _uiState.update {
        it.copy(decisionReason = text, formErrors = it.formErrors - DecisionForm.FIELD_REASON)
    }

    fun onInformationMessageChanged(text: String) = _uiState.update {
        it.copy(informationMessage = text, formErrors = it.formErrors - DecisionValidator.FIELD_MESSAGE)
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }

    // ----- actions -----

    /** SUBMITTED -> UNDER_REVIEW. */
    fun startReview() {
        val detail = _uiState.value.detail ?: return
        if (!ReviewWorkflow.canStartReview(detail.applicationStatus)) return
        runDecision(successMessage = "Review started.", event = null) {
            adminReviewRepository.startReview(detail.applicationId)
        }
    }

    /** UNDER_REVIEW -> APPROVED, with an award. */
    fun approve() {
        val state = _uiState.value
        val detail = state.detail ?: return
        if (!ReviewWorkflow.canDecide(detail.applicationStatus)) return

        val check = DecisionValidator.validateApproval(detail, state.awardedAmountText)
        val amount = check.awardedAmountKes
        if (!check.isValid || amount == null) {
            _uiState.update { it.copy(formErrors = check.formErrors) }
            return
        }

        val remarks = state.decisionReason.trim().ifEmpty { DecisionValidator.DEFAULT_APPROVAL_REMARKS }
        runDecision(
            successMessage = "Award approved.",
            event = DecisionEvent(detail.applicationId, detail.studentId, ApplicationStatus.APPROVED, amount, remarks)
        ) {
            adminReviewRepository.approveApplication(detail.applicationId, amount, remarks, reviewedByUserId)
        }
    }

    /** UNDER_REVIEW -> REJECTED. A reason is required. */
    fun reject() {
        val state = _uiState.value
        val detail = state.detail ?: return
        if (!ReviewWorkflow.canDecide(detail.applicationStatus)) return

        val errors = DecisionValidator.validateReason(state.decisionReason, DecisionForm.FIELD_REASON)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(formErrors = errors) }
            return
        }

        val reason = state.decisionReason.trim()
        runDecision(
            successMessage = "Application rejected.",
            event = DecisionEvent(detail.applicationId, detail.studentId, ApplicationStatus.REJECTED, 0L, reason)
        ) {
            adminReviewRepository.rejectApplication(detail.applicationId, reason, reviewedByUserId)
        }
    }

    /** UNDER_REVIEW -> NEEDS_INFORMATION. The message goes to the student. */
    fun requestInformation() {
        val state = _uiState.value
        val detail = state.detail ?: return
        if (!ReviewWorkflow.canRequestInformation(detail.applicationStatus)) return

        val errors = DecisionValidator.validateReason(state.informationMessage, DecisionValidator.FIELD_MESSAGE)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(formErrors = errors) }
            return
        }

        val message = state.informationMessage.trim()
        runDecision(
            successMessage = "Information requested from the student.",
            event = DecisionEvent(detail.applicationId, detail.studentId, ApplicationStatus.NEEDS_INFORMATION, 0L, message)
        ) {
            adminReviewRepository.requestInformation(detail.applicationId, message, reviewedByUserId)
        }
    }

    // ----- shared plumbing -----

    /** Saves, reloads the screen's data, then tells other modules. One save at a time. */
    private fun runDecision(
        successMessage: String,
        event: DecisionEvent?,
        action: suspend () -> DecisionResult
    ) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            try {
                val result = action()
                if (result.isSuccess) {
                    refreshDetail()
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            successMessage = successMessage,
                            awardedAmountText = "",
                            decisionReason = "",
                            informationMessage = "",
                            formErrors = emptyMap()
                        )
                    }
                    if (event != null) notifyDecision(event)
                } else {
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = result.message ?: "The decision could not be saved.")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Could not save. Please try again.") }
            }
        }
    }

    /** Reloads after a decision. If this fails, the saved decision stays saved. */
    private suspend fun refreshDetail() {
        try {
            val detail = adminReviewRepository.getApplicationDetail(selectedApplicationId) ?: return
            _uiState.update {
                it.copy(detail = detail, eligibility = EligibilityRules.check(detail.toEligibilityInput()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // keep showing the previous data
        }
    }

    /** The decision is already saved, so a failed notification must never undo it. */
    private suspend fun notifyDecision(event: DecisionEvent) {
        try {
            onDecisionRecorded(event)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // ignored on purpose
        }
    }
}