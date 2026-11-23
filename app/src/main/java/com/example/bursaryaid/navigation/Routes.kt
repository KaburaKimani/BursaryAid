
package com.example.bursaryaid.navigation

object Routes {

    // Authentication
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Dashboards
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val STUDENT_DASHBOARD = "student_dashboard"

    // Student navigation
    const val MY_APPLICATIONS = "my_applications"
    const val PROFILE = "profile"
    const val BURSARY_LIST = "bursary_list"

    // Administrator navigation
    const val ADMIN_APPLICATIONS = "admin_applications"
    const val ADMIN_REVIEW_PATTERN = "admin_review/{appId}"
    const val ADMIN_CREATE_BURSARY = "admin_create_bursary"
    const val ADMIN_BURSARY_OVERVIEW = "admin_bursary_overview"
    const val ADMIN_BURSARY_APPLICANTS_PATTERN = "admin_bursary_applicants/{bursaryId}"
    const val ADMIN_PROFILE = "admin_profile"

    // Navigation arguments
    const val ARG_BURSARY_ID = "bursaryId"
    const val ARG_APP_ID = "appId"

    // Route patterns
    const val FORM_PATTERN =
        "form?bursaryId={bursaryId}&appId={appId}"

    const val DETAILS_PATTERN =
        "details/{appId}"

    const val BURSARY_DETAILS_PATTERN =
        "bursary_details/{bursaryId}"

    // Student application form
    fun form(
        bursaryId: Long,
        appId: Long? = null
    ): String =
        "form?bursaryId=$bursaryId" +
                (appId?.let { "&appId=$it" } ?: "")

    // Student application details
    fun details(appId: Long): String =
        "details/$appId"

    // Bursary details
    fun bursaryDetails(bursaryId: Long): String =
        "bursary_details/$bursaryId"

    // Administrator application review
    fun adminReview(applicationId: Long): String =
        "admin_review/$applicationId"

    // Administrator bursary applicants drill-down
    fun adminBursaryApplicants(bursaryId: Long): String =
        "admin_bursary_applicants/$bursaryId"
}
