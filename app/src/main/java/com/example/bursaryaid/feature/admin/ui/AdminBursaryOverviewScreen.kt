package com.example.bursaryaid.feature.admin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bursaryaid.feature.admin.viewmodel.AdminBursaryOverviewViewModel
import com.example.bursaryaid.feature.student.ui.formatKes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBursaryOverviewScreen(
    viewModel: AdminBursaryOverviewViewModel,
    onSelectBursary: (bursaryId: Long) -> Unit,
    onCreateBursary: () -> Unit,
    onBack: () -> Unit
) {
    val overviews by viewModel.bursaryOverviews.collectAsStateWithLifecycle()
    val totalApplicants = overviews.sumOf { it.applicantCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bursary management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateBursary) {
                Icon(Icons.Default.Add, contentDescription = "Create bursary")
            }
        }
    ) { padding ->
        if (overviews.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No bursary opportunities yet.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Create a bursary to publish an opportunity and start receiving applications.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ADMIN OVERVIEW", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("${overviews.size} bursaries", style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("${totalApplicants} applications received",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Icon(Icons.Default.Groups, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                item {
                    Text("Published opportunities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Select a bursary to review its applicants and make decisions.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(overviews, key = { it.bursary.bursaryId }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectBursary(item.bursary.bursaryId) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.bursary.bursaryName, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(end = 8.dp))
                                Text(if (item.bursary.isBursaryOpen) "OPEN" else "CLOSED",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (item.bursary.isBursaryOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold)
                            }
                            Text("Sponsor: ${item.bursary.fundingOrganization}", style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Applicants", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(item.applicantCount.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total budget", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatKes(item.bursary.totalBudgetKes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SummaryCount("Pending", item.pendingCount, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                                SummaryCount("Approved", item.approvedCount, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                                SummaryCount("Rejected", item.rejectedCount, MaterialTheme.colorScheme.error, Modifier.weight(1f))
                            }
                            Text("View applicants  →", color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCount(label: String, count: Int, tint: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(count.toString(), color = tint, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}