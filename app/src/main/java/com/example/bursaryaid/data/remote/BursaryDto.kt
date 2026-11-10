package com.example.bursaryaid.data.remote

import com.squareup.moshi.Json

data class BursaryDto(
    @Json(name = "bursary_id")            val bursaryId: Long,
    @Json(name = "bursary_name")          val bursaryName: String,
    @Json(name = "bursary_description")   val bursaryDescription: String,
    @Json(name = "funding_organization")  val fundingOrganization: String,
    @Json(name = "total_budget_kes")      val totalBudgetKes: Long,
    @Json(name = "application_deadline")  val applicationDeadlineMillis: Long,
    @Json(name = "is_bursary_open")       val isBursaryOpen: Boolean
)