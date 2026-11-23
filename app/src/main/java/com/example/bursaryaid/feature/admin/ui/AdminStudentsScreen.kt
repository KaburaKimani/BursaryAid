package com.example.bursaryaid.feature.admin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.bursaryaid.feature.admin.viewmodel.AdminStudentsViewModel
import com.example.bursaryaid.feature.student.ui.formatKes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentsScreen(
    viewModel: AdminStudentsViewModel,
    onBack: () -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Students (${students.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (students.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No registered students found.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students, key = { it.user.userId }) { item ->
                    val profile = item.profile
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.user.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (item.user.isAccountActive) "Active" else "Inactive",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Email: ${item.user.email} | Phone: ${item.user.phoneNumber.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodyMedium)

                            if (profile != null) {
                                Spacer(Modifier.height(8.dp))
                                Text("Admission No: ${profile.registrationNumber}", style = MaterialTheme.typography.bodySmall)
                                Text("Institution: ${profile.institutionName} | Course: ${profile.courseName}", style = MaterialTheme.typography.bodySmall)
                                Text("Year of Study: Year ${profile.yearOfStudy} | Household Size: ${profile.householdSize}", style = MaterialTheme.typography.bodySmall)
                                Text("Monthly Income: ${formatKes(profile.monthlyHouseholdIncomeKes)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            } else {
                                Spacer(Modifier.height(4.dp))
                                Text("Profile not yet completed by student.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
