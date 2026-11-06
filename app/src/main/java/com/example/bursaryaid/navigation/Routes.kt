package com.example.bursaryaid.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val STUDENT_DASHBOARD = "student_dashboard"
    const val MY_APPLICATIONS = "my_applications"
    const val PROFILE = "profile"
    const val BURSARY_LIST = "bursary_list"

    const val ARG_BURSARY_ID = "bursaryId"
    const val ARG_APP_ID = "appId"

    const val FORM_PATTERN = "form?bursaryId={bursaryId}&appId={appId}"
    const val DETAILS_PATTERN = "details/{appId}"
    const val BURSARY_DETAILS_PATTERN = "bursary_details/{bursaryId}"

    fun form(bursaryId: Long, appId: Long? = null): String =
        "form?bursaryId=$bursaryId" + (appId?.let { "&appId=$it" } ?: "")

    fun details(appId: Long): String = "details/$appId"

    fun bursaryDetails(bursaryId: Long): String = "bursary_details/$bursaryId"
}