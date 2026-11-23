
package com.example.bursaryaid.feature.bursary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.bursaryaid.feature.bursary.viewmodel.BursaryBrowseViewModel
import com.example.bursaryaid.feature.student.ui.formatKes

import com.example.bursaryaid.ui.components.AppButton
import com.example.bursaryaid.ui.components.AppTextField
import com.example.bursaryaid.ui.components.EmptyState
import com.example.bursaryaid.ui.components.ErrorMessage
import com.example.bursaryaid.ui.components.LoadingState

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

    val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Available Bursaries",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Browse open funding opportunities and apply today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        AppTextField(
            value = viewModel.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = "Search bursaries by name or sponsor"
        )

        Spacer(Modifier.height(14.dp))

        when {

            viewModel.isLoading -> {

                LoadingState(
                    modifier = Modifier.weight(1f)
                )
            }

            viewModel.errorMessage != null -> {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {

                    ErrorMessage(viewModel.errorMessage)

                    AppButton(
                        text = "Try again",
                        onClick = viewModel::retryLoading
                    )
                }
            }

            bursaries.isEmpty() -> {

                EmptyState(
                    message = if (viewModel.searchQuery.isBlank()) {
                        "No bursaries are currently available."
                    } else {
                        "No bursaries match your search."
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(
                        bursaries,
                        key = { it.bursaryId }
                    ) { bursary ->

                        Card(
                            shape = RoundedCornerShape(16.dp),

                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 3.dp
                            ),

                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.5f
                                    )
                            ),

                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectBursary(bursary.bursaryId)
                                }
                        ) {

                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = bursary.bursaryName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = bursary.fundingOrganization,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {

                                    Text(
                                        text = "Max Award: ${
                                            formatKes(bursary.maximumAwardKes)
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = if (bursary.isBursaryOpen) {
                                            "Deadline: ${
                                                dateFormat.format(
                                                    Date(bursary.applicationDeadlineMillis)
                                                )
                                            }"
                                        } else {
                                            "Closed"
                                        },

                                        fontWeight = FontWeight.SemiBold,

                                        style = MaterialTheme.typography.bodySmall,

                                        color = if (bursary.isBursaryOpen) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                "Back to Dashboard",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
