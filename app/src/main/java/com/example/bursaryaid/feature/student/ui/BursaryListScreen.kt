package com.example.bursaryaid.feature.student.ui

import androidx.compose.runtime.Composable
import com.example.bursaryaid.feature.bursary.ui.BursaryListScreen
import com.example.bursaryaid.feature.bursary.viewmodel.BursaryBrowseViewModel

@Composable
fun StudentBursaryListScreen(
    viewModel: BursaryBrowseViewModel,
    onSelectBursary: (Long) -> Unit,
    onBack: () -> Unit
) {
    BursaryListScreen(
        viewModel = viewModel,
        onSelectBursary = onSelectBursary,
        onBack = onBack
    )
}
