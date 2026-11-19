package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewWorkflowTest {

    @Test
    fun onlySubmittedCanStartReview() {
        assertTrue(ReviewWorkflow.canStartReview(ApplicationStatus.SUBMITTED))
        ApplicationStatus.entries
            .filter { it != ApplicationStatus.SUBMITTED }
            .forEach { assertFalse(ReviewWorkflow.canStartReview(it)) }
    }

    @Test
    fun onlyUnderReviewCanBeDecided() {
        assertTrue(ReviewWorkflow.canDecide(ApplicationStatus.UNDER_REVIEW))
        ApplicationStatus.entries
            .filter { it != ApplicationStatus.UNDER_REVIEW }
            .forEach { assertFalse(ReviewWorkflow.canDecide(it)) }
    }

    @Test
    fun decisionsAreFinal() {
        assertTrue(ReviewWorkflow.isFinal(ApplicationStatus.APPROVED))
        assertTrue(ReviewWorkflow.isFinal(ApplicationStatus.REJECTED))
        assertFalse(ReviewWorkflow.canDecide(ApplicationStatus.APPROVED))
        assertFalse(ReviewWorkflow.canDecide(ApplicationStatus.REJECTED))
    }

    @Test
    fun onlyUnderReviewCanRequestInformation() {
        assertTrue(ReviewWorkflow.canRequestInformation(ApplicationStatus.UNDER_REVIEW))
        assertFalse(ReviewWorkflow.canRequestInformation(ApplicationStatus.SUBMITTED))
    }
}