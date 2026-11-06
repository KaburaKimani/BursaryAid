package com.example.bursaryaid.feature.bursary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bursaryaid.feature.bursary.viewmodel.BursaryBrowseViewModel
import com.example.bursaryaid.ui.components.AppTextField
import com.example.bursaryaid.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BursaryListScreen(
    viewModel: BursaryBrowseViewModel,
    onSelectBursary: (Long) -> Unit,
    onBack: () -> Unit
) {
    val bursaries = viewModel.filteredBursaries
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Available bursaries", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        AppTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = "Search bursaries"
        )
        Spacer(Modifier.height(12.dp))

        if (bursaries.isEmpty()) {
            EmptyState(
                message = "No bursaries match your search.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bursaries, key = { it.bursaryId }) { bursary ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBursary(bursary.bursaryId) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(bursary.bursaryName, style = MaterialTheme.typography.titleMedium)
                            Text(bursary.fundingOrganization, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Text("Up to KSh ${"%,d".format(bursary.maximumAwardKes)}")
                            Text(
                                if (bursary.isBursaryOpen)
                                    "Deadline: ${dateFormat.format(Date(bursary.applicationDeadlineMillis))}"
                                else "Closed",
                                color = if (bursary.isBursaryOpen) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}