package com.example.bursaryaid.feature.student.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bursaryaid.feature.student.logic.FormField
import com.example.bursaryaid.feature.student.viewmodel.StudentProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel,
    onProfileSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onProfileSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My student profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.errorMessage?.let { StudentMessageBanner(it, isError = true) }
            state.successMessage?.let { StudentMessageBanner(it, isError = false) }

            Text("Education", style = MaterialTheme.typography.titleMedium)
            StudentFormTextField(state.registrationNumber, viewModel::onRegistrationNumberChanged,
                "Registration number", state.formErrors[FormField.REGISTRATION_NUMBER], enabled = !state.isLoading)
            StudentFormTextField(state.institutionName, viewModel::onInstitutionNameChanged,
                "Institution", state.formErrors[FormField.INSTITUTION_NAME], enabled = !state.isLoading)
            StudentFormTextField(state.courseName, viewModel::onCourseNameChanged,
                "Course / programme", state.formErrors[FormField.COURSE_NAME], enabled = !state.isLoading)
            StudentFormTextField(state.yearOfStudyInput, viewModel::onYearOfStudyChanged,
                "Year of study", state.formErrors[FormField.YEAR_OF_STUDY],
                keyboardType = KeyboardType.Number, enabled = !state.isLoading)

            Spacer(Modifier.height(8.dp))
            Text("Household", style = MaterialTheme.typography.titleMedium)
            StudentFormTextField(state.householdSizeInput, viewModel::onHouseholdSizeChanged,
                "Number of people in household", state.formErrors[FormField.HOUSEHOLD_SIZE],
                keyboardType = KeyboardType.Number, enabled = !state.isLoading)
            StudentFormTextField(state.monthlyHouseholdIncomeKesInput, viewModel::onMonthlyHouseholdIncomeKesChanged,
                "Monthly household income (KES)", state.formErrors[FormField.MONTHLY_HOUSEHOLD_INCOME_KES],
                keyboardType = KeyboardType.Number, enabled = !state.isLoading)

            Spacer(Modifier.height(8.dp))
            Text("Guardian (optional)", style = MaterialTheme.typography.titleMedium)
            StudentFormTextField(state.guardianName, viewModel::onGuardianNameChanged,
                "Guardian's name", state.formErrors[FormField.GUARDIAN_NAME], enabled = !state.isLoading)
            StudentFormTextField(state.guardianPhoneNumber, viewModel::onGuardianPhoneNumberChanged,
                "Guardian's phone number", state.formErrors[FormField.GUARDIAN_PHONE_NUMBER],
                keyboardType = KeyboardType.Phone, enabled = !state.isLoading,
                helperText = "Both guardian fields are needed if you fill in either.")

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = viewModel::saveStudentProfile,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (state.hasExistingProfile) "Update profile" else "Save profile") }
        }
    }
}
