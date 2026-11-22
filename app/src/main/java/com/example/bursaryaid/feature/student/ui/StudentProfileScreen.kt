package com.example.bursaryaid.feature.student.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bursaryaid.feature.student.logic.FormField
import com.example.bursaryaid.feature.student.viewmodel.StudentProfileViewModel
import com.example.bursaryaid.ui.theme.DarkPurpleBg
import com.example.bursaryaid.ui.theme.DarkPurpleSurface
import com.example.bursaryaid.ui.theme.DisabledPurple
import com.example.bursaryaid.ui.theme.IndigoPurple
import com.example.bursaryaid.ui.theme.LightLavender
import com.example.bursaryaid.ui.theme.PrimaryPurple

// Form Field Icons
private val BadgeIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Badge", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = SolidColor(PrimaryPurple)) {
            moveTo(19f, 3f); lineTo(5f, 3f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); lineToRelative(0f, 14f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); lineToRelative(14f, 0f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); lineTo(21f, 5f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
            moveTo(12f, 6f); curveToRelative(1.66f, 0f, 3f, 1.34f, 3f, 3f); curveToRelative(0f, 1.66f, -1.34f, 3f, -3f, 3f); curveToRelative(-1.66f, 0f, -3f, -1.34f, -3f, -3f); curveToRelative(0f, -1.66f, 1.34f, -3f, 3f, -3f); close()
            moveTo(18f, 17f); lineTo(6f, 17f); lineToRelative(0f, -0.5f); curveToRelative(0f, -2f, 4f, -3.1f, 6f, -3.1f); curveToRelative(2f, 0f, 6f, 1.1f, 6f, 3.1f); lineToRelative(0f, 0.5f); close()
        }.build()
}

private val SchoolIcon: ImageVector by lazy {
    ImageVector.Builder(name = "School", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = SolidColor(PrimaryPurple)) {
            moveTo(5f, 13.18f); lineTo(5f, 17f); curveToRelative(0f, 2.21f, 3.13f, 4f, 7f, 4f); curveToRelative(3.87f, 0f, 7f, -1.79f, 7f, -4f); lineToRelative(0f, -3.82f); lineToRelative(-7f, 3.82f); lineToRelative(-7f, -3.82f); close()
            moveTo(12f, 3f); lineTo(1f, 9f); lineToRelative(11f, 6f); lineToRelative(9f, -4.91f); lineTo(21f, 17f); lineToRelative(2f, 0f); lineTo(23f, 9f); lineToRelative(-11f, -6f); close()
        }.build()
}

private val BookIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Book", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = SolidColor(PrimaryPurple)) {
            moveTo(18f, 2f); lineTo(6f, 2f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); lineToRelative(0f, 16f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); lineToRelative(12f, 0f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); lineTo(20f, 4f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
            moveTo(6f, 4f); lineToRelative(5f, 0f); lineToRelative(0f, 8f); lineToRelative(-2.5f, -1.5f); lineTo(6f, 12f); lineTo(6f, 4f); close()
        }.build()
}

private val CalendarIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Calendar", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = SolidColor(PrimaryPurple)) {
            moveTo(19f, 4f); lineTo(18f, 4f); lineTo(18f, 2f); lineTo(16f, 2f); lineTo(16f, 4f); lineTo(8f, 4f); lineTo(8f, 2f); lineTo(6f, 2f); lineTo(6f, 4f); lineTo(5f, 4f); curveToRelative(-1.11f, 0f, -1.99f, 0.9f, -1.99f, 2f); lineTo(3f, 20f); curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f); lineToRelative(14f, 0f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); lineTo(21f, 6f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
            moveTo(19f, 20f); lineTo(5f, 20f); lineTo(5f, 9f); lineToRelative(14f, 0f); lineToRelative(0f, 11f); close()
        }.build()
}

private val PeopleIcon: ImageVector by lazy {
    ImageVector.Builder(name = "People", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = SolidColor(PrimaryPurple)) {
            moveTo(16f, 11f); curveToRelative(1.66f, 0f, 2.99f, -1.34f, 2.99f, -3f); curveToRelative(0f, -1.66f, -1.33f, -3f, -2.99f, -3f); curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f); curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f); close()
            moveTo(8f, 11f); curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f); curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f); curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f); curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f); close()
            moveTo(8f, 13f); curveToRelative(-2.67f, 0f, -8f, 1.34f, -8f, 4f); lineToRelative(0f, 3f); lineToRelative(16f, 0f); lineToRelative(0f, -3f); curveToRelative(0f, -2.66f, -5.33f, -4f, -8f, -4f); close()
        }.build()
}

private val MoneyIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Money", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = SolidColor(PrimaryPurple)) {
            moveTo(11.8f, 10.9f); curveToRelative(-2.27f, -0.59f, -3f, -1.2f, -3f, -2.15f); curveToRelative(0f, -1.09f, 1.01f, -1.85f, 2.7f, -1.85f); curveToRelative(1.78f, 0f, 2.44f, 0.85f, 2.5f, 2.1f); lineToRelative(2.21f, 0f); curveToRelative(-0.07f, -1.72f, -1.12f, -3.3f, -3.21f, -3.81f); lineTo(13f, 3f); lineToRelative(-2f, 0f); lineToRelative(0f, 2.16f); curveToRelative(-1.94f, 0.42f, -3.5f, 1.68f, -3.5f, 3.61f); curveToRelative(0f, 2.31f, 1.91f, 3.46f, 4.7f, 4.13f); curveToRelative(2.5f, 0.6f, 3f, 1.48f, 3f, 2.41f); curveToRelative(0f, 0.69f, -0.49f, 1.79f, -2.7f, 1.79f); curveToRelative(-2.06f, 0f, -2.87f, -0.92f, -2.98f, -2.1f); lineTo(6.3f, 15f); curveToRelative(0.12f, 2.19f, 1.76f, 3.42f, 3.7f, 3.84f); lineTo(10f, 21f); lineToRelative(2f, 0f); lineToRelative(0f, -2.15f); curveToRelative(1.95f, -0.37f, 3.5f, -1.5f, 3.5f, -3.55f); curveToRelative(0f, -2.84f, -2.43f, -3.81f, -4.7f, -4.4f); close()
        }.build()
}

private val PersonIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Person", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = SolidColor(PrimaryPurple)) {
            moveTo(12f, 12f); curveToRelative(2.21f, 0f, 4f, -1.79f, 4f, -4f); curveToRelative(0f, -2.21f, -1.79f, -4f, -4f, -4f); curveToRelative(-2.21f, 0f, -4f, 1.79f, -4f, 4f); curveToRelative(0f, 2.21f, 1.79f, 4f, 4f, 4f); close()
            moveTo(12f, 14f); curveToRelative(-2.67f, 0f, -8f, 1.34f, -8f, 4f); lineToRelative(0f, 2f); lineToRelative(16f, 0f); lineToRelative(0f, -2f); curveToRelative(0f, -2.66f, -5.33f, -4f, -8f, -4f); close()
        }.build()
}

private val PhoneIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Phone", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = SolidColor(PrimaryPurple)) {
            moveTo(6.62f, 10.79f); curveToRelative(1.44f, 2.83f, 3.76f, 5.14f, 6.59f, 6.59f); lineToRelative(2.2f, -2.2f); curveToRelative(0.27f, -0.27f, 0.67f, -0.36f, 1.02f, -0.24f); curveToRelative(1.12f, 0.37f, 2.33f, 0.57f, 3.57f, 0.57f); curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f); lineToRelative(0f, 3.5f); curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f); curveToRelative(-9.39f, 0f, -17f, -7.61f, -17f, -17f); curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f); lineToRelative(3.5f, 0f); curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f); curveToRelative(0f, 1.25f, 0.2f, 2.45f, 0.57f, 3.57f); curveToRelative(0.11f, 0.35f, 0.03f, 0.74f, -0.25f, 1.02f); lineToRelative(-2.2f, 2.2f); close()
        }.build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel,
    onProfileSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(DarkPurpleBg, DarkPurpleSurface))
    } else {
        Brush.verticalGradient(listOf(LightLavender, Color.White))
    }

    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "save_button_scale"
    )

    val buttonBrush = if (!state.isLoading) {
        Brush.horizontalGradient(listOf(PrimaryPurple, IndigoPurple))
    } else {
        SolidColor(DisabledPurple.copy(alpha = 0.5f))
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onProfileSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My student profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) DarkPurpleSurface else LightLavender
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                state.errorMessage?.let { StudentMessageBanner(it, isError = true) }
                state.successMessage?.let { StudentMessageBanner(it, isError = false) }

                // Card 1: Education Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) DarkPurpleSurface else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Education",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StudentFormTextField(
                            value = state.registrationNumber,
                            onValueChange = viewModel::onRegistrationNumberChanged,
                            label = "Registration number",
                            error = state.formErrors[FormField.REGISTRATION_NUMBER],
                            leadingIcon = { Icon(BadgeIcon, contentDescription = null, tint = PrimaryPurple) },
                            enabled = !state.isLoading
                        )
                        StudentFormTextField(
                            value = state.institutionName,
                            onValueChange = viewModel::onInstitutionNameChanged,
                            label = "Institution",
                            error = state.formErrors[FormField.INSTITUTION_NAME],
                            leadingIcon = { Icon(SchoolIcon, contentDescription = null, tint = PrimaryPurple) },
                            enabled = !state.isLoading
                        )
                        StudentFormTextField(
                            value = state.courseName,
                            onValueChange = viewModel::onCourseNameChanged,
                            label = "Course / programme",
                            error = state.formErrors[FormField.COURSE_NAME],
                            leadingIcon = { Icon(BookIcon, contentDescription = null, tint = PrimaryPurple) },
                            enabled = !state.isLoading
                        )
                        StudentFormTextField(
                            value = state.yearOfStudyInput,
                            onValueChange = viewModel::onYearOfStudyChanged,
                            label = "Year of study",
                            error = state.formErrors[FormField.YEAR_OF_STUDY],
                            leadingIcon = { Icon(CalendarIcon, contentDescription = null, tint = PrimaryPurple) },
                            keyboardType = KeyboardType.Number,
                            enabled = !state.isLoading
                        )
                    }
                }

                // Card 2: Household Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) DarkPurpleSurface else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Household",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StudentFormTextField(
                            value = state.householdSizeInput,
                            onValueChange = viewModel::onHouseholdSizeChanged,
                            label = "Number of people in household",
                            error = state.formErrors[FormField.HOUSEHOLD_SIZE],
                            leadingIcon = { Icon(PeopleIcon, contentDescription = null, tint = PrimaryPurple) },
                            keyboardType = KeyboardType.Number,
                            enabled = !state.isLoading
                        )
                        StudentFormTextField(
                            value = state.monthlyHouseholdIncomeKesInput,
                            onValueChange = viewModel::onMonthlyHouseholdIncomeKesChanged,
                            label = "Monthly household income (KES)",
                            error = state.formErrors[FormField.MONTHLY_HOUSEHOLD_INCOME_KES],
                            leadingIcon = { Icon(MoneyIcon, contentDescription = null, tint = PrimaryPurple) },
                            keyboardType = KeyboardType.Number,
                            enabled = !state.isLoading
                        )
                    }
                }

                // Card 3: Guardian (optional) Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) DarkPurpleSurface else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Guardian (optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StudentFormTextField(
                            value = state.guardianName,
                            onValueChange = viewModel::onGuardianNameChanged,
                            label = "Guardian's name",
                            error = state.formErrors[FormField.GUARDIAN_NAME],
                            leadingIcon = { Icon(PersonIcon, contentDescription = null, tint = PrimaryPurple) },
                            enabled = !state.isLoading
                        )
                        StudentFormTextField(
                            value = state.guardianPhoneNumber,
                            onValueChange = viewModel::onGuardianPhoneNumberChanged,
                            label = "Guardian's phone number",
                            error = state.formErrors[FormField.GUARDIAN_PHONE_NUMBER],
                            leadingIcon = { Icon(PhoneIcon, contentDescription = null, tint = PrimaryPurple) },
                            keyboardType = KeyboardType.Phone,
                            enabled = !state.isLoading,
                            helperText = "Both guardian fields are needed if you fill in either."
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Full-width gradient Save button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .background(buttonBrush, shape = RoundedCornerShape(12.dp))
                ) {
                    Button(
                        onClick = viewModel::saveStudentProfile,
                        enabled = !state.isLoading,
                        interactionSource = buttonInteractionSource,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (state.hasExistingProfile) "Update profile" else "Save profile",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
