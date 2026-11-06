package com.example.bursaryaid.feature.bursary.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.bursary.viewmodel.BursaryBrowseViewModel
import com.example.bursaryaid.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BursaryDetailsScreen(
    viewModel: BursaryBrowseViewModel,
    bursaryId: Long,
    onApply: (Long) -> Unit,
    onBack: () -> Unit
) {
    val bursary = viewModel.getBursaryById(bursaryId)

    if (bursary == null) {
        EmptyState(message = "This bursary could not be found.")
        return
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(bursary.bursaryName, style = MaterialTheme.typography.headlineMedium)
        Text(bursary.fundingOrganization, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(16.dp))

        Text(bursary.bursaryDescription, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))

        Text("Maximum award: KSh ${"%,d".format(bursary.maximumAwardKes)}")
        Text("Total budget: KSh ${"%,d".format(bursary.totalBudgetKes)}")
        Text("Deadline: ${dateFormat.format(Date(bursary.applicationDeadlineMillis))}")
        Text(
            if (bursary.isBursaryOpen) "Status: Open" else "Status: Closed",
            color = if (bursary.isBursaryOpen) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onApply(bursary.bursaryId) },
            enabled = bursary.isBursaryOpen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (bursary.isBursaryOpen) "Apply for this bursary" else "Applications closed")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}