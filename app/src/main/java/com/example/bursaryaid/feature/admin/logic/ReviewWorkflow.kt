package com.example.bursaryaid.feature.admin.logic

import com.example.bursaryaid.model.ApplicationStatus

/**
 * Which status changes the administrator may make.
 * Decisions are final: an APPROVED or REJECTED application never changes again.
 */
object ReviewWorkflow {

    fun canStartReview(status: ApplicationStatus): Boolean =
        status == ApplicationStatus.SUBMITTED

    fun canDecide(status: ApplicationStatus): Boolean =
        status == ApplicationStatus.UNDER_REVIEW

    fun canRequestInformation(status: ApplicationStatus): Boolean =
        status == ApplicationStatus.UNDER_REVIEW

    fun isFinal(status: ApplicationStatus): Boolean =
        status == ApplicationStatus.APPROVED || status == ApplicationStatus.REJECTED
}