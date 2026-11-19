package com.example.bursaryaid.feature.admin.data

import com.example.bursaryaid.feature.admin.logic.AdminApplicationItem
import kotlinx.coroutines.flow.Flow

/**
 * What the admin ViewModels need from the data layer. It is an interface of our own,
 * so the ViewModels do not depend on how the shared schema ends up being named.
 * One adapter will implement it over the real repositories.
 */
interface AdminReviewRepository {

    /** Every application as a plain row. Emits again whenever the database changes. */
    fun observeApplications(): Flow<List<AdminApplicationItem>>
}